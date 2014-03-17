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

File: TwoInputGate.java 

 */

package des.main.circuitlib;

import galois.objects.graph.GNode;

import java.util.ArrayList;
import java.util.List;

import des.main.Event;
import des.main.SimObject;

/**
 * The Class TwoInputGate.
 */
public abstract class TwoInputGate extends LogicGate {

  /** The input1 name. */
  protected String input1Name;

  /** The input2 name. */
  protected String input2Name;

  /** The output name. */
  protected String outputName;

  /** The input1 val. */
  protected char input1Val;

  /** The input2 val. */
  protected char input2Val;

  /** The output val. */
  protected char outputVal;

  /**
   * Instantiates a new two input gate.
   *
   * @param outputName the output name
   * @param input1Name the input1 name
   * @param input2Name the input2 name
   * @param delay the delay
   */
  public TwoInputGate(String outputName, String input1Name, String input2Name, long delay) {
    super();
    this.outputName = outputName;
    this.input1Name = input1Name;
    this.input2Name = input2Name;
    this.setDelay(delay);
    input1Val = '0';
    input2Val = '0';
    outputVal = '0';
  }

  /**
   * Instantiates a new two input gate.
   */
  public TwoInputGate() {
    this("", "", "", SimObject.MIN_DELAY);
  }

  // overriding the SimObject Methods
  /*
   * Two ways to execute the event
   *
   * 1. when input is updated, schedule an event to self to update the output
   * with new output value, delay = gate delay, also schedule an event to fanout objects with new output
   * with delay = gate delay.
   * output will be updated at the same time as the inputs of fanout gates.
   *
   * 2. when input is updated, schedule an event to update the output with delay = gate delay
   * when the output is updated, schedule an event for fanout objects with delay = 0
   *
   * 1 is better than 2
   *
   */

  /* (non-Javadoc)
   * @see des.main.SimObject#execEvent(galois.objects.graph.GNode, des.main.Event)
   */
  @Override
  public List<Event<?>> execEvent(GNode<SimObject> myNode, Event<?> event) {
    Event<LogicEvent> inputEvent = (Event<LogicEvent>) event;
    LogicEvent le = inputEvent.getMessage();
    List<Event<?>> genEvents = new ArrayList<Event<?>>();
    if (outputName.equals(le.getNetName())) {
      outputVal = le.getNetVal();
    } else {
      if (input1Name.equals(le.getNetName())) {
        input1Val = le.getNetVal();
      } else if (input2Name.equals(le.getNetName())) {
        input2Val = le.getNetVal();
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
    TwoInputGate that = (TwoInputGate) sobj;
    this.input1Name = that.input1Name;
    this.input2Name = that.input2Name;
    this.outputName = that.outputName;
    this.delay = that.delay;
    this.input1Val = that.input1Val;
    this.input2Val = that.input2Val;
    this.outputVal = that.outputVal;
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#hasInputName(java.lang.String)
   */
  @Override
  public boolean hasInputName(String net) {
    return (input1Name.equals(net) || input2Name.equals(net));
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
    s = s + " delay = " + delay + " output " + outputName + " = " + outputVal + " input1 " + input1Name + " = "
        + input1Val + " input2 " + input2Name + " = " + input2Val + "\n";
    return s;
  }

  /**
   * Gets the input1 name.
   *
   * @return the input1 name
   */
  public String getInput1Name() {
    return input1Name;
  }

  /**
   * Sets the input1 name.
   *
   * @param input1Name the new input1 name
   */
  public void setInput1Name(String input1Name) {
    this.input1Name = input1Name;
  }

  /**
   * Gets the input1 val.
   *
   * @return the input1 val
   */
  public char getInput1Val() {
    return input1Val;
  }

  /**
   * Sets the input1 val.
   *
   * @param input1Val the new input1 val
   */
  public void setInput1Val(char input1Val) {
    this.input1Val = input1Val;
  }

  /**
   * Gets the input2 name.
   *
   * @return the input2 name
   */
  public String getInput2Name() {
    return input2Name;
  }

  /**
   * Sets the input2 name.
   *
   * @param input2Name the new input2 name
   */
  public void setInput2Name(String input2Name) {
    this.input2Name = input2Name;
  }

  /**
   * Gets the input2 val.
   *
   * @return the input2 val
   */
  public char getInput2Val() {
    return input2Val;
  }

  /**
   * Sets the input2 val.
   *
   * @param input2Val the new input2 val
   */
  public void setInput2Val(char input2Val) {
    this.input2Val = input2Val;
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
