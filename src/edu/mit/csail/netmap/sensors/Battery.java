package edu.mit.csail.netmap.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;


public final class Battery {

	  private static Intent batteryStatus;
	  
	  public static void initialize(Context context) {
		  IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		  batteryStatus = context.registerReceiver(null , ifilter);
		  
	  }

	  /**
	   * Writes a JSON representation of the battery info to the given buffer.
	   * 
	   * @param buffer a {@link StringBuffer} that receives a JSON representation of
	   *     the WiFisensor data
	   */
	  public static void getJson(StringBuffer buffer) {
		  buffer.append("{\"status\":\"");
		  switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)){
		  	case BatteryManager.BATTERY_STATUS_CHARGING:  	
		  		buffer.append("charging");
		      break;
		  	case BatteryManager.BATTERY_STATUS_DISCHARGING:  	
		  		buffer.append("discharging");
			  break;
		  	case BatteryManager.BATTERY_STATUS_FULL:  	
		  		buffer.append("full");
			  break;
		  	case BatteryManager.BATTERY_STATUS_NOT_CHARGING:  	
		  		buffer.append("not_charging");
			  break;
		  	case BatteryManager.BATTERY_STATUS_UNKNOWN:  	
		  		buffer.append("unknown");
			  break;  
		  }
		  
		  buffer.append("\",\"plugged\":\"");
		  switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)){
		  	case BatteryManager.BATTERY_PLUGGED_AC:
		  		buffer.append("ac");
		  		break;
		  	case BatteryManager.BATTERY_PLUGGED_USB:
		  		buffer.append("usb");
		  		break;
		  	case BatteryManager.BATTERY_PLUGGED_WIRELESS:
		  		buffer.append("wireless");
		  		break;
		  }
		 
		  buffer.append("\",\"charge\":{\"level\":");
		  buffer.append(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
		  buffer.append(",\"scale\":");
		  buffer.append(batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1));
		 
		  buffer.append("},\"health\":\"");
		  
		  switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)){
		 	case BatteryManager.BATTERY_HEALTH_COLD:
		 		buffer.append("cold");
				break;
		 	case BatteryManager.BATTERY_HEALTH_DEAD:
		 		buffer.append("dead");
				break;
		 	case BatteryManager.BATTERY_HEALTH_GOOD:
		 		buffer.append("good");
				break;	
		 	case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
		 		buffer.append("over_voltage");
				break;
		 	case BatteryManager.BATTERY_HEALTH_OVERHEAT:
		 		buffer.append("over_heat");
				break;	
		 	case BatteryManager.BATTERY_HEALTH_UNKNOWN:
		 		buffer.append("unknown");
				break;
		 	case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
		 		buffer.append("unknwon_failure");
				break;
		 }
		  buffer.append("\"");
		  
		  
		  buffer.append("}");  
	  }
 
	
}