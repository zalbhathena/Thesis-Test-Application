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

File: EdgeData.java 

 */
package surveypropagation.main;

import galois.objects.AbstractNoConflictBaseObject;

import java.util.Random;

/**
 * The Class EdgeData.
 * Represent information stored on the edge of the FactorGraph.
 * This includes the type of the literal (+1 indicating a simple literal,
 * -1 indicating a complemented literal) and eeta, the weight on that edge. 
 * 
 */
public class EdgeData extends AbstractNoConflictBaseObject{

  /** The random number generator. */
  private static Random rand = util.Launcher.getLauncher().getRandom(3);

  /** The literal type: +1 indicates a positive and -1 a negative literal */
  public int litType;

  /** The weight eeta on the edge. */
  public double eeta;

  /**
   * Instantiates a new edge data.
   * Eeta is initialized to a random double,
   * Edge type is set to complemented.
   */
  public EdgeData() {
    litType = -1;
    eeta = rand.nextDouble();
  }

@Override
public Object gclone()
{
	EdgeData e = new EdgeData();
	e.litType = litType;
	e.eeta = eeta;
	return e;
}

@Override
public void restoreFrom(Object copy)
{
	EdgeData e = (EdgeData)copy;
	litType = e.litType;
	eeta = e.eeta;	
}
}
