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

File: Dijkstra.java 

*/



package sssp.main;

import galois.objects.graph.GNode;

import java.util.Comparator;
import java.util.PriorityQueue;

import util.fn.Lambda2Void;

/**
 * Implementation of Dijkstra's shortest path algorithm.
 * 
 *
 */
public class Dijkstra extends Main {

  @Override
  protected void runBody(GNode<Node> source) {
    final PriorityQueue<GrayNode> worklist = new PriorityQueue<GrayNode>(64, new GrayNodeComparator());

    source.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
      @Override
      public void call(GNode<Node> dst, GNode<Node> src) {
        GrayNode n = new GrayNode(dst, src.getData().dist + (int) graph.getEdgeData(src, dst));
        worklist.add(n);
      }
    }, source);

    while (!worklist.isEmpty()) {
      final GrayNode gn = worklist.poll();
      GNode<Node> src = gn.node;

      if (gn.weight < src.getData().dist) {
        src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
          @Override
          public void call(GNode<Node> dst, GNode<Node> src) {
            // hasn't been visited before
            if (dst.getData().dist == INFINITY) {
              int relaxed = gn.weight + (int) graph.getEdgeData(src, dst);
              GrayNode gm = new GrayNode(dst, relaxed);
              worklist.add(gm);
            }
          }
        }, src);

        // can relax the node label
        src.getData().dist = gn.weight;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    new Dijkstra().run(args);
  }

  @Override
  protected String getVersion() {
    return "dijkstra serial";
  }

  private static class GrayNode {
    public GNode<Node> node;
    public int weight;

    public GrayNode(GNode<Node> node, int weight) {
      this.node = node;
      this.weight = weight;
    }
  }

  public static class GrayNodeComparator implements Comparator<GrayNode> {
    @Override
    public int compare(GrayNode lhs, GrayNode rhs) {
      return lhs.weight - rhs.weight;
    }
  }
}
