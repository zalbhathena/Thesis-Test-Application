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

File: XOR2.java 

 */

package des.main.circuitlib;

import des.main.SimObject;

// TODO: Auto-generated Javadoc
/**
 * The Class XOR2.
 */
public class XOR2 extends TwoInputGate {

  /**
   * Instantiates a new XOR2.
   *
   * @param outputName the output name
   * @param input1Name the input1 name
   * @param input2Name the input2 name
   * @param delay the delay
   */
  public XOR2(String outputName, String input1Name, String input2Name, long delay) {
    super(outputName, input1Name, input2Name, delay);
  }

  /**
   * Instantiates a new XOR2.
   */
  public XOR2() {
    super();
  }

  /**
   * @see des.main.SimObject#newInstance();
   */
  @Override
  public SimObject newInstance() {
    return new XOR2();
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#evalOutput()
   */
  public char evalOutput() {
    return LogicFunctions.xor(input1Val, input2Val);
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.TwoInputGate#toString()
   */
  public String toString() {
    return "XOR2 " + super.toString();
  }
}
