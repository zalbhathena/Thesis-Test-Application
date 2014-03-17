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

package kruskal.main;

import galois.objects.graph.GNode;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.wl.Ordered;
import galois.runtime.wl.Priority;
import galois.runtime.wl.Priority.Rule;

import java.io.IOException;
import java.util.Set;

import util.SystemProperties;
import util.fn.Lambda2Void;

/**
 * The Class Main.
 */
public class Main extends AbstractMain {

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois";
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#newGraphInstance()
   */
  @Override
  protected ObjectGraph<KNode, Integer> newGraphInstance() {
    return new MorphGraph.ObjectGraphBuilder().create();
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#runLoop()
   */
  @Override
  public void runLoop() throws Exception {

    Set<KEdge> initial = this.edges;

    Rule rules = Priority.first(Ordered.class, new EdgeCmp());

    GaloisRuntime.foreachOrdered(initial, new Lambda2Void<KEdge, ForeachContext<KEdge>>() {

      @Override
      public void call(final KEdge e, final ForeachContext<KEdge> ctx) {

        GNode<KNode> rep1 = UFHelper.find(e.getFirst());
        GNode<KNode> rep2 = UFHelper.find(e.getSecond());

        assert rep1 == e.getFirst().getData().getRep() && rep2 == e.getSecond().getData().getRep();

        if (rep1 != rep2) {
          UFHelper.union(rep1, rep2);
          e.setInMST();
        }
      }
    }, rules);
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#verify()
   */
  @Override
  public void verify() {
    Boolean fullVerify = SystemProperties.getBooleanProperty("verify.full", false);
    long expected = SystemProperties.getLongProperty("verify.result", Long.MIN_VALUE);
    if (fullVerify || expected == Long.MIN_VALUE) {
      System.err.println("verifying against serial");
      SerialMain serial = new SerialMain();
      serial.graph = this.graph; // have to use the same input graph because weights are randomly generated;
      serial.initUF();
      serial.runLoop();
      serial.calcMSTWeight();
      expected = serial.mstWeight;
    }
    long result = mstWeight;
    if (result == expected) {
      System.out.println("MST ok.");
    } else {
      throw new IllegalStateException("Inconsistent MSTs: " + expected + " != " + result);
    }
  }

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args) throws IOException {
    new Main().run(args);
  }

}
