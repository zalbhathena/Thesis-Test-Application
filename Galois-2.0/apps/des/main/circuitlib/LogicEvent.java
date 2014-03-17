/*
Galois, a framework to exploit amorphous data-parallelism in irregular
programs.

Copyright (C) 2010, The University of Texas at Austin. All rights reserved.
UNIVERSITY EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES CONCERNING THIS SOFTWARE
AND DOCUMENTATION, INCLUDING ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR ANY
PARTICULAR PURPOSE, NON-INFRINGEMENT AND WARRANTIES OF PERFORMANCE, AND ANY
WARRANTY THAT MIGHT OTHERWISE ARISE FROM COURSE OF DEALING OR USAGE OF TRADE.
NO WARRANTY IS EITHER EXPRESS OR IMPLIED WITH RESPECT TO THE USE OF THE
SOFTWARE OR DOCUMENTATION. Under no circumstances shall University be liable
for incidental, special, indirect, direct or consequential damages or loss of
profits, interruption of business, or related expenses which may arise from use
of Software or Documentation, including but not limited to those resulting from
defects in Software and/or Documentation, or loss or inaccuracy of data of any
kind.

File: LogicEvent.java 

*/





package des.main.circuitlib;


/**
 * The Class LogicEvent is the msg carried by events. represents a change in the value of a net.
 */
public class LogicEvent {
  
  /** The net name. */
  String netName;
  
  /** The net val. */
  char netVal;

  /**
   * Instantiates a new logic event.
   *
   * @param netName the net name
   * @param netVal the net val
   */
  public LogicEvent(String netName, char netVal) {
    super();
    this.netName = netName;
    this.netVal = netVal;
  }

  /**
   * Instantiates a new logic event.
   */
  public LogicEvent() {
    this(null, 'U');
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return " netName = " + netName + " netVal = " + netVal;
  }

  /**
   * Gets the net name.
   *
   * @return the net name
   */
  public String getNetName() {
    return netName;
  }

  /**
   * Sets the net name.
   *
   * @param netName the new net name
   */
  public void setNetName(String netName) {
    this.netName = netName;
  }

  /**
   * Gets the net val.
   *
   * @return the net val
   */
  public char getNetVal() {
    return netVal;
  }

  /**
   * Sets the net val.
   *
   * @param netVal the new net val
   */
  public void setNetVal(char netVal) {
    this.netVal = netVal;
  }
}
