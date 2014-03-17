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

package sssp.main;

import galois.objects.AbstractNoConflictBaseObject;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.GraphGenerator;
import galois.objects.graph.LocalComputationGraph;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.wl.Bucketed;
import galois.runtime.wl.BulkSynchronous;
import galois.runtime.wl.ChunkedFIFO;
import galois.runtime.wl.FIFO;
import galois.runtime.wl.Ordered;
import galois.runtime.wl.Priority;
import galois.runtime.wl.Priority.Rule;

import java.util.Collections;
import java.util.Comparator;

import util.Launcher;
import util.fn.Lambda;
import util.fn.Lambda2Void;
import util.fn.Lambda3Void;
import util.fn.LambdaVoid;

/**
 * Single-source shortest path for positive edge weights. Best performance
 * requires tweaking worklist parameters. 
 * 
 *
 */
public class Main {
  protected static final int INFINITY = Integer.MAX_VALUE / 2 - 1;

  private ExecutorType executorType;

  protected ObjectGraph<Node, Integer> graph;
  private int numNodes;
  private int numEdges;

  private GNode<Node> source;
  private GNode<Node> sink;

  private void printUsage() {
    System.err.println("<num runs> <filename> [executor type]");
    System.exit(1);
  }

  protected String getVersion() {
    return GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois";
  }

  public final void run(String[] args) throws Exception {
    if (args.length < 2) {
      printUsage();
    }

    int maxNodes = Integer.parseInt(args[0]);
    String filename = args[1];
    //    executorType = ExecutorType.DEFAULT_FIFO;
    //    executorType = ExecutorType.DELTA_FIFO;
    executorType = ExecutorType.DELTA_CHUNKED;

    if (args.length > 2) {
      executorType = ExecutorType.valueOf(args[2]);
    }

    printStartMessage(getVersion(), filename);

    // Use serial graph to build because its faster and we don't
    // need concurrent access in this phase
    MorphGraph.ObjectGraphBuilder builder = new MorphGraph.ObjectGraphBuilder();
    ObjectGraph<Node, Integer> empty = builder.directed(true).serial(true).create();
    GraphGenerator.readIntegerEdgeGraph(filename, empty, new Lambda<Integer, Node>() {
      @Override
      public Node call(Integer arg0) {
        return new Node(arg0, INFINITY);
      }
    });
    graph = new LocalComputationGraph.ObjectGraphBuilder().from(empty).create();

    Launcher.getLauncher().startTiming();
    for (int i = 0; i < maxNodes; i++) {
      updateSourceAndSink(i, i + maxNodes);
      runBody(source);
      System.out.println(sink.getData());
    }
    Launcher.getLauncher().stopTiming();

    if (Launcher.getLauncher().isFirstRun()) {
      verify();
    }
  }

  private final Rule getRule() {
    return executorType.getRule(graph, numNodes, numEdges, 300000);
  }

