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

File: INV.java 

 */

package des.main.circuitlib;

import des.main.SimObject;

/**
 * The Class INV.
 */
public class INV extends OneInputGate {

  /**
   * Instantiates a new iNV.
   *
   * @param outputName the output name
   * @param inputName the input name
   * @param delay the delay
   */
  public INV(String outputName, String inputName, long delay) {
    super(outputName, inputName, delay);
  }

  /**
   * Instantiates a new iNV.
   */
  public INV() {
    super();
  }

  /**
   * @see des.main.SimObject#newInstance();
   */
  @Override
  public SimObject newInstance() {
    return new INV();
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.LogicGate#evalOutput()
   */
  public char evalOutput() {
    return LogicFunctions.not(inputVal);
  }

  /* (non-Javadoc)
   * @see des.main.circuitlib.OneInputGate#toString()
   */
  public String toString() {
    return "INV " + super.toString();
  }
}
