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

package des.main.circuitlib;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;

import java.util.List;

import util.fn.Lambda2Void;
import des.main.Event;
import des.main.SimObject;

/**
 * The Class LogicGate represents an abstract logic gate.
 */
public abstract class LogicGate extends SimObject {

  /** The delay. */
  protected long delay = SimObject.MIN_DELAY;

  /**
   * Evaluate the  output.
   *
   * @return the new output
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
   * Sets the delay.
   *
   * @param delay the new delay
   */
  public void setDelay(long delay) {
    if (delay <= 0) {
      delay = SimObject.MIN_DELAY;
    }
    this.delay = delay;
  }

  /**
   * Gets the delay.
   *
   * @return the delay
   */
  public long getDelay() {
    return delay;
  }

  @Override
  protected void deepCopy(SimObject sobj) {
    super.deepCopy(sobj);
    LogicGate that = (LogicGate) sobj;
    this.delay = that.delay;
  }

  /**
   * Net name mismatch.
   *
   * @param le the logic event
   */
  protected void netNameMismatch(LogicEvent le) {
    throw new IllegalStateException("Received logic event : " + le + " with mismatching net name, this = " + this);
  }

  /**
   * Make events for fanout gates.
   *
   * @param myNode the my node
   * @param inputEvent the input event
   * @param newEvents the new events
   * @param newMsg the new msg
   */
  protected void makeEventsForFanout(final GNode<SimObject> myNode, final Event<LogicEvent> inputEvent,
      final List<Event<?>> newEvents, final LogicEvent newMsg) {

    assert myNode == inputEvent.getRecvObj(); // that's me
    final long sendTime = inputEvent.getRecvTime();

    Lambda2Void<GNode<SimObject>, GNode<SimObject>> fanoutClosure = new Lambda2Void<GNode<SimObject>, GNode<SimObject>>() {

      @Override
      public void call(GNode<SimObject> dst, GNode<SimObject> src) {
        Event<LogicEvent> ne = makeEvent(src, dst, newMsg, sendTime, getDelay());
        newEvents.add(ne);
      }
    };

    myNode.map(fanoutClosure, myNode, MethodFlag.NONE); // since we have a lock on myNode and we're just reading GNodes from neighbors
  }

}
