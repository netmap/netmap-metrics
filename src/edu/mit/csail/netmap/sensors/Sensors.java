package edu.mit.csail.netmap.sensors;

import java.util.HashSet;

import android.app.Application;
import android.content.Context;


/**
 * Entry point to the sensors package.
 */
public final class Sensors {
  /** The object that gets notified */
  static EventClient eventClient = null;
  
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
    Gps.initialize(context);
    WiFi.initialize(context);
    GSM.initialize(context);
  }
  private static boolean initialized = false;
  
  /**
   * Collect the sensor reading data that will be stored in the database. 
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "latency,speed"
   * @param jsonData {@link StringBuffer} that receives the reading data,
   *     formatted as a JSON string
   */
  public static void readSensors(String measurements, StringBuffer jsonData) {
    HashSet<String> keywords = new HashSet<String>();
    for (String measurement : measurements.split(",")) {
      keywords.add(measurement);
    }
    
    jsonData.append("{");
    Config.getJsonFragment(jsonData);
    jsonData.append(",\"location\":");
    Location.getJson(jsonData);
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
