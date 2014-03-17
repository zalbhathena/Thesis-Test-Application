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

File: LogicGate.java 

 */

package des.unordered.circuitlib;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import util.fn.Lambda2Void;
import des.main.circuitlib.LogicEvent;
import des.unordered.Event;
import des.unordered.SimObject;
import des.unordered.Event.Type;

/**
 * The Class LogicGate represents an abstract logic gate.
 */
public abstract class LogicGate extends SimObject {

  /** The delay. */
  protected long delay = 0;

  /** an instance of closure per object, which is used to create and send events to fanout 
    since a thread holds a lock on the logicgate, one instance per gate should be safe
   */
  protected SendEventsClosure sendEventsClosure = new SendEventsClosure();

  /**
   * Instantiates a new logic gate.
   *
   * @param numInputs the num inputs
   * @param numOutputs the num outputs
   */
  public LogicGate(int numInputs, int numOutputs) {
    super(numInputs, numOutputs);
  }

  /**
   * Gets the delay.
   *
   * @return the delay
   */
  public long getDelay() {
    return delay;
  }

  /**
   * Sets the delay.
   *
   * @param delay the new delay
   */
  public void setDelay(long delay) {
    this.delay = delay;
    if (this.delay <= 0) {
      this.delay = SimObject.MIN_DELAY;
    }
  }

  /* (non-Javadoc)
   * @see des.unordered.SimObject#copy(des.unordered.SimObject)
   */
  @Override
  protected void deepCopy(SimObject sobj) {
    super.deepCopy(sobj);
    LogicGate that = (LogicGate) sobj;

    this.delay = that.delay;
    this.sendEventsClosure = new SendEventsClosure();
  }

  /**
   * Eval output.
   *
   * @return the char
   */
  public abstract char evalOutput();

  /**
   * Checks for input name.
   *
   * @param net the net
   * @return true, if successful
   */
  public abstract boolean hasInputName(String net);

  /**
   * Checks for output name.
   *
   * @param net the net
   * @return true, if successful
   */
  public abstract boolean hasOutputName(String net);

  /**
   * Gets the output name.
   *
   * @return the output name
   */
  public abstract String getOutputName();

  /**
   * Gets the input index.
   *
   * @param net name of the net an input of this gate is connected to
   * @return index of that input
   */
  public abstract int getInputIndex(String net);

  /**
   * Net name mismatch.
   *
   * @param le the le
   */
  protected void netNameMismatch(LogicEvent le) {
    throw new IllegalStateException("Received logic event : " + le + " with mismatching net name, this = " + this);
  }

  /**
   * Send events to fanout.
   *
   * @param myNode the my node
   * @param inputEvent the input event
   * @param type the type
   * @param msg the msg
   */
  protected void sendEventsToFanout(GNode<SimObject> myNode, Event<LogicEvent> inputEvent, Event.Type type,
      LogicEvent msg) {

    long sendTime = inputEvent.getRecvTime();
    assert myNode == inputEvent.getRecvObj();

    // always set the fields first 
    sendEventsClosure.setFields(type, msg, sendTime);

    myNode.map(sendEventsClosure, myNode, MethodFlag.NONE); // should already hold lock on out neighbors
  }

  /**
   * The Class SendEventsClosure.
   */
  protected static class SendEventsClosure implements Lambda2Void<GNode<SimObject>, GNode<SimObject>> {

    /** The type. */
    private Type type = null;

    /** The msg. */
    private LogicEvent msg = null;

    /** The send time. */
    private long sendTime = -1;

    /**
     * Sets the fields.
     *
     * @param type the type
     * @param msg the msg
     * @param sendTime the send time
     */
    public void setFields(Event.Type type, LogicEvent msg, long sendTime) {
      this.type = type;
      this.msg = msg;
      this.sendTime = sendTime;
    }

    /* (non-Javadoc)
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @Override
    public void call(GNode<SimObject> dst, GNode<SimObject> src) {
      LogicGate srcGate = (LogicGate) src.getData(MethodFlag.NONE); // should already hold a lock on this

      Event<LogicEvent> ne = srcGate.makeEvent(src, dst, type, msg, sendTime, srcGate.getDelay());

      LogicGate dstGate = (LogicGate) dst.getData(MethodFlag.NONE); // should already hold lock on this

      String outNet = srcGate.getOutputName();
      int dstIn = dstGate.getInputIndex(outNet); // get the input index of the net to which my output is connected

      dstGate.recvEvent(dstIn, ne);

    }

  }

}
