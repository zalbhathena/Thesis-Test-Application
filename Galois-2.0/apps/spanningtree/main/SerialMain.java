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

package spanningtree.main;

import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.Graphs;
import galois.objects.graph.MorphGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class SerialMain extends AbstractMain {

  @Override
  protected Graph<NodeData> newGraphInstance() {
    return new MorphGraph.VoidGraphBuilder().directed(false).serial(true).create();
  }

  @Override
  // assume no self-edges
  public Collection<Edge> runLoop() {
    final List<Edge> spanningTree = new ArrayList<Edge>(graph.size() - 1);
    final Stack<GNode<NodeData>> worklist = new Stack<GNode<NodeData>>();
    final GNode<NodeData> startNode = Graphs.getRandom(graph);
    startNode.getData().setInSpanningTree(true);
    worklist.add(startNode);
    while (!worklist.isEmpty()) {
      final GNode<NodeData> src = worklist.pop();
      src.map(new ActivityBody() {
        @Override
        public void call(final GNode<NodeData> dst) {
          NodeData dstData = dst.getData();
          if (!dstData.inSpanningTree()) {
            Edge edge = new Edge(src, dst);
            spanningTree.add(edge);
            dstData.setInSpanningTree(true);
            worklist.add(dst);
          }
        }
      });
    }
    return spanningTree;
  }

  @Override
  protected String getVersion() {
    return "handwritten serial";
  }

  public static void main(String[] args) throws Exception {
    new SerialMain().run(args);
  }
}
