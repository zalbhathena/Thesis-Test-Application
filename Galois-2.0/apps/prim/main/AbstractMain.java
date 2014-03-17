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

package prim.main;

import galois.objects.graph.GNode;
import galois.objects.graph.GraphGenerator;
import galois.objects.graph.Graphs;
import galois.objects.graph.LocalComputationGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.GaloisRuntime;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

import util.Launcher;
import util.SystemProperties;
import util.fn.Lambda;
import util.fn.LambdaVoid;

public abstract class AbstractMain {

  ObjectGraph<MstNode, Integer> graph;
  static final Comparator<MstEdge> EDGE_COMPARATOR = new Comparator<MstEdge>() {
    @Override
    public int compare(MstEdge e1, MstEdge e2) {
      return e1.getData().compareTo(e2.getData());
    }
  };

  public void run(String[] args) throws Exception {
    final Launcher launcher = Launcher.getLauncher();
    if (launcher.isFirstRun()) {
      System.err.println();
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.printf("application: Prim's MST (%s version)\n", getVersion());
      System.err.println("Finds the Minimal Spanning Tree of a graph with integer weights");
      System.err.println("http://iss.ices.utexas.edu/lonestar/prim.html");
      System.err.println();
      System.err.println("number of threads: " + GaloisRuntime.getRuntime().getMaxThreads());
    }
    System.err.println();
    // initialize graph
    GNode<MstNode> startNode = initialize(args);
    launcher.startTiming();
    Set<MstEdge> mstEdges = runLoop(startNode);
    launcher.stopTiming();
    long totalWeight = calcMstWeight(mstEdges);
    if (launcher.isFirstRun()) {
      System.out.println("MST weight: " + totalWeight);
      verify(mstEdges, startNode);
    }
  }

  public GNode<MstNode> initialize(String[] args) throws IOException {
    graph = newGraphInstance();
    GraphGenerator.readIntegerEdgeGraph(args, graph, new Lambda<Integer, MstNode>() {
      @Override
      public MstNode call(Integer id) {
        return new MstNode(null);
      }
    });
    // use local-computation type of graph?
    boolean useArrayGraph = SystemProperties.getBooleanProperty("useArrayGraph", true);
    if (useArrayGraph) {
      graph = new LocalComputationGraph.ObjectGraphBuilder().from(graph).create();
    }
    return Graphs.getRandom(graph);
  }

  public long calcMstWeight(Set<MstEdge> mstEdges) {
    long total = 0;
    for (MstEdge e : mstEdges) {
      total += e.getData();
    }
    return total;
  }

  public void reset() {
    graph.map(new LambdaVoid<GNode<MstNode>>() {
      @Override
      public void call(GNode<MstNode> node) {
        MstNode n = node.getData();
        n.setInMst(false);
      }
    });
  }

  protected abstract ObjectGraph<MstNode, Integer> newGraphInstance();

  public abstract Set<MstEdge> runLoop(GNode<MstNode> root) throws Exception;

  public abstract void verify(Set<MstEdge> mstEdges, GNode<MstNode> startNode);

  protected abstract String getVersion();
}
