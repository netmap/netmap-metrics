package edu.mit.csail.netmap.sensors;

import android.content.Context;
import android.location.LocationManager;

public final class Location {
  /** The most recent location passed to onLocationChanged. */
  private static android.location.Location lastLocation;
  
  private static LocationManager locationManager;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    locationManager =
        (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
  }
  
  /**
   * The last known location.
   * 
   * @return the last known location, or null if no location information is
   *     available
   */
  public static android.location.Location last() {
    if (lastLocation != null) {
      return lastLocation;
    }
    
    for (String provider : locationManager.getAllProviders()) {
      android.location.Location location =
          locationManager.getLastKnownLocation(provider);
      if (location == null) continue;
      useLocation(location);
    }
    return lastLocation;
  }
  
  /**
   * Writes a JSON representation of the location data to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *     the GPS sensor data
   */
  public static void getJson(StringBuffer buffer) {
    // Cache the location we'll report, to avoid race conditions.
    android.location.Location location = lastLocation; 
    
    if (location == null) {
      buffer.append("{}");
      return;
    }
    buffer.append("{\"latitude\":");
    buffer.append(location.getLatitude());
    buffer.append(",\"longitude\":");
    buffer.append(location.getLongitude());
    buffer.append(",\"provider\":\"");
    buffer.append(location.getProvider());
    buffer.append("\",\"timestamp\":");
    buffer.append(location.getTime());
    if (location.hasAccuracy()) {
      buffer.append(",\"accuracy\":");
      buffer.append(location.getAccuracy());
    }
    if (location.hasAltitude()) {
      buffer.append(",\"altitude\":");
      buffer.append(location.getAltitude());
    }
    if (location.hasBearing()) {
      // NOTE: calling this "heading" for consistency with HTML5 Geolocation
      buffer.append(",\"heading\":");
      buffer.append(location.getBearing());
    }
    if (location.hasSpeed()) {
      buffer.append(",\"speed\":");
      buffer.append(location.getSpeed());
    }
    buffer.append("}");
  }
  
  public static void on() {
    // Fire off an update right away.
    if (Sensors.eventClient != null) {
      last();
      if (lastLocation != null) {
        Sensors.eventClient.onLocation();
      }
    }

    Gps.start();    
    WiFi.start();
    GSM.start();
  }
  
  public static void off() {
    Gps.stop();
    WiFi.stop();
    GSM.stop();
  }
  
  /**
   * Called by various sensors' listeners to report location updates.
   * 
   * @param location Android's most recent location estimate
   */
  static void onLocationChanged(android.location.Location location) {
    boolean updated = useLocation(location);
    
    if (updated && Sensors.eventClient != null) {
      Sensors.eventClient.onLocation();
    }
  }
  
  /**
   * Updates internal location state to account for new location information.
   * 
   * @param location new information provided by Android
   * @return true if the location state was updated, false if the new
   *     information wasn't used
   */
  private static boolean useLocation(android.location.Location location) {
    if (lastLocation == null) {
      lastLocation = location;
      return true;
    }
    
    if (location.getTime() < lastLocation.getTime()) {
      return false;
    }
    
    // TODO(pwnall): discard noise
    lastLocation = location;
    return true;
  }
}
