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

package des.unordered.circuitlib;

import galois.objects.graph.GNode;
import des.main.circuitlib.LogicEvent;
import des.unordered.Event;
import des.unordered.SimObject;

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
    super(2, 1); // two inputs, one output
    this.outputName = outputName;
    this.input1Name = input1Name;
    this.input2Name = input2Name;
    super.setDelay(delay);
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

  /* (non-Javadoc)
   * @see des.unordered.SimObject#execEvent(galois.objects.graph.GNode, des.unordered.Event)
   */
  @Override
  protected void execEvent(GNode<SimObject> myNode, Event<?> event) {

    Event<LogicEvent> e = (Event<LogicEvent>) event;

    if (e.getType() == Event.Type.NULL) {
      // send out null messages

      sendEventsToFanout(myNode, e, Event.Type.NULL, null);
    } else {
      // update the inputs of fanout gates
      LogicEvent le = (LogicEvent) e.getMessage();
      if (input1Name.equals(le.getNetName())) {
        input1Val = le.getNetVal();
      } else if (input2Name.equals(le.getNetName())) {
        input2Val = le.getNetVal();
      } else {
        netNameMismatch(le);
      }

      // output has been changed
      // update output immediately
      // generate events to send to all fanout gates to update their inputs afer delay
      char newOutput = evalOutput();
      this.outputVal = newOutput;

      LogicEvent drvFanout = new LogicEvent(outputName, newOutput);

      sendEventsToFanout(myNode, e, Event.Type.REGULAR, drvFanout);

    }

  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#copy(des.unordered.SimObject)
   */
  @Override
  protected void deepCopy(SimObject sobj) {
    super.deepCopy(sobj);

    TwoInputGate that = (TwoInputGate) sobj;

    this.input1Name = that.input1Name;
    this.input2Name = that.input2Name;
    this.outputName = that.outputName;
    this.input1Val = that.input1Val;
    this.input2Val = that.input2Val;
    this.outputVal = that.outputVal;
  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#hasInputName(java.lang.String)
   */
  @Override
  public boolean hasInputName(String net) {
    return (input1Name.equals(net) || input2Name.equals(net));
  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#hasOutputName(java.lang.String)
   */
  @Override
  public boolean hasOutputName(String net) {
    return outputName.equals(net);
  }

  // for debugging
  /* (non-Javadoc)
   * @see des.unordered.SimObject#toString()
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
   * @see des.unordered.circuitlib.LogicGate#getOutputName()
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

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#getInputIndex(java.lang.String)
   */
  @Override
  public int getInputIndex(String net) {
    if (this.input2Name.equals(net)) {
      return 1;
    } else if (this.input1Name.equals(net)) {
      return 0;
    } else {
      return -1; // error 
    }
  }
}
