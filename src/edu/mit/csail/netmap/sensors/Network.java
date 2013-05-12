package edu.mit.csail.netmap.sensors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.measurementlab.ndt.MLabNS;
import net.measurementlab.ndt.NdtTests;
import net.measurementlab.ndt.UiServices;

import org.json.JSONObject;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
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
  
  /** Application context. */
  private static Context context_;

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

  /** Collects network performance information from the NDT library. */
  private static NdtListener ndtListener;
    
  /** True when collecting network performance measurements. */
  private static boolean measuring = false;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    context_ = context;
    ndtListener = new NdtListener();
  }
  
  /**
   * Starts listening for location updates.
   * 
   * This should be called when your application / activity becomes active.
   */
  public static void measure() {
    if (measuring) return;
    measuring = true;
    
    String serverHost = MLabNS.Lookup(context_, "ndt", "ipv4", null);
    
    try {
      Thread measureThread = new Thread(new NdtTests(serverHost, ndtListener,
    		  getNetworkType()));
      measureThread.start();
      measureThread.join();
    } catch (Throwable tr) {
      Log.e(LOG_TAG, "Problem running tests.", tr);
    }
    
    measuring = false;
  }
  /**
   * Gets the type of network the device is currently using.
   */
  private static String getNetworkType() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context_.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (null == networkInfo) {
			return NdtTests.NETWORK_UNKNOWN;
		}
		switch (networkInfo.getType()) {
		case ConnectivityManager.TYPE_MOBILE:
			return NdtTests.NETWORK_MOBILE;
		case ConnectivityManager.TYPE_WIFI:
			return NdtTests.NETWORK_WIFI;
		default:
			return NdtTests.NETWORK_UNKNOWN;
		}
	}
  /**
   * Writes a JSON representation of the WiFi data to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *     the WiFisensor data
   */
  public static void getJson(StringBuffer buffer) {
	  buffer.append("{\"enabled\":");
	  buffer.append(enabled ? "true" : "false");
	  
	  //avgrtt
	  buffer.append(",\"avgrtt\":");
	  if (ndtListener.results.containsKey("avgrtt")){
		  buffer.append(ndtListener.results.get("avgrtt"));
	  }else{
		  buffer.append("-1");
	  } 
	  //c2sspd
	  buffer.append(",\"c2sspd\":");
	  if (ndtListener.results.containsKey("c2sspd")){
		  buffer.append(ndtListener.results.get("c2sspd"));
	  }else{
		  buffer.append("-1");
	  }
	  
	  //congestion
	  buffer.append(",\"congestion\":");
	  if (ndtListener.results.containsKey("congestion")){
		  buffer.append(ndtListener.results.get("congestion"));
	  }else{
		  buffer.append("-1");
	  }
	  
	  //CurRTO
	  buffer.append(",\"CurRTO\":");
	  if (ndtListener.results.containsKey("CurRTO")){
		  buffer.append(ndtListener.results.get("CurRTO"));
	  }else{
		  buffer.append("-1");
	  }
	  
	  //CurRwinRcvd
	  buffer.append(",\"CurRwinRcvd\":");
	  if (ndtListener.results.containsKey("CurRwinRcvd")){
		  buffer.append(ndtListener.results.get("CurRwinRcvd"));
	  }else{
		  buffer.append("-1");
	  }
	  
	  //CurRwinRcvd
	  buffer.append(",\"CurRwinRcvd\":");
	  if (ndtListener.results.containsKey("CurRwinRcvd")){
		  buffer.append(ndtListener.results.get("CurRwinRcvd"));
	  }else{
		  buffer.append("-1");
	  }
	  //DupAcksOut
	  buffer.append(",\"DupAcksOut\":");
	  if (ndtListener.results.containsKey("DupAcksOut")){
		  buffer.append(ndtListener.results.get("DupAcksOut"));
	  }else{
		  buffer.append("-1");
	  }
	  //host
	  buffer.append(",\"host\":");
	  if (ndtListener.results.containsKey("host")){
		  buffer.append(ndtListener.results.get("host"));
	  }else{
		  buffer.append("-1");
	  }
	  //jitter
	  buffer.append(",\"jitter\":");
	  if (ndtListener.results.containsKey("jitter")){
		  buffer.append(ndtListener.results.get("jitter"));
	  }else{
		  buffer.append("-1");
	  }
	  //loss
	  buffer.append(",\"loss\":");
	  if (ndtListener.results.containsKey("loss")){
		  buffer.append(ndtListener.results.get("loss"));
	  }else{
		  buffer.append("-1");
	  }
	  //MaxRTT
	  buffer.append(",\"MaxRTT\":");
	  if (ndtListener.results.containsKey("MaxRTT")){
		  buffer.append(ndtListener.results.get("MaxRTT"));
	  }else{
		  buffer.append("-1");
	  }
	  //MaxRwinRcvd
	  buffer.append(",\"MaxRwinRcvd\":");
	  if (ndtListener.results.containsKey("MaxRwinRcvd")){
		  buffer.append(ndtListener.results.get("MaxRwinRcvd"));
	  }else{
		  buffer.append("-1");
	  }
	  //natStatus
	  buffer.append(",\"natStatus\":");
	  if (ndtListener.results.containsKey("natStatus")){
		  buffer.append(ndtListener.results.get("natStatus"));
	  }else{
		  buffer.append("-1");
	  }
	  //optimalRcvrBuffer
	  buffer.append(",\"optimalRcvrBuffer\":");
	  if (ndtListener.results.containsKey("optimalRcvrBuffer")){
		  buffer.append(ndtListener.results.get("optimalRcvrBuffer"));
	  }else{
		  buffer.append("-1");
	  }
	  //Ping
	  buffer.append(",\"Ping\":");
	  if (ndtListener.results.containsKey("Ping")){
		  buffer.append(ndtListener.results.get("Ping"));
	  }else{
		  buffer.append("-1");
	  }
	  //s2cspd
	  buffer.append(",\"s2cspd\":");
	  if (ndtListener.results.containsKey("s2cspd")){
		  buffer.append(ndtListener.results.get("s2cspd"));
	  }else{
		  buffer.append("-1");
	  }
	  //SACKsRcvd
	  buffer.append(",\"SACKsRcvd\":");
	  if (ndtListener.results.containsKey("SACKsRcvd")){
		  buffer.append(ndtListener.results.get("SACKsRcvd"));
	  }else{
		  buffer.append("-1");
	  }
	  //WaitSec
	  buffer.append(",\"WaitSec\":");
	  if (ndtListener.results.containsKey("WaitSec")){
		  buffer.append(ndtListener.results.get("WaitSec"));
	  }else{
		  buffer.append("-1");
	  }
	  buffer.append("}");
	  System.out.println(buffer);
  }
  
  /** Collects the performance results reported by the NDT library. */
  private static class NdtListener implements UiServices {
    /** If this becomes true, the NDT performance test will abort early. */
    private boolean wantToStop = false;

    /** Accmulates the performance results reported by the NDT library. */
    public Map<String, String> results;
    
    public NdtListener() {
      results = new HashMap<String, String>();
    }

    @Override
    public void appendString(String message, int viewId) {
      Log.d(LOG_TAG, String.format("Appended: (%1$d) %2$s.", viewId,
          message.trim()));

      if (viewId == UiServices.DIAG_VIEW) {
        String[] keyValue = message.split(":", 2);
        results.put(keyValue[0], keyValue[1]);
      }
    }

    @Override
    public void logError(String str) {
      Log.e(LOG_TAG, String.format("Error: %1$s.", str.trim()));
    }

    @Override
    public void onBeginTest() {
    }

    @Override
    public void onEndTest() {
      Log.d(LOG_TAG, "Test ended.");
      wantToStop = false;
    }

    @Override
    public void onFailure(String errorMessage) {
      Log.d(LOG_TAG, String.format("Failed: %1$s.", errorMessage));
      wantToStop = false;
    }

    @Override
    public void incrementProgress() {
    }

    @Override
    public void onLoginSent() {
    }

    @Override
    public void onPacketQueuingDetected() {
    }

    @Override
    public void setVariable(String name, int value) {
    }

    @Override
    public void setVariable(String name, double value) {
    }

    @Override
    public void setVariable(String name, Object value) {
    }

    @Override
    public void updateStatus(String status) {
      Log.d(LOG_TAG, String.format("Updating status: %1$s.", status));
    }

    @Override
    public void updateStatusPanel(String status) {
    }

    @Override
    public boolean wantToStop() {
      return wantToStop;
    }

    /**
     * If this returns true, the NDT library will stop the measurement.
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