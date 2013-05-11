package edu.mit.csail.netmap.sensors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.measurementlab.ndt.NdtTests;

import org.json.JSONObject;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.format.Formatter;
import android.util.Log;

public final class Network {

  
  // log tag so logcat can be filtered to just the variable
  // written out by the core NDT Java code, useful for understanding
  // what is available for display in the test results
  private static final String VARS_TAG = "variables";
  
  static final String LOG_TAG = "NDT";

  // user visible test phases, more coarse than actual tests
  /**
   * Includes middle box and SFW testing.
   */
  static final int STATUS_PREPARING = 0;
  /**
   * Client to server test.
   */
  static final int STATUS_UPLOADING = 1;
  /**
   * Server to client and meta test.
   */
  static final int STATUS_DOWNLOADING = 2;
  /**
   * Indicates tests are concluded.
   */
  static final int STATUS_COMPLETE = 3;
  /**
   * Indicates the service encountered an error.
   */
  static final int STATUS_ERROR = 4;

  /**
   * Intent for sending updates from this service to trigger display changes
   * in @link {@link TestsActivity}.
   */
  static final String INTENT_UPDATE_STATUS = "net.measurementlab.ndt.UpdateStatus";
  /**
   * Intent for @link {@link TestsActivity} to request that this service stop
   * testing.
   */
  static final String INTENT_STOP_TESTS = "net.measurementlab.ndt.StopTests";

  /**
   * Label for extra data in {@link Intent} sent for test status updates.
   */
  static final String EXTRA_STATUS = "status";

  /**
   * Label for extra data about network type in {@link Intent} sent from
   * {@link InitialActivity}.
   */
  static final String EXTRA_NETWORK_TYPE = "networkType";

  /**
   * Status line used for the advanced display.
   */
  static final String EXTRA_DIAG_STATUS = "diagnosticStatus";

  /**
   * Server against which to perform the test.
   */
  static final String EXTRA_SERVER_HOST = "serverHost";

  /**
   * Label for variables captured during testing and used in presentation of
   * results in {@link ResultsActivity}.
   */
  static final String EXTRA_VARS = VARS_TAG;

  private Intent intent;

  private String networkType;

  private static CaptiveUiServices uiServices;

  private BroadcastReceiver stopReceiver;
  
  /** True when the WiFi is enabled by the user. */
  private static boolean enabled = false;
  
  /** True when the WiFi is powered up and reporting information. - not being used now*/
  private static boolean started = false;
  
