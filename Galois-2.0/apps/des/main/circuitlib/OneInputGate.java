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

File: OneInputGate.java 

 */

package des.main.circuitlib;

import galois.objects.graph.GNode;

import java.util.ArrayList;
import java.util.List;

import des.main.Event;
import des.main.SimObject;

/**
 * The Class OneInputGate.
 */
public abstract class OneInputGate extends LogicGate {

  /** The input name. */
  protected String inputName;

  /** The output name. */
  protected String outputName;

  /** The input val. */
  protected char inputVal;

  /** The output val. */
  protected char outputVal;

  /**
   * Instantiates a new one input gate.
   *
   * @param outputName the output name
   * @param inputName the input name
   * @param delay the delay
   */
  public OneInputGate(String outputName, String inputName, long delay) {
    super();
    this.outputName = outputName;
    this.inputName = inputName;
    super.setDelay(delay);
    inputVal = '0';
    outputVal = '0';
  }

  /**
   * Instantiates a new one input gate.
   */
  public OneInputGate() {
    this("", "", 0);
  }

  // override the SimObject methods
  /*
   * execEvent follows the same logic as TwoInputGate.execEvent()
   *
   */

  /* (non-Javadoc)
   * @see des.main.SimObject#execEvent(galois.objects.graph.GNode, des.main.Event)
   */
  @Override
  public List<Event<?>> execEvent(GNode<SimObject> myNode, Event<?> event) {

    Event<LogicEvent> inputEvent = (Event<LogicEvent>) event;
    LogicEvent le = (LogicEvent) inputEvent.getMessage();
    List<Event<?>> genEvents = new ArrayList<Event<?>>();
    if (outputName.equals(le.getNetName())) {
      // update the output
      outputVal = le.getNetVal();
    } else {
      if (inputName.equals(le.getNetName())) {
        inputVal = le.getNetVal();
      } else {
        super.netNameMismatch(le);
      }
      // output has been changed
      // generate an event to update the output after delay
      // generate events to send to all fanout gates to update their inputs afer delay
      char newOutput = evalOutput();
      // to reduce the number of events we can skip creating an event to update output
      // and instead update output immediately
      // another optimization would be when output does not change, don't do anything
      this.outputVal = newOutput;
      // LogicEvent updOut = new LogicEvent(outputName, newOutput);
      // // inputEvent.recvObj is myself
      // genEvents.add(makeEvent(inputEvent.getRecvObj(), inputEvent.getRecvObj(), updOut, inputEvent.getRecvTime(), this.delay));

      // create events to send to fanout nodes
      LogicEvent drvFanout = new LogicEvent(outputName, newOutput);
      super.makeEventsForFanout(myNode, inputEvent, genEvents, drvFanout);
    }
    return genEvents;
  }

  /* (non-Javadoc)
   * @see des.main.SimObject#copy(des.main.SimObject)
   */
  @Override
  protected void deepCopy(SimObject sobj) {
    super.deepCopy(sobj);
    OneInputGate that = (OneInputGate) sobj;
    this.inputName = that.inputName;
    this.outputName = that.outputName;
    this.delay = that.delay;
    this.inputVal = that.inputVal;
    this.outputVal = that.outputVal;
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#hasInputName(java.lang.String)
   */
  @Override
  public boolean hasInputName(String net) {
    return (inputName.equals(net));
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#hasOutputName(java.lang.String)
   */
  @Override
  public boolean hasOutputName(String net) {
    return outputName.equals(net);
  }

  // for debugging
  /* (non-Javadoc)
   * @see des.main.SimObject#toString()
   */
  @Override
  public String toString() {
    String s = super.toString(); // TODO: may be commented once code is sound
    s = s + " delay = " + delay + " output " + outputName + " = " + outputVal + " input " + inputName + " = "
        + inputVal + "\n";
    return s;
  }

  /**
   * Gets the input name.
   *
   * @return the input name
   */
  public String getInputName() {
    return inputName;
  }

  /**
   * Sets the input name.
   *
   * @param inputName the new input name
   */
  public void setInputName(String inputName) {
    this.inputName = inputName;
  }

  /**
   * Gets the input val.
   *
   * @return the input val
   */
  public char getInputVal() {
    return inputVal;
  }

  /**
   * Sets the input val.
   *
   * @param inputVal the new input val
   */
  public void setInputVal(char inputVal) {
    this.inputVal = inputVal;
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#getOutputName()
   */
  @Override
  public String getOutputName() {
    return outputName;
  }

  /**
   * Sets the output name.
   *
   * @param outputName the new output name
   */
  public void setOutputName(String outputName) {
    this.outputName = outputName;
  }

  /**
   * Gets the output val.
   *
   * @return the output val
   */
  public char getOutputVal() {
    return outputVal;
  }

  /**
   * Sets the output val.
   *
   * @param outputVal the new output val
   */
  public void setOutputVal(char outputVal) {
    this.outputVal = outputVal;
  }

}
