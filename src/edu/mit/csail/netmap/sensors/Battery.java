package edu.mit.csail.netmap.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;


public final class Battery {

	  private static Intent batteryStatus;
	  
	  public static void initialize(Context context) {
		  IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		  batteryStatus = context.registerReceiver(new PowerConnectionReceiver() , ifilter);
		  
	  }

	  /**
	   * Writes a JSON representation of the battery info to the given buffer.
	   * 
	   * @param buffer a {@link StringBuffer} that receives a JSON representation of
	   *     the WiFisensor data
	   */
	  public static void getJson(StringBuffer buffer) {
		  buffer.append("{\"isCharing\":\"");
		  buffer.append(isCharging());
		  buffer.append("\",\"batteryPct\":\"");
		  buffer.append(getBetteryPct());
		  buffer.append("\"");
		  buffer.append("}");  
	  }
	  
	  /** Returns the charging status of the device. */
	  public static boolean isCharging(){
		  // Are we charging / charged?
		  int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		  boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                       status == BatteryManager.BATTERY_STATUS_FULL;
		  
		  // How are we charging?
		  //int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		  //boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		  //boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		  return isCharging;
	   }
	  
	  /** Returns the battery status (%) */
	  public static float getBetteryPct(){
		  
		  int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		  int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		  float batteryPct = level / (float)scale;
		  return batteryPct;
	  }
	   
	  /** Monitors the charging status of the device */
	  public static class PowerConnectionReceiver extends BroadcastReceiver {
		    @Override
		    public void onReceive(Context context, Intent intent) { 
		        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                            status == BatteryManager.BATTERY_STATUS_FULL;
		    
		        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		    }
		}
}