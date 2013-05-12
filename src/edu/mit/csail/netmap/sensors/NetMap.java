package edu.mit.csail.netmap.sensors;

import android.content.Context;

/**
 * Facade for the network measurement code.
 * 
 * Most games should only have to interact with methods in this class.
 */
public class NetMap {
  /**
   * Sets up the network measurement modules.
   * 
   * @param context the application's Android context
   */
  public static void initialize(Context context) {
    Sensors.initialize(context);
  }
  
  /**
   * Synchronously collects and stores a network performance measurement.
   * 
   * This method should not be called on any thread handling UI events, such as
   * the UI thread or JavaScript execution thread.
   * Use {@link NetMap#measureAsync(String, MeasureCallback)} in those cases.
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
   * Synchronously uploads a batch of measurements to the NetMap servers.
   * 
   * @return true if there are more batches of measurements available; if the
   *    method returns true and the environment still facilitates uploading
   *    (e.g. the phone is charging and connected to a WiFi network), the caller
   *    should call this method again
   */
  public static boolean upload() {
    return Recorder.uploadReadingPack();
  }
  
  /**
   * Asynchronously collects and stores a network performance measurement.
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "ndt,wifi-ap"
   * @param callback receives the digest of the measurement
   * @return the Thread doing the storage and computation
   * 
   * @see MeasureCallback#done(String)
   * @see NetMap#measure(String)
   */
  public static Thread measureAsync(final String measurements,
                                    final MeasureCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        final String digest = measure(measurements); 
        callback.done(digest);
      }
    });
    thread.start();
    return thread;
  }  
  
  /** This class is not intended to be instantiated. */
  private NetMap() { }
}
