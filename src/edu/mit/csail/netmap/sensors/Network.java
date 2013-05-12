package edu.mit.csail.netmap.sensors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import net.measurementlab.ndt.MLabNS;
import net.measurementlab.ndt.NdtTests;
import net.measurementlab.ndt.UiServices;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class Network {
  // log tag so logcat can be filtered to just the variable
  // written out by the core NDT Java code, useful for understanding
  // what is available for display in the test results
  private static final String VARS_TAG = "variables";
  
  static final String LOG_TAG = "NDT";
  
  /** Application context. */
  private static Context context_;

  /** Provides snformation about the currently used network connection. */
  private static ConnectivityManager connectivityManager_;
  
  /** Collects network performance information from the NDT library. */
  private static NdtListener ndtListener;
    
  /** True when collecting network performance measurements. */
  private static boolean measuring = false;
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    context_ = context;
    connectivityManager_ = (ConnectivityManager)context_.getSystemService(
        Context.CONNECTIVITY_SERVICE);
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
		NetworkInfo networkInfo = connectivityManager_.getActiveNetworkInfo();
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
	  buffer.append("{\"provider\":");
	  NetworkInfo networkInfo = connectivityManager_.getActiveNetworkInfo();
	  if (networkInfo == null) {
	    buffer.append("null");
	  } else {
	    buffer.append("\"");
	    // TODO(yuhan): expand this to all the types
	    switch (networkInfo.getType()) {
	    case ConnectivityManager.TYPE_ETHERNET:
	      buffer.append("ethernet");
	      break;
      case ConnectivityManager.TYPE_MOBILE:
        buffer.append("mobile");
        break;
      case ConnectivityManager.TYPE_WIFI:
        buffer.append("wifi");
        break;
	    }
	    buffer.append("\"");
	  }
	  
	  for (Entry<String, String> entry : ndtListener.results.entrySet()) {
	    buffer.append(",\"");
	    buffer.append(entry.getKey());
	    buffer.append("\":");
	    buffer.append(JSONObject.quote(entry.getValue()));
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
        results.put(keyValue[0].trim(), keyValue[1].trim());
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