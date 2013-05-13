package edu.mit.csail.netmap;

/** Return value of {@link NetMap#networkState()}. */
public enum NetworkSource {
  /** Data comes from a cellular network, such as EDGE, 3G, HSPA, EVDO. */
  CELLULAR,
  /** Data comes from a WiFi access point. */
  WIFI
}
