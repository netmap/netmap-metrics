package edu.mit.csail.netmap.sensors;

/** 
 * Wraps the callback to an asynchronous network measurement call.
 * 
 * @see Sensors#measureAsync(String, MeasurementCallback)
 */
public interface MeasureCallback {
  /**
   * Called after the sensor measurement is collected and stored.
   * 
   * @param digest the digest of the sensor measurement; this is null if the
   *     measurement data could not be collected, or if an error occurred while
   *     storing the data
   */
  public void done(String digest);
}

