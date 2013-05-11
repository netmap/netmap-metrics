package edu.mit.csail.netmap.sensors;

import java.util.List;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public final class GSM {
  /** Entry point to Android's GSM functionality. */
  private static TelephonyManager telephonyManager;
  
  private static StateListener stateListener;  
  
  private static int connectedTowerRSSI = 0;
  private static int connectedTowerBER = 0;
  
  private static List<NeighboringCellInfo> neighboringCells;
  private static GsmCellLocation gsmCell;
  
  /** True when the GSM is enabled by the user. */
  private static boolean enabled = false;
  
  /** True when the GSM is powered up and reporting information. - not being used now*/
  private static boolean started = false;
  
  /** True when listening for GSM updates. */
  private static boolean listening = false;
  
  
  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    telephonyManager = 
      (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    stateListener = new StateListener();
  }
  
  /**
   * Starts listening for location updates.
   * 
   * This should be called when your application / activity becomes active.
   */
  public static void start() {
    if (listening) return;
    telephonyManager.listen(stateListener,
        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
        PhoneStateListener.LISTEN_CELL_LOCATION);
    neighboringCells = telephonyManager.getNeighboringCellInfo();
    gsmCell = (GsmCellLocation)telephonyManager.getCellLocation(); 
    
    enabled = isEnabled();
    listening = true;
  }

  /** 
   * Stops listening for location updates.
   * 
   * This should be called when your application / activity is no longer active.
   */
  public static void stop() {
    if (!listening) return;
    telephonyManager.listen(stateListener, PhoneStateListener.LISTEN_NONE);
    listening = false;
  }
  
  /**
   * Checks if the user's preferences allow the use of GSM.
   * 
   * @return true if the user lets us use GSM
   */
  public static boolean isEnabled() {
    return true;
  }
  
  /**
   * Writes a JSON representation of the GPS data to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *     the GPS sensor data
   */
  public static void getJson(StringBuffer buffer) {
    buffer.append("{\"enabled\":");
    if (enabled) { buffer.append("true"); } else { buffer.append("false"); }
    /**if (started) {
      buffer.append(",\"started\": true");
    }**/    
    
    if (gsmCell != null) {
      buffer.append(",\"type\":");
      buffer.append(telephonyManager.getNetworkType());
      buffer.append(",\"cid\":");
      buffer.append(gsmCell.getCid());
      buffer.append(",\"lac\":");
      buffer.append(gsmCell.getLac());
      buffer.append(",\"rssi\":");
      buffer.append(connectedTowerRSSI);
      buffer.append(",\"ber\":");
      buffer.append(connectedTowerBER);
    }
    
    
    if (neighboringCells != null) {
      buffer.append(",\"numOfCells\":");
      buffer.append(neighboringCells.size());
      buffer.append(",\"cells\":[");
      boolean firstElement = true;
      for (int i = 0; i < neighboringCells.size(); i++) {
        if (firstElement) {
          firstElement = false;
          buffer.append("{\"type\":");
        } else {
          buffer.append(",{\"type\":");
        }
        buffer.append(neighboringCells.get(i).getNetworkType());
        buffer.append(",\"cid\":");
        buffer.append(neighboringCells.get(i).getCid()&0xFFFF);
        buffer.append(",\"lac\":");
        buffer.append(neighboringCells.get(i).getLac());
        buffer.append(",\"rssi\":");
        buffer.append(neighboringCells.get(i).getRssi());
        buffer.append(",\"psc\":");
        buffer.append(neighboringCells.get(i).getPsc());
        buffer.append("}");
        }
        buffer.append("]");
      }
     
    buffer.append("}");
    
  }
  
  
  private static class StateListener extends PhoneStateListener {
    public void onSignalStrengthsChanged(SignalStrength ss) {
      connectedTowerRSSI = ss.getGsmSignalStrength();
      connectedTowerBER = ss.getGsmBitErrorRate();
    }
    
    public void onCellLocationChanged(CellLocation location) {
    }
  };
}
