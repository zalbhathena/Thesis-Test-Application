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

File: SerialMain.java 

 */

package des.unordered;

import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.MorphGraph;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import util.fn.Lambda2Void;

/**
 * The Class SerialMain.
 */
public class SerialMain extends AbstractMain {

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return "handwritten serial";
  }

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#newGraphInstance()
   */
  @Override
  protected Graph<SimObject> newGraphInstance() {
    return new MorphGraph.VoidGraphBuilder().directed(true).serial(true).create();
  }

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#runLoop()
   */
  @Override
  protected void runLoop() {

    Queue<GNode<SimObject>> worklist = new LinkedList<GNode<SimObject>>();
    worklist.addAll(simInit.getInputNodes());

    // closure to loop over fanout objects
    Lambda2Void<GNode<SimObject>, Queue<GNode<SimObject>>> addNeighClosure = new Lambda2Void<GNode<SimObject>, Queue<GNode<SimObject>>>() {

      @Override
      public void call(GNode<SimObject> neigh, Queue<GNode<SimObject>> worklist) {
        SimObject sobj = neigh.getData();
        sobj.updateActive();

        if (sobj.isActive()) {
          AtomicBoolean onwl = neigh.getData().onwlFlag();
          if (onwl.compareAndSet(false, true)) {
            worklist.add(neigh);
          }
        }
      }
    };

    int numEvents = 0;
    while (!worklist.isEmpty()) {

      GNode<SimObject> activeNode = worklist.poll();
      SimObject currObj = activeNode.getData();
      AtomicBoolean currOnwl = currObj.onwlFlag();
      currOnwl.set(false);

      numEvents += currObj.simulate(activeNode);

      activeNode.map(addNeighClosure, worklist);

      currObj.updateActive();
      if (currObj.isActive()) {
        if (currOnwl.compareAndSet(false, true)) {
          worklist.add(activeNode);
        }
      }

    }

    System.out.println("Simulation ended");
    System.out.println("Number of events processed = " + numEvents);
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    new SerialMain().run(args);
  }

}
