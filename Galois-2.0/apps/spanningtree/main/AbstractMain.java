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

File: AbstractMain.java 

 */

package spanningtree.main;

import galois.objects.graph.*;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import util.Launcher;
import util.fn.Lambda;
import util.fn.Lambda2Void;
import util.fn.LambdaVoid;

import java.io.IOException;
import java.util.Collection;

public abstract class AbstractMain {

  Graph<NodeData> graph;

  public void run(String[] args) throws Exception {
    final Launcher launcher = Launcher.getLauncher();
    if (launcher.isFirstRun()) {
      System.err.println();
      System.err.println();
      System.err.printf("application: Spanning Tree (%s version)\n", getVersion());
      System.err.println("Finds a valid spanning tree for a given graph");
      System.err.println();
      System.err.println("number of threads: " + GaloisRuntime.getRuntime().getMaxThreads());
    }
    System.err.println();
    initialize(args);
    launcher.startTiming();
    Collection<Edge> mstEdges = runLoop();
    launcher.stopTiming();
    if (launcher.isFirstRun()) {
      verify(mstEdges);
    }
  }

  public void initialize(String[] args) throws IOException {
    graph = newGraphInstance();
    // TODO: read a void graph instead of playing around with the adapters
    VoidGraphToObjectGraphAdapter<NodeData, Integer> tmp = new VoidGraphToObjectGraphAdapter<NodeData, Integer>(graph);
    GraphGenerator.readIntegerEdgeGraph(args, tmp, new Lambda<Integer, NodeData>() {
      @Override
      public NodeData call(Integer id) {
        return new NodeData(String.valueOf(id));
      }
    });
    graph = new LocalComputationGraph.VoidGraphBuilder().from(graph).create();
  }

  protected abstract Graph<NodeData> newGraphInstance();

  public abstract Collection<Edge> runLoop() throws Exception;

  public void verify(Collection<Edge> mstEdges) {
    System.err.print("verifying...");
    // every node must be in the spanning tree
    graph.map(new LambdaVoid<GNode<NodeData>>() {
      @Override
      public void call(GNode<NodeData> node) {
        NodeData n = node.getData();
        if (!n.inSpanningTree()) {
          throw new IllegalStateException("not all the nodes are in the spanning tree.");
        }
      }
    });
    int resultSize = mstEdges.size();
    int expectedSize = graph.size() - 1;
    if (resultSize != expectedSize) {
      throw new IllegalStateException("Wrong number of edges in the mst. Expected: " + expectedSize + ", found: "
          + resultSize);
    }
    System.err.println("okay.");
    System.err.println();
  }

  protected abstract String getVersion();

  // make the code easier to read
  abstract class Worklist implements ForeachContext<GNode<NodeData>> {
  }

  abstract class ForeeachBody implements Lambda2Void<GNode<NodeData>, ForeachContext<GNode<NodeData>>> {
  }

  abstract class ActivityBody implements LambdaVoid<GNode<NodeData>> {
  }
}
