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

File: Prims.java 

 */

package boruvka.main;

import boruvka.uf.BinaryHeap;
import galois.objects.graph.GNode;
import util.fn.Lambda2Void;
import util.fn.LambdaVoid;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Prims extends Main {

  static int INFINITY = Integer.MAX_VALUE;

  @Override
  public long run(String[] args) throws Exception {

    final Map<GNode<Node>, Integer> ordering = new HashMap<GNode<Node>, Integer>();
    Comparator<GNode<Node>> comparator = new PrimsComparator<Node>(ordering);
    final BinaryHeap<GNode<Node>> heap = new BinaryHeap<GNode<Node>>(graph.size(), comparator);
    final AtomicBoolean first = new AtomicBoolean(true);

    graph.map(new LambdaVoid<GNode<Node>>() {

      @Override
      public void call(GNode<Node> src) {
        if (first.get()) {
          ordering.put(src, 0);
          first.set(false);
        } else {
          ordering.put(src, INFINITY);
        }
        heap.add(src);
      }
    });

    // Straight out of CLRS
    final Map<GNode<Node>, GNode<Node>> parent = new HashMap<GNode<Node>, GNode<Node>>();
    final Lambda2Void<GNode<Node>, GNode<Node>> body = new Lambda2Void<GNode<Node>, GNode<Node>>() {
      @Override
      public void call(GNode<Node> dst, GNode<Node> src) {
        int w = graph.getEdgeData(src, dst);

        if (heap.contains(dst) && w < ordering.get(dst)) {
          parent.put(dst, src);

          // Update weight
          ordering.put(dst, w);
          heap.decreaseKey(dst);
        }
      }
    };

    while (!heap.isEmpty()) {
      GNode<Node> src = heap.poll();

      // Add outgoing edges to component
      src.map(body, src);
    }

    if (parent.size() != graph.size() - 1) {
      throw new Error("Gold standard did not generate a tree: " + parent.size() + " != " + (graph.size() - 1));
    }

    // Weigh tree
    long weight = 0;
    for (GNode<Node> key : parent.keySet()) {
      final Integer data = graph.getEdgeData(key, parent.get(key));
      weight += data;
    }
    return weight;
  }

  @Override
  protected void printMessage(String msg) {
  }

}
