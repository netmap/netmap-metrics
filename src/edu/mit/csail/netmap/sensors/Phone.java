package edu.mit.csail.netmap.sensors;

import android.content.Context;
import android.telephony.TelephonyManager;

public final class Phone {
  /** Entry point to Android's GSM functionality. */
  private static TelephonyManager telephonyManager_;

  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    telephonyManager_ = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);

  }

  /**
   * Writes a JSON representation of the phone info to the given buffer.
   * 
   * @param buffer a {@link StringBuffer} that receives a JSON representation of
   *          the WiFisensor data
   */
  public static void getJson(StringBuffer buffer) {
    buffer.append("{\"phoneId\":\"");
    buffer.append(telephonyManager_.getDeviceId());
    buffer.append("\",\"radioType\":\"");
    switch (telephonyManager_.getNetworkType()) {
    case TelephonyManager.NETWORK_TYPE_1xRTT:
      buffer.append("1xRTT");
      break;
    case TelephonyManager.NETWORK_TYPE_CDMA:
      buffer.append("cdma");
      break;
    case TelephonyManager.NETWORK_TYPE_EDGE:
      buffer.append("edge");
      break;
    case TelephonyManager.NETWORK_TYPE_EHRPD:
      buffer.append("ehrpd");
      break;
    case TelephonyManager.NETWORK_TYPE_EVDO_0:
      buffer.append("evdo_0");
      break;
    case TelephonyManager.NETWORK_TYPE_EVDO_A:
      buffer.append("evdo_a");
      break;
    case TelephonyManager.NETWORK_TYPE_EVDO_B:
      buffer.append("evdo_b");
      break;
    case TelephonyManager.NETWORK_TYPE_GPRS:
      buffer.append("gprs");
      break;
    case TelephonyManager.NETWORK_TYPE_HSDPA:
      buffer.append("hsdpa");
      break;
    case TelephonyManager.NETWORK_TYPE_HSPA:
      buffer.append("hspa");
      break;
    case TelephonyManager.NETWORK_TYPE_HSPAP:
      buffer.append("hspap");
      break;
    case TelephonyManager.NETWORK_TYPE_HSUPA:
      buffer.append("hsupa");
      break;
    case TelephonyManager.NETWORK_TYPE_IDEN:
      buffer.append("iden");
      break;
    case TelephonyManager.NETWORK_TYPE_LTE:
      buffer.append("lte");
      break;
    case TelephonyManager.NETWORK_TYPE_UMTS:
      buffer.append("umts");
      break;
    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
      buffer.append("unknown");
      break;
    }
    buffer.append("\"");
    buffer.append("}");
  }

}
