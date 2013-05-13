package edu.mit.csail.netmap.sensors;

/** 
 * Wraps the callback to an asynchronous network performance measurement call.
 * 
 * @see NetMap#measureAsync(String, MeasureCallback)
 */
public interface MeasureCallback {
  /**
   * Called after the network performance measurement is collected and stored.
   * 
   * @param digest the digest of the network measurement; this is null if the
   *     measurement data could not be collected, or if an error occurred while
   *     storing the data
   */
  public void done(String digest);
}