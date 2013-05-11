package edu.mit.csail.netmap.sensors;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.http.AndroidHttpClient;
import android.util.Log;

/** Queues up sensor readings so they can be uploaded over WiFi. */
public final class Recorder {
  /** Approximate size of a pack of readings. */
  private static final int PACK_SIZE = 128 * 1024;
  
  /** User-Agent header used during uploading. */
  private static final String USER_AGENT = "NetMap/1.0 Recorder/1.0";

  /** Logging tag. */
  private static final String TAG = "recorder";
  
  /** The table that recordings are saved into. */
  private static final String TABLE_NAME = "metrics";
  
  /** Android context used to access the application's private storage. */
  private static Context context_ = null;
  
  /** The database that stores sensor readings. */
  private static SQLiteDatabase db_ = null;
  
  /** Message digest cloned for each reading. */
  private static MessageDigest digestPrototype_ = null;
  
  /** Encoding used when computing the crypto-hash of a sensor reading. */
  private static Charset digestCharset_ = null;
  
  /**
   * Adds a sensor reading to the transmission queue.
   * 
   * @param jsonData the reading's information, encoded as a JSON string
   * @return a cryptographic hash of the reading 
   */
  public static final String storeReading(String jsonData) {
    String digest = Recorder.digest(jsonData);
    insertReading(jsonData);
    return digest;
  }
  
  /**
   * 
   *
   * @return if true, the caller should call {@link #sendPack(URI)} again to
   *     upload more data 
   */
  public static final boolean uploadReadingPack() {
    StringBuffer packData = new StringBuffer();
    long lastReadingId = readPack(packData, PACK_SIZE);
    if (lastReadingId == 0) {
      // No stored readings.
      return false;
    }
    if (!uploadPackData(packData.toString(), Config.getReadingsUploadUri())) {
      // The data upload failed.
      return true;
    }
    deletePack(lastReadingId);
    return true;
  }
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static final void initialize(Context context) {
    context_ = context;
    
    DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(context);    
    db_ = databaseOpenHelper.getWritableDatabase();
    
    try {
      digestPrototype_ = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, "Device does not support SHA-256");
    }
    digestCharset_ = Charset.defaultCharset();
  }
  
  /**
   * Fetches queued sensor readings from the database, so they can be uploaded.
   * 
   * @param packData {@link StringBuffer} that receives readings, in a format
   *     suitable for server consumption
   * @param packSize a guideline for the size of JSON data to be read
   * @return the ID of the last reading 
   */
  private static final long readPack(StringBuffer packData, int packSize) {
    long lastReadingId = 0;  // INTEGER PRIMARY KEY values start at 1
    int limit = 100;
    
    // The size guideline only applies to the data that we add in this call.
    packSize += packData.length();
    
    while (true) {
      final Cursor cursor = db_.rawQuery("SELECT id, json FROM " + TABLE_NAME +
          " WHERE id > " + Long.toString(lastReadingId) +
          " ORDER BY id LIMIT " + Integer.toString(limit), null);
      if (!cursor.moveToFirst()) {
        // Empty result set.
        break;
      }
      while (true) {
        lastReadingId = cursor.getLong(0);
        final String jsonData = cursor.getString(1);
        appendReading(packData, jsonData);
        if (packData.length() >= packSize) {
          // Got enough data.
          cursor.close();
          return lastReadingId;
        }
        
        if (!cursor.moveToNext()) {
          // Done with this query set.
          cursor.close();
          break;
        }
      }
    }
    return lastReadingId;
  }
  
  /**
   * Uploads an already-assembled pack of sensor readings data to the server.
   * 
   * @param packData the sensor readings data to be uploaded; this should be
   *     prepared by {@link #readPack(StringBuffer, int)}
   * @param uploadUri {@link URI} for the HTTP backend that receives sensor data
   * @return true if the upload operation succeeded, false if an error occurred
   */
  private static final boolean uploadPackData(String packData, URI uploadUri) {
    HttpPost request = new HttpPost(uploadUri);
    try {
      request.setEntity(new StringEntity(packData));
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, "http.entity.StringEntity rejected pack");
      return false;
    }
    HttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT, context_);
    try {
      HttpResponse response = httpClient.execute(request);
      int statusCode = response.getStatusLine().getStatusCode();
      
      // Must read the response, otherwise AndroidHttpClient leaks.
      String jsonResponse = "{}";
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream content = entity.getContent();
        Scanner scanner = new Scanner(content).useDelimiter("\\A");
        if (scanner.hasNext()) {
          jsonResponse = scanner.next();
        }
        scanner.close();
        entity.consumeContent();
      }
      
      if (statusCode >= 200 && statusCode < 300) {
        return true;
      }
      // TODO(pwnall): invalid JSON should be removed from the database,
      //               otherwise there will be an infinite loop
      
      Log.e(TAG, "Server error during pack upload: " + jsonResponse);
      return false;
    } catch (ClientProtocolException e) {
      Log.e(TAG, "ClientProtocolException during pack upload");
      return false;
    } catch (IOException e) {
      Log.e(TAG, "IOException during pack upload");
      return false;
    }
  }
  
  /**
   * Removes successfully uploaded sensor readings from the database.
   * 
   * This method permanently removes the readings from the local database, so it
   * should only be called 
   * 
   * @param lastReadingId return value from {@link #readPack(ArrayList, int)}
   */
  private static final void deletePack(long lastReadingId) {
    if (lastReadingId == 0) {
      // readPack did not return any data
      return;
    }
    db_.execSQL("DELETE FROM  " + TABLE_NAME + " WHERE id <= " +
        Long.toString(lastReadingId));
  }

  /**
   * Adds a reading's data to a pack that will be uploaded to the server.
   * 
   * @param packData {@link StringBuffer} that receives readings, in a format
   *     suitable for server consumption
   * @param jsonData a sensor reading's data, encoded as a JSON string
   */
  private static final void appendReading(StringBuffer packData,
      String jsonData) {
    packData.append(jsonData);
    packData.append('\n');
  }
  
  /**
   * 
   * 
   * @param jsonData the reading's information, encoded as a JSON string
   */
  private static final void insertReading(String jsonData) {
    ContentValues values = new ContentValues();
    values.put("json", jsonData);
    db_.insert(TABLE_NAME, null, values);
  }
  
  /**
   * Computes a cryptographically secure hash of a reading's data.
   * 
   * @param jsonData the reading's information, encoded as JSON string
   * @return a small string that uniquely identifies this reading
   */
  private static final String digest(String jsonData) {
    MessageDigest digest;
    try {
      digest = (MessageDigest)digestPrototype_.clone();
    } catch(CloneNotSupportedException e) {
      try {
        digest = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e1) {
        Log.e(TAG, "Device does not support SHA-256");
        return null;
      }
    }
    byte[] digestBytes = digest.digest(jsonData.getBytes(digestCharset_));
    StringBuffer hexDigest = new StringBuffer();
    for (byte b : digestBytes) {
      String hexByte = Integer.toHexString(b & 0xff);
      if (hexByte.length() != 2) {  // hexByte will have 1 or 2 hex digits
        hexDigest.append('0');
      }
      hexDigest.append(hexByte);
    }
    return hexDigest.toString();
  }  

  /** Opens the readings database and creates its schema. */
  private static final class DatabaseOpenHelper extends SQLiteOpenHelper {
    public DatabaseOpenHelper(Context context) {
      super(context, "metrics", null, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // We'll fill this in if the database ever requires schema changes.
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME +
          " (id INTEGER PRIMARY KEY, json TEXT);");
    }    
  }
}
