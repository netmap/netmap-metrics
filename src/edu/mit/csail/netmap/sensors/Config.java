package edu.mit.csail.netmap.sensors;

import java.net.URI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/* Stores persistent configuration supplied by the game side. */
public class Config {
  /** Name used by the sensors preferences file. */
  private static final String PREFERENCES_NAME = "recorder";

  /** Persistent settings, such as the upload server URI. */
  private static SharedPreferences preferences_ = null;
  
  /** URI of the HTTP backend that receives sensor reading data. */
  private static URI uploadUri_ = null;
  
  /** The value of the 'uid' field in sensor reading data. */
  private static String uid_ = null;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static final void initialize(Context context) {
    preferences_ = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE);
    uploadUri_ = URI.create(preferences_.getString("uploadUrl",
        "http://netmap.csail.mit.edu/net_readings/"));
    uid_ = preferences_.getString("uploadUid", "");
  }
  
  /**
   * Sets the HTTP backend that receives the sensor readings.
   * 
   * The backend information will be persisted until a new backend is set by
   * calling this method.
   * 
   * @param url fully-qualified URL to the HTTP backkend that will receive
   *     readings as POST data
   * @param uid the user token to be stored in the "uid" property of all sensor
   *     readings
   */
  public static final void setReadingsUploadBackend(String url, String uid) {
    // Check that the URL parses before making persistent changes.
    URI uploadUri = URI.create(url);
    
    Editor editor = preferences_.edit();
    editor.putString("uploadUrl", url);
    editor.putString("uploadUid", uid);
    editor.commit();
    
    uploadUri_ = uploadUri;
    uid_ = uid;
  }
  
  /**
   * The URL of the HTTP backend that receives the sensor readings.
   *
   * @return the URL of the HTTP backend that receives the sensor readings
   */
  public static final URI getReadingsUploadUri() {
    return uploadUri_;
  }
  
  /**
   * The user token used as the "uid" value in sensor readings.
   *
   * @return the user token used as the "uid" value in sensor readings
   */
  public static final String getReadingsUploadUid() {
    return uid_;
  }
  
  /**
   * Writes a JSON representation of the sensor config to the given buffer.
   * 
   * @param jsonData a {@link StringBuffer} that receives a JSON representation
   *     of the GPS sensor data
   */
  public static final void getJsonFragment(StringBuffer buffer) {
    buffer.append("\"uid\":\"");
    buffer.append(uid_);
    buffer.append("\",\"timestamp\":");
    buffer.append(System.currentTimeMillis());
  }
}
