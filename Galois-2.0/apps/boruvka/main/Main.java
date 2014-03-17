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

package boruvka.main;

import static galois.objects.MethodFlag.CHECK_CONFLICT;
import static galois.objects.MethodFlag.NONE;
import galois.objects.AbstractNoConflictBaseObject;
import galois.objects.Bag;
import galois.objects.BagBuilder;
import galois.objects.GMap;
import galois.objects.GMapBuilder;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.GraphGenerator;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.WorkNotUsefulException;
import galois.runtime.wl.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import util.Launcher;
import util.SystemProperties;
import util.fn.Lambda;
import util.fn.Lambda2Void;
import util.fn.Lambda3Void;
import util.fn.LambdaVoid;

public class Main {
  static class Node extends AbstractNoConflictBaseObject {
    @Override
    public Object gclone() {
      return null;
    }

    @Override
    public void restoreFrom(Object copy) {
    }
  }

  protected ObjectGraph<Node, Integer> graph;
  private ObjectGraph<Node, Integer> graphCopy;
  private Bag<Integer> contracted;

  private DummyClosure dummy = new DummyClosure();
  private ContractClosure contract = new ContractClosure();

  private ObjectGraph<Node, Integer> newGraphInstance() {
    MorphGraph.ObjectGraphBuilder builder = new MorphGraph.ObjectGraphBuilder();
    return builder.create();
  }

  protected void runLoop() throws ExecutionException {
    GaloisRuntime.foreach(graph, new Lambda2Void<GNode<Node>, ForeachContext<GNode<Node>>>() {
      @Override
      public void call(GNode<Node> item, ForeachContext<GNode<Node>> ctx) {
        runBody(item, ctx);
      }
    }, Priority.defaultOrder());
  }

  protected final void runBody(GNode<Node> n, ForeachContext<GNode<Node>> ctx) {
    if (!graph.contains(n)) {
      WorkNotUsefulException.throwException();
    }
    FindLightestClosure closure = new FindLightestClosure();
    n.map(closure, n, CHECK_CONFLICT);
    // Contract along lightest edge
    if (closure.lightest != null) {
      // Remember lightest to recreate MST
      GNode<Node> gNode = edgeContract(n, closure.lightest);
      ctx.add(gNode, NONE);
      // NB(ddn): This needs to happen after edgeContract otherwise
      // we lose the cautious property of boruvka: we only
      // mutate data structures after we have seen our neighborhood.
      contracted.add(closure.min, NONE);
    }
  }

  private GNode<Node> edgeContract(GNode<Node> dst, GNode<Node> src) {
    // NB(ddn): Added the following to make boruvka cautious
    src.map(dummy, CHECK_CONFLICT);
    // Remove edge between n1 and n2
    graph.removeNeighbor(dst, src, NONE);
    // Set n2 neighbors to n1's neighbors, update weights to
    // reflect new reachability
    src.map(contract, src, dst, NONE);

    graph.remove(src, NONE);
    return dst;
  }

  private static long sum(Collection<Integer> s) {
    long ret = 0;
    for (Integer d : s) {
      assert d >= 0;
      ret += d;
    }
    return ret;
  }

  public long run(String[] args) throws Exception {
    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.printf("application: Boruvka's algorithm (%s version)\n",
          GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois");
      System.err.println("Compute the minimal spanning tree");
      System.err.println("http://iss.ices.utexas.edu/lonestar/boruvka.html");
      System.err.println();
      System.err.println("number of threads: " + GaloisRuntime.getRuntime().getMaxThreads());
      System.err.println();
    }

    init(args);
    contracted = new BagBuilder<Integer>().create();

    Launcher.getLauncher().startTiming();
    runLoop();
    Launcher.getLauncher().stopTiming();

    long weightSum = sum(contracted);

    if (shouldVerify()) {
      System.out.printf("MST weight: " + weightSum + "\n");
      verify(weightSum, args);
    }

    return weightSum;
  }

