package edu.mit.csail.netmap.sensors;

/** 
 * Wraps the callback to an asynchronous measurement upload call.
 * 
 * @see NetMap#uploadAsync(UploadCallback)
 */
public interface UploadCallback {
  /**
   * Called after the measurement data is uploaded to the NetMap server.
   * 
   * @param hasMoreData true if there are more batches of measurements
   *    available; if the method returns true and the environment still
   *    facilitates uploading (e.g. the phone is charging and connected to a
   *    WiFi network), the caller should call this method again
   */
  public void done(boolean hasMoreData);
}