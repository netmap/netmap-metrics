package edu.mit.csail.netmap;

/** Return value of {@link NetMap#powerSource()}. */
public enum PowerSource {
  /** Using battery power. Expensive actions should be avoided. */
  BATTERY,
  /** Connected to a power source. Expensive action should be done now. */
  CHARGER
}