  private void init(String[] args) throws IOException, ExecutionException {
    this.graph = newGraphInstance();
    GraphGenerator.readIntegerEdgeGraph(args, graph, new Lambda<Integer, Node>() {
      @Override
      public Node call(Integer arg0) {
        return new Node();
      }
    });
    long expected = SystemProperties.getLongProperty("verify.result", Long.MIN_VALUE);
    if (shouldVerify() && expected == Long.MIN_VALUE) {
      copyGraph(); // needed for use by prim for verification
    }
  }

  private static boolean shouldVerify() {
    return Launcher.getLauncher().isFirstRun();
  }

  private void copyGraph() throws ExecutionException {
    graphCopy = newGraphInstance();

    final GMap<GNode<Node>, GNode<Node>> nodeMapping = new GMapBuilder<GNode<Node>, GNode<Node>>().create();

    // for every node in graph, create a node in graphCopy
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> n) {
        GNode<Node> copy = graphCopy.createNode(n.getData());
        graphCopy.add(copy);

        nodeMapping.put(n, copy);
      }
    });

    // lambda to create edges for outgoing neighbors
    final Lambda2Void<GNode<Node>, GNode<Node>> createEdgesFn = new Lambda2Void<GNode<Node>, GNode<Node>>() {
      @Override
      public void call(GNode<Node> dst, GNode<Node> src) {
        Integer weight = graph.getEdgeData(src, dst, MethodFlag.NONE);
        GNode<Node> srcCpy = nodeMapping.get(src, MethodFlag.NONE);
        GNode<Node> dstCpy = nodeMapping.get(dst, MethodFlag.NONE);

        graphCopy.addEdge(srcCpy, dstCpy, weight, MethodFlag.CHECK_CONFLICT);
      }
    };

    // for each edge in graph, create an edge with same weight in graphCopy
    GaloisRuntime.foreach(graph, new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> n) {
        n.map(createEdgesFn, n, MethodFlag.CHECK_CONFLICT);
      }
    });
  }

  protected void printMessage(String msg) {
    if (Launcher.getLauncher().isFirstRun()) {
      System.err.print(msg);
    }
  }

  private void verify(long result, String[] args) throws Exception {
    Boolean fullVerify = SystemProperties.getBooleanProperty("verify.full", false);
    long expected = SystemProperties.getLongProperty("verify.result", Long.MIN_VALUE);
    if (fullVerify || expected == Long.MIN_VALUE) {
      Prims prim = new Prims();
      // use the same graph as mine, because the graphs are randomly generated.
      prim.graph = this.graphCopy;
      expected = prim.run(args);
    }
    if (result == expected) {
      System.out.println("MST ok.");
    } else {
      throw new IllegalStateException("Inconsistent MSTs: " + expected + " != " + result);
    }
  }

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }

  private static class DummyClosure implements LambdaVoid<GNode<Node>> {
    @Override
    public void call(GNode<Node> dst) {
    }
  }

  private class ContractClosure implements Lambda3Void<GNode<Node>, GNode<Node>, GNode<Node>> {
    @Override
    public void call(GNode<Node> dst, GNode<Node> src, GNode<Node> origDst) {
      int newWeight = graph.getEdgeData(src, dst, NONE);
      if (!graph.addEdge(dst, origDst, newWeight, NONE)) {
        // Already neighbors
        int oldWeight = graph.getEdgeData(dst, origDst, NONE);
        if (newWeight < oldWeight) {
          graph.setEdgeData(dst, origDst, newWeight, NONE);
        }
      }
    }
  }

  private class FindLightestClosure implements Lambda2Void<GNode<Node>, GNode<Node>> {
    GNode<Node> lightest = null;
    int min = Integer.MAX_VALUE;

    @Override
    public void call(GNode<Node> neighbor, GNode<Node> node) {
      int curr = graph.getEdgeData(node, neighbor, NONE);
      if (curr < min) {
        lightest = neighbor;
        min = curr;
      }
    }
  }
}
