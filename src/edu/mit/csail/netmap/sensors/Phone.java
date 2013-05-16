package edu.mit.csail.netmap.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public final class Phone {
  /** Entry point to Android's GSM functionality. */
  private static TelephonyManager telephonyManager_;

  /** Provides snformation about the currently used network connection. */
  private static ConnectivityManager connectivityManager_;

  /** Called by {@link Sensors#initialize(android.content.Context)}. */
  public static void initialize(Context context) {
    telephonyManager_ = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    connectivityManager_ = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    context.registerReceiver(new connectivityReceiver(), new IntentFilter(
        ConnectivityManager.CONNECTIVITY_ACTION));
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
    buffer.append("\",\"line1Number\":\"");
    buffer.append(telephonyManager_.getLine1Number());
    buffer.append("\",\"networkOperator\":\"");
    buffer.append(telephonyManager_.getNetworkOperator());
    buffer.append("\",\"networkOperatorName\":\"");
    buffer.append(telephonyManager_.getNetworkOperatorName());
    buffer.append("\",\"simOperator\":\"");
    buffer.append(telephonyManager_.getSimOperator());
    buffer.append("\",\"simOperatorName\":\"");
    buffer.append(telephonyManager_.getSimOperatorName());
    buffer.append("\",\"softwareVersion\":\"");
    buffer.append(telephonyManager_.getDeviceSoftwareVersion());
    buffer.append("\",\"networkCountryISO\":\"");
    buffer.append(telephonyManager_.getNetworkCountryIso());
    buffer.append("\",\"simCountryIso\":\"");
    buffer.append(telephonyManager_.getSimCountryIso());
    buffer.append("\",\"simSerialNumber\":\"");
    buffer.append(telephonyManager_.getSimSerialNumber());
    buffer.append("\",\"isRoaming\":");
    buffer.append(telephonyManager_.isNetworkRoaming());
    buffer.append(",\"callState\":");
    buffer.append(telephonyManager_.getCallState());
    buffer.append(",\"subscriberId\":\"");
    buffer.append(telephonyManager_.getSubscriberId());

    buffer.append("\",\"phoneType\":\"");
    switch (telephonyManager_.getPhoneType()) {
    case TelephonyManager.PHONE_TYPE_CDMA:
      buffer.append("cdma");
      break;
    case TelephonyManager.PHONE_TYPE_GSM:
      buffer.append("gsm");
      break;
    case TelephonyManager.PHONE_TYPE_NONE:
      buffer.append("none");
      break;
    case TelephonyManager.PHONE_TYPE_SIP:
      buffer.append("sip");
      break;
    default:
      buffer.append("unknown");
      break;
    }

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
    default:
      buffer.append("unknwon");
      break;
    }

    buffer.append("\",\"dataActivity\":\"");
    switch (telephonyManager_.getDataActivity()) {
    case TelephonyManager.DATA_ACTIVITY_DORMANT:
      buffer.append("dormant");
      break;
    case TelephonyManager.DATA_ACTIVITY_IN:
      buffer.append("in");
      break;
    case TelephonyManager.DATA_ACTIVITY_INOUT:
      buffer.append("inout");
      break;
    case TelephonyManager.DATA_ACTIVITY_NONE:
      buffer.append("none");
      break;
    case TelephonyManager.DATA_ACTIVITY_OUT:
      buffer.append("out");
      break;
    default:
      buffer.append("unknown");
      break;
    }
    buffer.append("\",\"dataState\":\"");
    switch (telephonyManager_.getDataState()) {
    case TelephonyManager.DATA_CONNECTED:
      buffer.append("connected");
      break;
    case TelephonyManager.DATA_CONNECTING:
      buffer.append("connecting");
      break;
    case TelephonyManager.DATA_DISCONNECTED:
      buffer.append("disconnected");
      break;
    case TelephonyManager.DATA_SUSPENDED:
      buffer.append("suspended");
      break;
    default:
      buffer.append("unknown");
      break;
    }
    buffer.append("\",\"simState\":\"");
    switch (telephonyManager_.getSimState()) {
    case TelephonyManager.SIM_STATE_ABSENT:
      buffer.append("absent");
      break;
    case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
      buffer.append("networkLocked");
      break;
    case TelephonyManager.SIM_STATE_PIN_REQUIRED:
      buffer.append("pinRequired");
      break;
    case TelephonyManager.SIM_STATE_PUK_REQUIRED:
      buffer.append("pukRequired");
      break;
    case TelephonyManager.SIM_STATE_READY:
      buffer.append("ready");
      break;
    case TelephonyManager.SIM_STATE_UNKNOWN:
      buffer.append("unknown");
      break;
    default:
      buffer.append("unknown");
      break;
    }

    buffer.append("\"");
    buffer.append("}");
  }

  private static class connectivityReceiver extends BroadcastReceiver {
    public void onReceive(Context c, Intent intent) {

      NetworkInfo networkInfo = connectivityManager_.getNetworkInfo(intent
          .getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1));
      if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
          if (networkInfo.isConnected()) {
            // WiFi us connected
          } else {
            // WiFi is disconnected
          }
      } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
        if (networkInfo.isConnected()) {
          // Cellular network connected
        } else {
          // Cellular network disconnected
        }
      }
    }
  }
}
