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

File: Main.java 

 */

package des.unordered;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.MorphGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.wl.FIFO;
import galois.runtime.wl.Priority;
import galois.runtime.wl.Priority.Rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import util.fn.Lambda2Void;
import util.fn.LambdaVoid;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public class Main extends AbstractMain {

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois";
  }

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#newGraphInstance()
   */
  @Override
  protected Graph<SimObject> newGraphInstance() {
    return new MorphGraph.VoidGraphBuilder().directed(true).create();
  }

  /* (non-Javadoc)
   * @see des.unordered.AbstractMain#runLoop()
   */
  @Override
  protected void runLoop() throws Exception {

    List<GNode<SimObject>> initialActive = simInit.getInputNodes();

    final LambdaVoid<GNode<SimObject>> oneShot = new LambdaVoid<GNode<SimObject>>() {

      @Override
      public void call(GNode<SimObject> neigh) {
        neigh.getData(MethodFlag.CHECK_CONFLICT);
      }
    };

    final Lambda2Void<GNode<SimObject>, ForeachContext<GNode<SimObject>>> fanoutClosure = new Lambda2Void<GNode<SimObject>, ForeachContext<GNode<SimObject>>>() {

      @Override
      public void call(GNode<SimObject> neigh, ForeachContext<GNode<SimObject>> ctx) {
        SimObject sobj = neigh.getData(MethodFlag.NONE);
        sobj.updateActive();

        if (sobj.isActive()) {
          AtomicBoolean onwl = sobj.onwlFlag();
          if (onwl.compareAndSet(false, true)) {
            ctx.add(neigh);
          }
        }
      }
    };

    Rule rules = Priority.first(FIFO.class);

    final AtomicInteger numEvents = new AtomicInteger(0);
    final AtomicInteger numIter = new AtomicInteger(0);

    GaloisRuntime.foreach(initialActive, new Lambda2Void<GNode<SimObject>, ForeachContext<GNode<SimObject>>>() {

      @Override
      public void call(GNode<SimObject> activeNode, ForeachContext<GNode<SimObject>> ctx) {
        SimObject currObj = activeNode.getData(MethodFlag.CHECK_CONFLICT);
        AtomicBoolean currOnwl = currObj.onwlFlag();
        currOnwl.set(false);

        // acquire locks on neighbor hood: one shot
        activeNode.map(oneShot, MethodFlag.CHECK_CONFLICT);

        // should be pass the fail-safe point by now

        int proc = currObj.simulate(activeNode); // number of events processed
        numEvents.addAndGet(proc);

        activeNode.map(fanoutClosure, ctx, MethodFlag.NONE);

        currObj.updateActive();
        if (currObj.isActive()) {
          if (currOnwl.compareAndSet(false, true)) {
            ctx.add(activeNode);
          }
        }

        numIter.incrementAndGet();
      }
    }, rules);

    System.err.println("Number of events processed = " + numEvents);
    System.err.println("Number of iterations performed = " + numIter);
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    new Main().run(args);
  }

}
