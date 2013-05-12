package edu.mit.csail.netmap.sensors;

import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.os.Handler;


/**
 * Entry point to the sensors package.
 */
public final class Sensors {
  /** The object that gets notified */
  static EventClient eventClient = null;
  
  /**
   * Asynchronously collects and stores a network performance measurement.
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "ndt,wifi-ap"
   * @param callback receives the digest of the measurement
   * @return the Thread doing the storage and computation
   * 
   * @see MeasureCallback#done(String)
   * @see Sensors#measure(String)
   */
  public static Thread measureAsync(final String measurements,
                                    final MeasureCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        final String digest = Sensors.measure(measurements); 
        callback.done(digest);
      }
    });
    thread.start();
    return thread;
  }
  
  /**
   * Synchronously collects and stores a network performance measurement.
   * 
   * This method should not be called on any thread handling UI events, such as
   * the UI thread or JavaScript execution thread.
   * Use {@link Sensors#measureAsync(String, MeasureCallback)} in those cases.
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "ndt,wifi-ap"
   * @return the digest of the measurement
   */
  public static String measure(String measurements) {
    StringBuffer jsonData = new StringBuffer(); 
    Sensors.readSensors(measurements, jsonData);
    return Recorder.storeReading(jsonData.toString());    
  }
  
  /**
   * Sets up all the sensors.
   * 
   * This should be called once from {@link Application#onCreate()}.
   * 
   * @param context the application's Android context
   */
  public static void initialize(Context context) {
    if (initialized) return;
    initialized = true;
        
    // Config goes first, so every other module can use it right away.
    Config.initialize(context);
    // Recorder goes right after config, so the actual sensors can use it.
    Recorder.initialize(context);
    
    // Location goes before location sensors.
    Location.initialize(context);
    // Sensors get initialized here.
    Battery.initialize(context);
    Gps.initialize(context);
    GSM.initialize(context);
    Network.initialize(context);
    Phone.initialize(context);
    WiFi.initialize(context);
  }
  private static boolean initialized = false;
  
  /**
   * Collects the sensor reading data that will be stored in the database. 
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "latency,speed"
   * @param jsonData {@link StringBuffer} that receives the reading data,
   *     formatted as a JSON string
   */
  private static void readSensors(String measurements, StringBuffer jsonData) {
    HashSet<String> keywords = new HashSet<String>();
    for (String measurement : measurements.split(",")) {
      keywords.add(measurement);
    }
    
    jsonData.append("{");
    Config.getJsonFragment(jsonData);
    jsonData.append(",\"location\":");
    Location.getJson(jsonData);
    jsonData.append(",\"battery\":");
    Battery.getJson(jsonData);
    jsonData.append(",\"cellular\":");
    Phone.getJson(jsonData);
    if (!keywords.contains("nogps")) {
      jsonData.append(",\"gps\":");
      Gps.getJson(jsonData);
    }
    if (!keywords.contains("nowifi")) {
      jsonData.append(",\"wifi\":");
      WiFi.getJson(jsonData);
    }
    if (!keywords.contains("nogsm")) {
      jsonData.append(",\"gsm\":");
      GSM.getJson(jsonData);
    }
    if (keywords.contains("ndt")) {
      // HACK(pwnall): measure and getJson should be combined
      Network.measure();      
      jsonData.append(",\"ndt\":");
      Network.getJson(jsonData);
    }
    jsonData.append("}");
  }
  
  /**
   * Sets the object that receives sensor-related event notifications.
   * 
   * @param eventClient_ the object that will receive new event notifications;
   *     this will replace an old event client
   */
  public static void setEventClient(EventClient eventClient_) {
    eventClient = eventClient_;
  }
}
