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

File: INPUT.java 

 */

package des.unordered.circuitlib;

import galois.objects.graph.GNode;
import des.main.circuitlib.LogicEvent;
import des.unordered.Event;
import des.unordered.SimObject;

// may as well be inherited from OneInputGate
/**
 * The Class INPUT.
 */
public class INPUT extends OneInputGate {

  /**
   * Instantiates a new INPUT.
   *
   * @param outputName the output name
   * @param inputName the INPUT name
   */
  public INPUT(String outputName, String inputName) {
    super(outputName, inputName, SimObject.MIN_DELAY);
  }

  /**
   * Instantiates a new INPUT.
   */
  public INPUT() {
    super();
  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.OneInputGate#execEvent(galois.objects.graph.GNode, des.unordered.Event)
   */
  @Override
  protected void execEvent(GNode<SimObject> myNode, Event<?> event) {

    if (event.getType() == Event.Type.NULL) {
      // send out null messages

      super.execEvent(myNode, event); // same functionality as OneInputGate

    } else {
      Event<LogicEvent> e = (Event<LogicEvent>) event;
      // update the inputs of fanout gates

      LogicEvent le = (LogicEvent) e.getMessage();
      if (outputName.equals(le.getNetName())) {
        inputVal = le.getNetVal();

        this.outputVal = inputVal;

        LogicEvent drvFanout = new LogicEvent(outputName, outputVal);

        sendEventsToFanout(myNode, e, Event.Type.REGULAR, drvFanout);
      } else {
        netNameMismatch(le);
      }

    }

  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#evalOutput()
   */
  @Override
  public char evalOutput() {
    return inputVal;
  }

  // for debugging
  /* (non-Javadoc)
   * @see des.unordered.circuitlib.OneInputGate#toString()
   */
  @Override
  public String toString() {
    return "INPUT: " + super.toString();
  }

  /**
   * @see des.unordered.SimObject#newInstance()
   */
  @Override
  public SimObject newInstance() {
    return new INPUT();
  }

}
