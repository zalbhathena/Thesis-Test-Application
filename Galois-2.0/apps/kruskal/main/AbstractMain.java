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

package kruskal.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.GraphGenerator;
import galois.objects.graph.LocalComputationGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.GaloisRuntime;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import util.Launcher;
import util.SystemProperties;
import util.fn.Lambda;
import util.fn.Lambda2Void;
import util.fn.LambdaVoid;

/**
 * The Class AbstractMain holds common functionality for {@link SerialMain} and {@link Main}.
 */
public abstract class AbstractMain {

  /** The useArrayGraphProperty, if provided the implementation uses a {@link LocalComputationGraph}. */
  public static final String useArrayGraphProperty = "useArrayGraph";

  /** The mst weight. */
  long mstWeight = 0;

  /** The number of edges in mst. */
  int numEdgesInMST = 0;

  /** The set of edges. */
  Set<KEdge> edges;

  /** The graph. */
  ObjectGraph<KNode, Integer> graph;

  /**
   * Gets the version (Galois or Serial).
   *
   * @return the version
   */
  protected abstract String getVersion();

  /**
   * New graph instance.
   *
   * @return a new instance of the graph
   */
  protected abstract ObjectGraph<KNode, Integer> newGraphInstance();

  /**
   * Run the main loop.
   *
   * @throws Exception the exception
   */
  public abstract void runLoop() throws Exception;

  /**
   * Verify the results.
   */
  public abstract void verify();

  /**
   * Calculate the  mst weight.
   *
   * @return the long
   */
  public long calcMSTWeight() {
    mstWeight = 0;
    numEdgesInMST = 0;
    for (KEdge e : edges) {
      if (e.isInMST()) {
        mstWeight += e.getWeight();
        ++numEdgesInMST;
      }
    }

    return mstWeight;
  }

  /**
   * Creates the input graph.
   *
   * @param args the command line args
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void createInputGraph(String[] args) throws IOException {
    this.graph = newGraphInstance();

    GraphGenerator.readIntegerEdgeGraph(args, this.graph, new Lambda<Integer, KNode>() {
      @Override
      public KNode call(Integer arg0) {
        return new KNode();
      }
    });

    boolean useArrayGraph = SystemProperties.getBooleanProperty(useArrayGraphProperty, true);
    if (useArrayGraph) {
      this.graph = new LocalComputationGraph.ObjectGraphBuilder().from(graph).create();
    }
  }

  /**
   * creates a set of edges
   * initializes the Union-Find related data
   */
  void initUF() {
    this.edges = new HashSet<KEdge>();

    long time = System.nanoTime();
    final Lambda2Void<GNode<KNode>, GNode<KNode>> outNeighborClosure = new Lambda2Void<GNode<KNode>, GNode<KNode>>() {

      @Override
      public void call(GNode<KNode> dst, GNode<KNode> src) {
        Integer weight = graph.getEdgeData(src, dst, MethodFlag.NONE);
        edges.add(new KEdge(src, dst, weight));
      }
    };

    // add all the edges to the the edges set 
    graph.map(new LambdaVoid<GNode<KNode>>() {

      @Override
      public void call(GNode<KNode> node) {
        node.map(outNeighborClosure, node, MethodFlag.NONE);
      }
    }, MethodFlag.NONE);

    time = (System.nanoTime() - time) / 1000000L;
    System.err.println("Time to create Edges = " + time);

    // call makeSet on every node of the graph
    graph.map(new LambdaVoid<GNode<KNode>>() {

      @Override
      public void call(GNode<KNode> node) {
        KNode kn = node.getData();
        kn.makeSet(node);
      }
    });

  }

  /**
   * Initialize.
   *
   * @param args the command line args
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void initialize(String[] args) throws IOException {
    createInputGraph(args);
    initUF();
  }

  /**
   * Run.
   *
   * @param args the command line args
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void run(String[] args) throws IOException {
    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println();
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.printf("application: Kruskal's MST (%s version)\n", getVersion());
      System.err.println("Finds the Minimal Spanning Tree of a graph with integer weights");
      System.err.println("http://iss.ices.utexas.edu/lonestar/kruskal.html");
      System.err.println();
    }

    // initialize graph
    initialize(args);

    if (Launcher.getLauncher().isFirstRun()) {
      System.err.printf("configuration: %d nodes, %d edges\n", graph.size(), edges.size());
      System.err.println("number of threads: " + GaloisRuntime.getRuntime().getMaxThreads());
      System.err.println();
    }

    Launcher.getLauncher().startTiming();

    try {
      runLoop();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Launcher.getLauncher().stopTiming();

    long totalWeight = calcMSTWeight();
    System.out.printf("MST weight = %d, num. edges in mst = %d\n", totalWeight, this.numEdgesInMST);

    if (Launcher.getLauncher().isFirstRun()) {
      verify();
    }

  }

}
