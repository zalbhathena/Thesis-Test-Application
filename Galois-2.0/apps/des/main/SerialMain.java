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

package des.main;

import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.MorphGraph;

import java.util.List;
import java.util.PriorityQueue;

import des.main.circuitlib.LogicEvent;

/**
 * The Class SerialMain.
 */
public class SerialMain extends AbstractMain {

  /* (non-Javadoc)
   * @see des.main.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return "handwritten serial";
  }

  /* (non-Javadoc)
   * @see des.main.AbstractMain#newGraphInstance()
   */
  @Override
  protected Graph<SimObject> newGraphInstance() {
    return new MorphGraph.VoidGraphBuilder().directed(true).serial(true).create();
  }

  /* (non-Javadoc)
   * @see des.main.AbstractMain#runLoop()
   */
  @Override
  protected void runLoop() {

    PriorityQueue<Event<?>> worklist = new PriorityQueue<Event<?>>(1024, new EventRcvTimeTieBrkCmp());
    worklist.addAll(simInit.getInitEvents());

    int numEvents = 0;

    // main loop
    while (!worklist.isEmpty()) {
      Event<LogicEvent> currEvent = (Event<LogicEvent>) worklist.poll();

      GNode<SimObject> n = currEvent.getRecvObj();
      SimObject so = n.getData();

      List<Event<?>> genEvents = so.execEvent(n, currEvent);

      if (genEvents != null && !genEvents.isEmpty()) {
        worklist.addAll(genEvents);
      }

      ++numEvents;
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
