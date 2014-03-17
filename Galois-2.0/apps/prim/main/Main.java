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

package prim.main;

import galois.objects.GSetBuilder;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.WorkNotUsefulException;
import galois.runtime.wl.Ordered;
import galois.runtime.wl.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import util.SystemProperties;
import util.fn.Lambda2Void;
import util.fn.Lambda3Void;

public class Main extends AbstractMain {

  @Override
  protected ObjectGraph<MstNode, Integer> newGraphInstance() {
    return new MorphGraph.ObjectGraphBuilder().create();
  }

  @Override
  public Set<MstEdge> runLoop(GNode<MstNode> startNode) throws Exception {
    startNode.getData().setInMst(true);
    final List<MstEdge> init = new ArrayList<MstEdge>();
    startNode.map(new Lambda2Void<GNode<MstNode>, GNode<MstNode>>() {
      @Override
      public void call(GNode<MstNode> dst, GNode<MstNode> src) {
        int weight = graph.getEdgeData(src, dst);
        init.add(new MstEdge(src, dst, weight));
      }
    }, startNode);

    final Set<MstEdge> mst = new GSetBuilder<MstEdge>().create();

    final Lambda3Void<GNode<MstNode>, GNode<MstNode>, ForeachContext<MstEdge>> closure = new Lambda3Void<GNode<MstNode>, GNode<MstNode>, ForeachContext<MstEdge>>() {
      @Override
      public void call(GNode<MstNode> dst, GNode<MstNode> src, ForeachContext<MstEdge> ctx) {
        MstNode mstNode = dst.getData();
        if (!mstNode.inMst()) {
          int data = graph.getEdgeData(src, dst, MethodFlag.NONE);
          MstEdge edge = new MstEdge(src, dst, data);
          ctx.add(edge);
        }
      }
    };
    GaloisRuntime.foreachOrdered(init, new Lambda2Void<MstEdge, ForeachContext<MstEdge>>() {
      @Override
      public void call(final MstEdge mstEdge, final ForeachContext<MstEdge> ctx) {
        MstNode src = mstEdge.getSrc().getData();
        MstNode dst = mstEdge.getDst().getData();
        boolean srcInMst = src.inMst();
        boolean dstInMst = dst.inMst();
        GNode<MstNode> gNode = null;
        MstNode mstNode = null;
        if (srcInMst) {
          if (dstInMst) {
            WorkNotUsefulException.throwException();
          }
          gNode = mstEdge.getDst();
          mstNode = dst;
        } else {
          if (!dstInMst) {
            throw new RuntimeException("At least one node must be in the growing MST");
          }
          gNode = mstEdge.getSrc();
          mstNode = src;
        }
        mstNode.setInMst(true);
        mst.add(mstEdge);
        gNode.map(closure, gNode, ctx);
      }
    }, Priority.first(Ordered.class, EDGE_COMPARATOR));
    return mst;
  }

  @Override
  public void verify(Set<MstEdge> mstEdges, GNode<MstNode> startNode) {
    Boolean fullVerify = SystemProperties.getBooleanProperty("verify.full", false);
    long expected = SystemProperties.getLongProperty("verify.result", Long.MIN_VALUE);
    if (fullVerify || expected == Long.MIN_VALUE) {
      System.out.print("verifying using serial version... ");
      SerialMain serial = new SerialMain(graph);
      Set<MstEdge> serialMstEdges = serial.runLoop(startNode);
      expected = serial.calcMstWeight(serialMstEdges);
    }
    long result = calcMstWeight(mstEdges);
    if (result != expected) {
      throw new IllegalStateException("Inconsistent MSTs: " + expected + " != " + result);
    }
    System.err.println("okay.");
  }

  @Override
  protected String getVersion() {
    return GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois";
  }

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }
}
