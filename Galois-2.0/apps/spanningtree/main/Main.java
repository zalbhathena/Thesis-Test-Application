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

package spanningtree.main;

import galois.objects.Bag;
import galois.objects.BagBuilder;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.Graphs;
import galois.objects.graph.MorphGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.wl.Priority;

import java.util.Arrays;
import java.util.Collection;

public class Main extends AbstractMain {

  @Override
  protected Graph<NodeData> newGraphInstance() {
    return new MorphGraph.VoidGraphBuilder().directed(false).create();
  }

  @Override
  public Collection<Edge> runLoop() throws Exception {
    final Bag<Edge> spanningTree = new BagBuilder<Edge>().create();
    GNode<NodeData> startNode = Graphs.getRandom(graph);
    startNode.getData().setInSpanningTree(true);
    GaloisRuntime.foreach(Arrays.asList(startNode), new ForeeachBody() {
      @Override
      public void call(final GNode<NodeData> src, final ForeachContext<GNode<NodeData>> worklist) {
        src.map(new ActivityBody() {
          @Override
          public void call(final GNode<NodeData> dst) {
            // assume no self-edges
            NodeData dstData = dst.getData(MethodFlag.NONE);
            if (!dstData.inSpanningTree()) {
              Edge edge = new Edge(src, dst);
              spanningTree.add(edge, MethodFlag.NONE);
              dstData.setInSpanningTree(true);
              worklist.add(dst, MethodFlag.NONE);
            }
          }
        }, MethodFlag.CHECK_CONFLICT);
      }
    }, Priority.defaultOrder());
    return spanningTree;
  }

  @Override
  protected String getVersion() {
    return GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois";
  }

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }
}
