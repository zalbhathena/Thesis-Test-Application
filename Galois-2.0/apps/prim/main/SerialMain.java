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

package prim.main;

import galois.objects.graph.GNode;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import util.fn.Lambda2Void;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class SerialMain extends AbstractMain {

  SerialMain() {
  }

  SerialMain(ObjectGraph<MstNode, Integer> graph) {
    this.graph = graph;
    reset();
  }

  @Override
  protected ObjectGraph<MstNode, Integer> newGraphInstance() {
    return new MorphGraph.ObjectGraphBuilder().serial(true).create();
  }

  @Override
  public Set<MstEdge> runLoop(GNode<MstNode> root) {
    final PriorityQueue<MstEdge> queue = new PriorityQueue<MstEdge>(graph.size(), EDGE_COMPARATOR);
    final Lambda2Void<GNode<MstNode>, GNode<MstNode>> closure = new Lambda2Void<GNode<MstNode>, GNode<MstNode>>() {
      @Override
      public void call(GNode<MstNode> dst, GNode<MstNode> src) {
        MstNode mstNode = dst.getData();
        if (!mstNode.inMst()) {
          Integer data = graph.getEdgeData(src, dst);
          MstEdge mstEdge = new MstEdge(src, dst, data);
          queue.add(mstEdge);
        }
      }
    };
    root.getData().setInMst(true);
    root.map(closure, root);
    // holds all the "edges" in the MST
    Set<MstEdge> mst = new HashSet<MstEdge>();
    while (!queue.isEmpty()) {
      MstEdge edge = queue.poll();
      MstNode pointA = edge.getSrc().getData();
      MstNode pointB = edge.getDst().getData();
      boolean AInMST = pointA.inMst();
      boolean BInMST = pointB.inMst();
      if (AInMST && BInMST) {
        continue;
      }
      if (!AInMST && !BInMST) {
        throw new RuntimeException("At least one node must be in the growing MST");
      }
      MstNode newMSTNode = AInMST ? pointB : pointA;
      newMSTNode.setInMst(true);
      mst.add(edge);
      GNode<MstNode> t = AInMST ? edge.getDst() : edge.getSrc();
      t.map(closure, t);
    }
    return mst;
  }

  @Override
  public void verify(Set<MstEdge> mstEdges, GNode<MstNode> startingNode) {
    System.out.println(getVersion() + " MST weight calculated = " + calcMstWeight(mstEdges));
    System.out.println("Not cross checking serial yet");
  }

  @Override
  protected String getVersion() {
    return "handwritten serial";
  }

  public static void main(String[] args) throws Exception {
    new SerialMain().run(args);
  }
}