  private void updateSourceAndSink(final int sourceId, final int sinkId) {
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();
        node.dist = INFINITY;

        if (node.id == sourceId) {
          source = src;
          node.dist = 0;
        } else if (node.id == sinkId) {
          sink = src;
        }
      }
    });
  }

  private void printStartMessage(String version, String filename) {
    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.printf("application: single source shortest path (%s version)\n", version);
      System.err.println("Computes the shortest path from a source node to all nodes");
      System.err.println("in a directed graph using a modified Bellman-Ford algorithm");
      System.err.println("http://iss.ices.utexas.edu/lonestar/sssp.html");
      System.err.println();
      System.err.println("configuration:");
      System.err.println("  threads: " + GaloisRuntime.getRuntime().getMaxThreads());
      System.err.printf("  file: %s\n  executor type: %s\n", filename, executorType);
    }
  }

  private int getEdgeData(int edgeData) {
    if (executorType.bfs)
      return 1;
    else
      return edgeData;
  }

  protected void verify() {
    if (source.getData().dist != 0) {
      throw new IllegalStateException("source has non-zero dist value");
    }

    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        final int dist = src.getData().dist;
        if (dist >= INFINITY) {
          System.err.printf("found node = %s with label >=  INFINITY = %d", src, dist);
        }

        src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
          @Override
          public void call(GNode<Node> dst, GNode<Node> src) {
            int ddist = dst.getData().dist;

            if (ddist > dist + getEdgeData(graph.getEdgeData(src, dst))) {
              throw new IllegalStateException("bad level value at " + dst + " which is a neighbor of " + src);
            }
          }
        }, src);
      }
    });

    System.err.println("result verified");
  }

  /**
   * One iteration.
   */
  private final Lambda3Void<GNode<Node>, GNode<Node>, ForeachContext<GNode<Node>>> body = new Lambda3Void<GNode<Node>, GNode<Node>, ForeachContext<GNode<Node>>>() {
    @Override
    public void call(GNode<Node> dst, GNode<Node> src, ForeachContext<GNode<Node>> ctx) {
      int dist = src.getData(MethodFlag.NONE).dist;
      int newDist = dist + (int) getEdgeData(graph.getEdgeData(src, dst, MethodFlag.NONE));
      int oldDist = dst.getData(MethodFlag.NONE).dist;

      if (newDist < oldDist) {
        dst.getData(MethodFlag.NONE).dist = newDist;
        ctx.add(dst, MethodFlag.NONE);
      }
    }
  };

  protected void runBody(GNode<Node> source) throws Exception {
    GaloisRuntime.foreach(Collections.singleton(source), new Lambda2Void<GNode<Node>, ForeachContext<GNode<Node>>>() {
      @Override
      public void call(GNode<Node> src, ForeachContext<GNode<Node>> ctx) {
        src.map(body, src, ctx);
      }
    }, getRule());
  }

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }

  static class Node extends AbstractNoConflictBaseObject {
    public final int id;
    public int dist;

    public Node(int id, int dist) {
      this.id = id;
      this.dist = dist;
    }

    public String toString() {
      return String.format("[%d] dist: %d", id, dist);
    }

    @Override
    public Object gclone() {
      Node n = new Node(id, dist);
      return n;
    }

    @Override
    public void restoreFrom(Object copy) {
      Node n = (Node) copy;
      this.dist = n.dist;
    }
  }

  private enum ExecutorType {
    DEFAULT_FIFO, DEFAULT_PRIORITY, CHUNKED_FIFO, CHUNKED_PRIORITY, CHUNKED_STAT_FIFO, ORD_CHUNKED_FIFO, ORD_CHUNKED_PRIORITY, DELTA_FIFO, DELTA_CHUNKED, BFS_DEFAULT(
        true), BFS_CHUNKED(true), BFS_PART_CHUNKED(true), BFS_PART(true), BFS_WAVEFRONT(true), BFS_WAVEFRONT_CHUNKED(
        true);

    private final boolean bfs;

    private ExecutorType() {
      this(false);
    }

    private ExecutorType(boolean bfs) {
      this.bfs = bfs;
    }

    private Rule getRule(Graph<Node> graph, int numNodes, int numEdges, int maxWeight) {
      Comparator<GNode<Node>> comp = new NodeComparator();

      // Some sensible bucketing scheme
      final int delta = maxWeight / 16;
      final int numBuckets = 1024;
      Lambda<GNode<Node>, Integer> indexer = new Lambda<GNode<Node>, Integer>() {
        @Override
        public Integer call(GNode<Node> x) {
          return Math.min(x.getData(MethodFlag.ALL).dist / delta, numBuckets - 1);
        }
      };

      int chunkSize = 16;
      int bfsChunkSize = 32;

      switch (this) {
      case DEFAULT_FIFO:
        return Priority.first(FIFO.class);
      case DEFAULT_PRIORITY:
        return Priority.first(Ordered.class, comp);
      case CHUNKED_FIFO:
        return Priority.first(ChunkedFIFO.class, chunkSize);
      case CHUNKED_PRIORITY:
        return Priority.first(Ordered.class, comp).then(ChunkedFIFO.class, chunkSize);
      case ORD_CHUNKED_FIFO:
        return Priority.first(ChunkedFIFO.class, chunkSize).then(Ordered.class, comp);
        // TODO(ddn): this might not be right translation
      case ORD_CHUNKED_PRIORITY:
        return Priority.first(Ordered.class, comp).then(ChunkedFIFO.class, chunkSize).then(Ordered.class, comp);
      case DELTA_FIFO:
        return Priority.first(Bucketed.class, numBuckets, indexer).then(FIFO.class);
      case DELTA_CHUNKED:
        return Priority.first(Bucketed.class, numBuckets, indexer).then(ChunkedFIFO.class, chunkSize);
      case BFS_DEFAULT:
        return Priority.first(FIFO.class);
      case BFS_CHUNKED:
        return Priority.first(ChunkedFIFO.class, bfsChunkSize);
      case BFS_PART_CHUNKED:
      case BFS_PART:
        throw new Error();
      case BFS_WAVEFRONT:
        return Priority.first(BulkSynchronous.class);
      case BFS_WAVEFRONT_CHUNKED:
        return Priority.first(BulkSynchronous.class).then(ChunkedFIFO.class, bfsChunkSize);
      default:
        throw new Error();
      }
    }

    private static class NodeComparator implements Comparator<GNode<Node>> {
      @Override
      public int compare(GNode<Node> n1, GNode<Node> n2) {
        return n1.getData(MethodFlag.NONE).dist - n2.getData(MethodFlag.NONE).dist;
      }
    }
  }
}
