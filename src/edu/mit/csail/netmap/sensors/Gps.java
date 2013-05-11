package edu.mit.csail.netmap.sensors;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;

/**
 * Information from the phone's GPS sensor.
 */
public final class Gps {
  /** Entry point to Android's GPS functionality. */
  private static LocationManager locationManager;
  
  /** The instance passed to all requestLocationUpdates() calls. */
  private static LocationStatusListener locationListener;
  
  /** The thread that processes location update events. */
  private static Looper locationListenerLooper;

  /** Current GPS status. */
  private static GpsStatus gpsStatus;
  
  /** True when the GPS is enabled by the user. */
  private static boolean enabled = false;
  
  /** True when the GPS is powered up and reporting information. */
  private static boolean started = false;
  
  /** True when the GPS won't be working for quite some time. */
  private static boolean outOfService = false;
  
  /** True when the GPS won't be working for a short while. */
  private static boolean unavailable = false;
  
  /** True when listening for GPS updates. */
  private static boolean listening = false;
    
  /** Number of milliseconds between location updates. */
  private static final long PRECISION_TIME_MS = 100;
  
  /** Number of meters between location updates. */
  private static final float PRECISION_DISTANCE_M = 0.5f;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    locationManager =
        (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    locationManager.addGpsStatusListener(new GpsStatusListener());
    locationManager.addNmeaListener(new NmeaStatusListener());
    
    gpsStatus = null;
    locationListener = new LocationStatusListener();
    locationListenerLooper = context.getMainLooper();
  }
  
  /**
   * Starts listening for location updates.
   * 
   * This should be called when your application / activity becomes active.
   */
  public static void start() {
    if (listening) return;
    // We only get onProviderDisabled() when we start listening.
    enabled = isEnabled();
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        PRECISION_TIME_MS, PRECISION_DISTANCE_M, locationListener,
        locationListenerLooper);
    listening = true;
  }

  /** 
   * Stops listening for location updates.
   * 
   * This should be called when your application / activity is no longer active.
   */
  public static void stop() {
    if (!listening) return;
    locationManager.removeUpdates(locationListener);
    listening = false;
  }
  
  /**
   * Checks if the user's preferences allow the use of GPS.
   * 
   * @return true if the user lets us use GPS
   */
  public static boolean isEnabled() {
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }
  
  /**
   * Writes a JSON representation of the GPS data to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *     the GPS sensor data
   */
  public static void getJson(StringBuffer buffer) {
    buffer.append("{\"enabled\":");
    buffer.append(enabled ? "true" : "false");
    if (started) {
      buffer.append(",\"started\": true");
    }
    if (outOfService) {
      buffer.append(",\"outOfService\": true");
    } else if (unavailable) {
      buffer.append(",\"unavailable\": true");
    }
    if (gpsStatus != null) {
      buffer.append(",\"timeToFix\":");
      int timeToFixMs = gpsStatus.getTimeToFirstFix();
      buffer.append(timeToFixMs / 1000);
      buffer.append('.');
      buffer.append(String.format("%03d", timeToFixMs % 1000));
      buffer.append(",\"satellites\":[");
      boolean firstElement = true;
      for (GpsSatellite satellite : gpsStatus.getSatellites()) {
        if (firstElement) {
          firstElement = false;
          buffer.append("{\"prn\":");
        } else {
          buffer.append(",{\"prn\":");
        }
        buffer.append(satellite.getPrn());
        buffer.append(",\"used\":");
        buffer.append(satellite.usedInFix());
        buffer.append(",\"almanac\":");
        buffer.append(satellite.hasAlmanac());
        buffer.append(",\"ephemeris\":");
        buffer.append(satellite.hasEphemeris());
        buffer.append(",\"azimuth\":");
        buffer.append(satellite.getAzimuth());
        buffer.append(",\"elevation\":");
        buffer.append(satellite.getElevation());
        buffer.append(",\"snr\":");
        buffer.append(satellite.getSnr());
        buffer.append("}");
      }
      buffer.append("]");
    }
    buffer.append("}");
  }
  
  private static class GpsStatusListener implements GpsStatus.Listener {
    @Override
    public void onGpsStatusChanged(int event) {
      gpsStatus = locationManager.getGpsStatus(gpsStatus);
      
      switch (event) {
      case GpsStatus.GPS_EVENT_STARTED:
        Gps.started = true;
        break;
      case GpsStatus.GPS_EVENT_STOPPED:
        Gps.started = false;
        break;
      case GpsStatus.GPS_EVENT_FIRST_FIX:
        // TODO(pwnall): record info
        break;
      case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
        // TODO(pwnall): record info
        break;
      }
    }
  }
  
  private static class NmeaStatusListener implements GpsStatus.NmeaListener {
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
      
    }
  }
  
  private static class LocationStatusListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
      //if (!location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
      //  return;
      //}
      edu.mit.csail.netmap.sensors.Location.onLocationChanged(location);
    }
    @Override
    public void onProviderDisabled(String provider) {
      if (!provider.equals(LocationManager.GPS_PROVIDER)) {
        return;
      }
      Gps.enabled = false;      
    }
    @Override
    public void onProviderEnabled(String provider) {
      if (!provider.equals(LocationManager.GPS_PROVIDER)) {
        return;
      }
      Gps.enabled = true;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      if (!provider.equals(LocationManager.GPS_PROVIDER)) {
        return;
      }
      switch (status) {
      case LocationProvider.OUT_OF_SERVICE:
        Gps.outOfService = true;
        Gps.unavailable = true;
        break;
      case LocationProvider.TEMPORARILY_UNAVAILABLE:
        Gps.outOfService = false;
        Gps.unavailable = true;
        break;
      case LocationProvider.AVAILABLE:
        Gps.outOfService = false;
        Gps.unavailable = false;
        break;
      }
    }
  }
}
