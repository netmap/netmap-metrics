package edu.mit.csail.netmap;

import edu.mit.csail.netmap.sensors.Config;
import edu.mit.csail.netmap.sensors.Location;
import edu.mit.csail.netmap.sensors.Recorder;
import edu.mit.csail.netmap.sensors.Sensors;
import android.content.Context;

/**
 * Facade for the network measurement code.
 * 
 * Most games should only have to interact with methods in this class.
 */
public final class NetMap {
  /**
   * Sets up the network measurement modules.
   * 
   * @param context the application's Android context
   */
  public static final void initialize(Context context) {
    Sensors.initialize(context);
  }
  
  /**
   * Synchronously collects and stores a network performance measurement.
   * 
   * This method should not be called on any thread handling UI events, such as
   * the UI thread or JavaScript execution thread. Use
   * {@link NetMap#measureAsync(String, MeasureCallback)} in those cases.
   * 
   * @param measurements comma-separated list of measurements to be performed,
   *     e.g. "ndt,wifi-ap"
   * @return the digest of the measurement
   */
  public static final String measure(String measurements) {
    StringBuffer jsonData = new StringBuffer(); 
    Sensors.readSensors(measurements, jsonData);
    return Recorder.storeReading(jsonData.toString());    
  }
  
  /**
   * Synchronously uploads a batch of measurements to the NetMap servers.
   * 
   * This method should not be called on any thread handling UI events, such as
   * the UI thread or JavaScript execution thread. Use
   * {@link NetMap#uploadAsync(UploadCallback)} in those cases.
   * 
   * @return true if there are more batches of measurements available; if the
   *    method returns true and the environment still facilitates uploading
   *    (e.g. the phone is charging and connected to a WiFi network), the caller
   *    should call this method again
   */
  public static final boolean upload() {
    return Recorder.uploadReadingPack();
  }

  /**
   * Sets the listener that receives NetMap event notifications.
   * 
   * There can only be one listener at a time. Calling this method replaces the
   * previously set listener.
   * 
   * @param listener the object that will receive event notifications
   */
  public static void setListener(NetMapListener listener) {
    Sensors.setEventClient(listener);
  }
  
  /**
   * Starts or stops tracking the user's location.
   * 
   * This method is idempotent.
   * 
   * @param enabled if true, the user's location will be tracked using all the
   *     available sensors and {@link NetMapListener#onLocation()} will be
   *     called when the user's location changes
   */
  public static void trackLocation(boolean enabled) {
    if (enabled) {
      Location.on();
    } else {
      Location.off();
    }
  }
  
  /**
   * The most recent known user location.
   * 
   * @return the most recent / accurate location; can be null if no location
   *     information is available; if trackLocation is not set, the retuned
   *     location information might be significantly old
   */
  public static android.location.Location location() {
    return Location.last();
  }
  
  /**
   * Configures the process of uploading data to the NetMap server.
   * 
   * The configuration information is persisted across application restarts. To
   * change it, call {@link NetMap#configure(String)} again.
   * 
   * @param uid the user token that identifies the application and user that
   *    is submitting data to the NetMap server
   */
  public static final void configure(String uid) {
    NetMap.configure(uid, "http://netmap-data.pwnb.us/readings/");
  }
  
  /**
   * Configures the process of uploading data to the NetMap server.
   * 
   * The configuration information is persisted across application restarts. To
   * change it, call {@link NetMap#configure(String)} again.
   * 
   * @param uid the user token that identifies the application and user that
   *    is submitting data to the NetMap server
   */
  public static final void configure(String uid, String url) {
    Config.setReadingsUploadBackend(url, uid);
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

  /**
   * Synchronously uploads a batch of measurements to the NetMap servers.
   * 
   * This method should not be called on any thread handling UI events, such as
   * the UI thread or JavaScript execution thread. Use
   * {@link NetMap#uploadAsync(UploadCallback)} in those cases.
   * 
   * @return true if there are more batches of measurements available; if the
   *    method returns true and the environment still facilitates uploading
   *    (e.g. the phone is charging and connected to a WiFi network), the caller
   *    should call this method again
   * 
   * @see MeasureCallback#done(String)
   * @see NetMap#measure(String)
   */
  public static Thread uploadAsync(final UploadCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        final boolean stillHasData = upload(); 
        callback.done(stillHasData);
      }
    });
    thread.start();
    return thread;
  }  

  /**
   * The most recent known user location.
   * 
   * @return the most recent / accurate location, encoded as a JSON object; this
   *     is meant to be passed to a JavaScript API
   *     
   * @see NetMap#location()
   */
  public static String locationJson() {
    StringBuffer buffer = new StringBuffer();
    Location.getJson(buffer);
    return buffer.toString();
  }
  
  /** This class is not intended to be instantiated. */
  private NetMap() { }
}
