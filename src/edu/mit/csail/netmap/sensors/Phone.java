package edu.mit.csail.netmap.sensors;


import android.content.Context;
import android.telephony.TelephonyManager;

public final class Phone {
	 /** Entry point to Android's GSM functionality. */
	  private static TelephonyManager telephonyManager;
	  
	  private static String phoneId;
	  
	  /** Called by {@link Sensors#initialize(android.content.Context)}. */
	  public static void initialize(Context context) {
	    telephonyManager = 
	      (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    
	     phoneId = telephonyManager.getDeviceId();
	  }

	  /** Gets the unique device ID **/
	  public static String getPhoneId(){
		  return phoneId;
	  }
}