  /** True when listening for WiFi updates. */
  private static boolean listening = false;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
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
 // this interface is how the service is able to observe the
    // tests as run by the core Java client code
    uiServices = new CaptiveUiServices();
    String serverHost = intent.getStringExtra(EXTRA_SERVER_HOST);
    if (null == serverHost) {
      serverHost = SelectServerActivity.lookupHostname(getBaseContext(), SelectServerActivity.DEFAULT_SERVER);
    }

    
    try {
      new Thread(new NdtTests(serverHost, uiServices, networkType))
          .start();
    } catch (Throwable tr) {
      Log.e(LOG_TAG, "Problem running tests.", tr);
     }
    
    
    listening = true;
  }

  /** 
   * Stops listening for location updates.
   * 
   * This should be called when your application / activity is no longer active.
   */
  public static void stop() {
    if (!listening) return;
    wifiLock.release();
    wifiLock = null;
    listening = false;
  }
  
  /**
   * Checks if the user's preferences allow the use of WiFi.
   * 
   * @return true if the user lets us use WiFi
   */
  public static boolean isEnabled() {
      return true;
  }
  
  /**
   * Writes a JSON representation of the WiFi data to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *     the WiFisensor data
   */
  @SuppressWarnings("deprecation")
  public static void getJson(StringBuffer buffer) {
   
    buffer.append("{\"enabled\":");
    buffer.append(enabled ? "true" : "false");
  }
  private class CaptiveUiServices implements UiServices {
    private final Map<Integer, StringBuilder> statusBuffers = new HashMap<Integer, StringBuilder>();

    // sub-string that identifies the message marking start of upload
    // testing
    private static final String S2C_MSG_FRAGMENT = "server-to-client";

    // sub-string that identifies the message marking start of download
    // testing
    private static final String C2S_MSG_FRAGMENT = "client-to-server";

    private boolean wantToStop = false;

    int status = STATUS_PREPARING;

    private Map<String, Object> variables = new HashMap<String, Object>();

    CaptiveUiServices() {
      // not needed now but may be useful in the future
      // statusBuffers.put(UiServices.MAIN_VIEW, new StringBuilder());
      // statusBuffers.put(UiServices.STAT_VIEW, new StringBuilder());
      statusBuffers.put(UiServices.DIAG_VIEW, new StringBuilder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendString(String message, int viewId) {
      Log.d(LOG_TAG, String.format("Appended: (%1$d) %2$s.", viewId,
          message.trim()));

      if (statusBuffers.containsKey(viewId)) {
        statusBuffers.get(viewId).append(message);
        if (!message.endsWith("\n")) {
          statusBuffers.get(viewId).append('\n');
        }
      }

      if (message.contains(C2S_MSG_FRAGMENT)
          && UiServices.MAIN_VIEW == viewId) {
        Log.i(LOG_TAG, "Starting upload test.");
        intent.putExtra(EXTRA_STATUS, STATUS_UPLOADING);
        status = STATUS_UPLOADING;
        Log.i(LOG_TAG, "Broadcast status change.");
      }

      if (message.contains(S2C_MSG_FRAGMENT)
          && UiServices.MAIN_VIEW == viewId) {
        Log.i(LOG_TAG, "Starting download test.");
        intent.putExtra(EXTRA_STATUS, STATUS_DOWNLOADING);
        status = STATUS_DOWNLOADING;
        Log.i(LOG_TAG, "Broadcast status change.");
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementProgress() {
      Log.d(LOG_TAG, "Incremented progress.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(String str) {
      Log.e(LOG_TAG, String.format("Error: %1$s.", str.trim()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBeginTest() {
      Log.d(LOG_TAG, "Test begun.");
      wantToStop = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEndTest() {
      Log.d(LOG_TAG, "Test ended.");
      intent.putExtra(EXTRA_STATUS, STATUS_COMPLETE);
      intent.putExtra(EXTRA_DIAG_STATUS, statusBuffers.get(
          UiServices.DIAG_VIEW).toString());
      intent.putExtra(EXTRA_VARS, (Serializable) variables);
      status = STATUS_COMPLETE;
      wantToStop = false;
      Log.i(LOG_TAG, "Broadcast status change.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailure(String errorMessage) {
      Log.d(LOG_TAG, String.format("Failed: %1$s.", errorMessage));
      intent.putExtra(EXTRA_STATUS, STATUS_ERROR);
      status = STATUS_ERROR;
      wantToStop = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoginSent() {
      Log.d(LOG_TAG, "Login sent.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketQueuingDetected() {
      Log.d(LOG_TAG, "Packet queuing detected.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariable(String name, int value) {
      Log.d(VARS_TAG, String.format(
          "Setting variable, %1$s, to value, %2$d.", name, value));
      variables.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariable(String name, double value) {
      Log.d(VARS_TAG, String.format(
          "Setting variable, %1$s, to value, %2$f.", name, value));
      variables.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariable(String name, Object value) {
      Log.d(VARS_TAG, String.format(
          "Setting variable, %1$s, to value, %2$s.", name,
          (null == value) ? "null" : value.toString()));
      variables.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(String status) {
      Log.d(LOG_TAG, String.format("Updating status: %1$s.", status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatusPanel(String status) {
      Log.d(LOG_TAG, String
          .format("Updating status panel: %1$s.", status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wantToStop() {
      return wantToStop;
    }

    /**
     * Allows the containing service instance to request the blackbox from
     * the core NDT Java code stop testing.
     */
    void requestStop() {
      wantToStop = true;
    }

    @Override
    public String getClientApp() {
      return "mobile_android";
    }
  }

  
}
