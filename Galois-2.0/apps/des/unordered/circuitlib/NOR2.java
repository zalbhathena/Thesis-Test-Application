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

File: NOR2.java 

 */

package des.unordered.circuitlib;

import des.main.circuitlib.LogicFunctions;
import des.unordered.SimObject;

/**
 * The Class NOR2.
 */
public class NOR2 extends TwoInputGate {

  /**
   * Instantiates a new NOR2.
   *
   * @param outputName the output name
   * @param input1Name the input1 name
   * @param input2Name the input2 name
   * @param delay the delay
   */
  public NOR2(String outputName, String input1Name, String input2Name, long delay) {
    super(outputName, input1Name, input2Name, delay);
  }

  /**
   * Instantiates a new NOR2.
   */
  public NOR2() {
    super();
  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.LogicGate#evalOutput()
   */
  @Override
  public char evalOutput() {
    return LogicFunctions.not(LogicFunctions.or(input1Val, input2Val));
  }

  /* (non-Javadoc)
   * @see des.unordered.circuitlib.TwoInputGate#toString()
   */
  @Override
  public String toString() {
    return "NOR2 " + super.toString();
  }

  /**
   * @see des.unordered.SimObject#newInstance()
   */
  @Override
  public SimObject newInstance() {
    return new NOR2();
  }

}
