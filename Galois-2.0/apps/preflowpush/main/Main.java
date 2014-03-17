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

package preflowpush.main;

import galois.objects.AbstractNoConflictBaseObject;
import galois.objects.BagBuilder;
import galois.objects.Counter;
import galois.objects.CounterToSuspendWithBuilder;
import galois.objects.Mappables;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.LocalComputationGraph;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.WorkNotUsefulException;
import galois.runtime.wl.Bucketed;
import galois.runtime.wl.ChunkedFIFO;
import galois.runtime.wl.FIFO;
import galois.runtime.wl.Priority;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import util.Launcher;
import util.MutableInteger;
import util.SystemProperties;
import util.ThreadTimer;
import util.ThreadTimer.Tick;
import util.fn.Lambda;
import util.fn.Lambda2Void;
import util.fn.Lambda3Void;
import util.fn.LambdaVoid;

/**
 * Preflowpush algorithm with global relabeling, hi-low ordering and gap relabeling.
 * 
 *
 */
public class Main {
  /**
   * Parameters from the original Goldberg algorithm to control when global
   * relabeling occurs. For comparison purposes, we keep them the same as
   * before, but it is possible to achieve much better performance by adjusting
   * the global relabel frequency. 
   */
  private final int ALPHA = 6;
  private final int BETA = 12;

  /**
   * Use serial global relabeling and gap relabeling or parallel variants.
   */
  private final boolean useSerialPeriodicActions = false;

  private long elapsedTimeInGlobalRelabel;
  private Tick globalRelabelId;
  protected int globalRelabelInterval;

  private ObjectGraph<Node, Edge> graph;
  private int numEdges;
  protected int numNodes;

  private GNode<Node> sink;
  private GNode<Node> source;

  public Main() {
  }

  private static void decrementGap(final Counter<GNode<Node>>[] gapYet, ForeachContext<GNode<Node>> ctx, int height) {
    if (gapYet != null) {
      gapYet[height].increment(ctx, MethodFlag.NONE);
    }
  }

  private static void incrementGap(final Counter<GNode<Node>>[] gapYet, ForeachContext<GNode<Node>> ctx, int height) {
    if (gapYet != null) {
      gapYet[height].increment(ctx, -1, MethodFlag.NONE);
    }
  }

  private static void incrementGap(final Counter<GNode<Node>>[] gapYet, int height) {
    if (gapYet != null) {
      gapYet[height].increment(-1);
    }
  }

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }

  private static InputStream openFile(File file) throws IOException {
    FileInputStream fileStream = new FileInputStream(file);
    String name = file.getName();
    if (name.endsWith(".gz")) {
      return new GZIPInputStream(fileStream);
    } else {
      return fileStream;
    }
  }

  private static void showUsage() {
    System.err.println("<filename>");
    System.exit(1);
  }

  private static boolean validEdgeCap(int scap, int dcap, int maxscap, int maxdcap) {
    return (maxscap - scap + maxdcap - dcap) == 0 && scap <= maxscap + maxdcap && dcap <= maxscap + maxdcap
        && scap >= 0 && dcap >= 0;
  }

  private void checkAugmentingPathExistence() {
    final Set<GNode<Node>> visited = new HashSet<GNode<Node>>();
    final Deque<GNode<Node>> queue = new ArrayDeque<GNode<Node>>();

    visited.add(source);
    queue.add(source);

    while (!queue.isEmpty()) {
      GNode<Node> src = queue.poll();
      src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
        @Override
        public void call(GNode<Node> dst, GNode<Node> src) {
          if (!visited.contains(dst) && graph.getEdgeData(src, dst).cap > 0) {
            visited.add(dst);
            queue.add(dst);
          }
        }
      }, src);
    }

    if (visited.contains(sink)) {
      throw new IllegalStateException("augmenting path exists");
    }
  }

  @SuppressWarnings("unused")
  private void checkFlows() {
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        final MutableInteger inflow = new MutableInteger();
        final MutableInteger outflow = new MutableInteger();

        src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
          @Override
          public void call(GNode<Node> dst, GNode<Node> src) {
            Edge e1 = graph.getEdgeData(src, dst);
            Edge e2 = graph.getEdgeData(dst, src);

            int scap = e1.cap;
            int dcap = e2.cap;
            int maxscap = e1.ocap;
            int maxdcap = e2.ocap;

            if (!validEdgeCap(scap, dcap, maxscap, maxdcap)) {
              throw new IllegalStateException("edge values are inconsistent: " + toStringEdge(src, dst));
            }
            if (maxscap > scap) {
              outflow.add(maxscap - scap);
            } else if (maxdcap > dcap) {
              inflow.add(maxdcap - dcap);
            }
          }
        }, src);

        Node node = src.getData();

        if (node.isSource) {
          if (inflow.get() > 0)
            throw new IllegalStateException("source has inflow");
        } else if (node.isSink) {
          if (outflow.get() > 0)
            throw new IllegalStateException("sink has outflow");
        } else {
          if (node.excess != 0)
            throw new IllegalStateException("node " + toStringNode(src) + " still has excess flow");
          int flow = inflow.get() - outflow.get();
          if (flow != 0)
            throw new IllegalStateException("node " + toStringNode(src) + " does not conserve flow: " + flow);
        }
      }
    });
  }

  private void checkFlowsForCut() {
    // Check psuedoflow
    // XXX: Currently broken
    // for (int i = 0; i < numNodes; i++) {
    // for (int j = nodes[i]; j < nodes[i+1]; j++) {
    // int edgeIdx = edges[j];
    // int ocap = orig.getCapacity(edgeIdx, i);
    // if (ocap <= 0)
    // continue;
    // // Original edge
    // int dst = getMate(edgeIdx, i);
    // int icap = getCapacity(edgeIdx, i);
    // int dcap = getCapacity(edgeIdx, dst);
    // if (icap + dcap != ocap || icap < 0 || dcap < 0) {
    // throw new IllegalStateException("Incorrect flow at " +
    // toStringEdge(edgeIdx) + " " + ocap);
    // }
    // }
    // }

    // Check conservation
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();

        if (node.isSource || node.isSink) {
          return;
        }

        if (node.excess < 0)
          throw new IllegalStateException("Excess at " + toStringNode(src));

        final MutableInteger sum = new MutableInteger();

        src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
          @Override
          public void call(GNode<Node> dst, GNode<Node> src) {
            int ocap = graph.getEdgeData(src, dst).ocap;
            int delta = 0;
            if (ocap > 0) {
              delta -= ocap - graph.getEdgeData(src, dst).cap;
            } else {
              delta += graph.getEdgeData(src, dst).cap;
            }
            sum.add(delta);
          }
        }, src);

        if (node.excess != sum.get())
          throw new IllegalStateException("Not pseudoflow " + node.excess + " != " + sum + " at node " + node.id);
      }
    });
  }

  private void checkHeights() {
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        src.map(new Lambda2Void<GNode<Node>, GNode<Node>>() {
          @Override
          public void call(GNode<Node> dst, GNode<Node> src) {
            int sh = src.getData().height;
            int dh = dst.getData().height;
            int cap = graph.getEdgeData(src, dst).cap;
            if (cap > 0 && sh > dh + 1) {
              throw new IllegalStateException("height violated at " + src + " with " + toStringEdge(src, dst));
            }
          }
        }, src);
      }
    });
  }

  private void checkMaxFlow() {
    Boolean fullVerify = SystemProperties.getBooleanProperty("verify.full", false);
    double expected = SystemProperties.getDoubleProperty("verify.result", Double.MIN_VALUE);
    if (fullVerify || expected == Double.MIN_VALUE) {
      // checkFlows(orig);
      checkFlowsForCut();
      checkHeights();
      checkAugmentingPathExistence();
    } else {
      double result = sink.getData().excess;
      if (result != expected) {
        throw new IllegalStateException("Inconsistent flows: " + expected + " != " + result);
      }
    }
  }

  private final Lambda3Void<GNode<Node>, Local<Node>, ForeachContext<GNode<Node>>> dbody = new Lambda3Void<GNode<Node>, Local<Node>, ForeachContext<GNode<Node>>>() {
    @Override
    public void call(GNode<Node> dst, Local<Node> l, ForeachContext<GNode<Node>> ctx) {
      if (l.finished)
        return;

      Node node = l.src.getData(MethodFlag.NONE);

      int cap = graph.getEdgeData(l.src, dst, MethodFlag.NONE).cap;

      if (cap > 0 && l.cur >= node.current) {
        int amount = 0;
        Node dnode = dst.getData(MethodFlag.NONE);
        if (node.height - 1 == dnode.height) {
          // Push flow
          amount = (int) Math.min(node.excess, cap);
          reduceCapacity(l.src, dst, amount);
          // Only add once
          if (!dnode.isSink && !dnode.isSource && dnode.excess == 0) {
            ctx.add(dst, MethodFlag.NONE);
          }
          node.excess -= amount;
          dnode.excess += amount;
          if (node.excess == 0) {
            l.finished = true;
            node.current = l.cur;
            return;
          }
        }
      }

      l.cur++;
    }
  };

  private boolean discharge(final ForeachContext<GNode<Node>> ctx, Counter<GNode<Node>>[] gapYet, GNode<Node> src) {
    final Node node = src.getData(MethodFlag.CHECK_CONFLICT, MethodFlag.NONE);
    int prevHeight = node.height;
    boolean retval = false;

    if (node.excess == 0 || node.height >= numNodes)
      WorkNotUsefulException.throwException();

    Local<Node> l = new Local<Node>();
    l.src = src;

    while (true) {
      l.finished = false;

      src.map(dbody, l, ctx, !retval ? MethodFlag.CHECK_CONFLICT : MethodFlag.NONE);
      if (l.finished)
        break;

      // Went through all our edges and still
      // have flow: Relabel
      relabel(src, l);

      retval = true;
      decrementGap(gapYet, ctx, prevHeight);

      if (node.height == numNodes)
        break;

      incrementGap(gapYet, ctx, node.height);
      prevHeight = node.height;
      l.cur = 0;
    }

    return retval;
  }

  private void endGlobalRelabel() {
    elapsedTimeInGlobalRelabel += globalRelabelId.elapsedTime(false, ThreadTimer.tick());
  }

  private void gapAtSerial(Counter<GNode<Node>>[] gapYet, final int h) {
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();
        if (node.isSink || node.isSource)
          return;
        assert h != node.height;
        if (h < node.height && node.height < numNodes)
          node.height = numNodes;

        return;
      }
    });

    if (gapYet != null) {
      for (int i = h + 1; i < numNodes; i++) {
        gapYet[h].reset();
      }
    }
  }

  private void gapAt(final Counter<GNode<Node>>[] gapYet, final int h) throws ExecutionException {
    GaloisRuntime.foreach(graph, new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData(MethodFlag.NONE);
        if (node.isSink || node.isSource)
          return;

        assert h != node.height;
        if (h < node.height && node.height < numNodes)
          node.height = numNodes;
      }
    });

    if (gapYet != null) {
      GaloisRuntime.foreach(Mappables.range(h + 1, numNodes), new LambdaVoid<Integer>() {
        @Override
        public void call(Integer arg0) {
          gapYet[arg0].reset();
        }
      });
    }
  }

  private void globalRelabelSerial(final Counter<GNode<Node>>[] gapYet, final ForeachContext<GNode<Node>> ctx) {
    final Set<GNode<Node>> visited = new HashSet<GNode<Node>>();
    final Deque<GNode<Node>> queue = new ArrayDeque<GNode<Node>>();

    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();
        // Max distance
        node.height = numNodes;
        node.current = 0;

        if (node.isSink) {
          node.height = 0;
          queue.add(src);
          visited.add(src);
        }
      }
    });

    if (gapYet != null) {
      for (int i = 0; i < numNodes; i++) {
        gapYet[i].reset();
      }
    }

    Lambda2Void<GNode<Node>, GNode<Node>> body = new Lambda2Void<GNode<Node>, GNode<Node>>() {
      @Override
      public void call(GNode<Node> dst, GNode<Node> src) {
        if (visited.contains(dst))
          return;

        if (graph.getEdgeData(dst, src).cap > 0) {
          visited.add(dst);
          Node node = dst.getData();
          node.height = src.getData().height + 1;
          assert node.height <= numNodes;
          queue.addLast(dst);
        }
      }
    };

    // Do walk on reverse *residual* graph!
    while (!queue.isEmpty()) {
      GNode<Node> src = queue.pollFirst();

      src.map(body, src);
    }

    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();

        if (node.isSink || node.isSource || node.height >= numNodes) {
          return;
        }

        if (node.excess > 0) {
          ctx.add(src);
        }

        incrementGap(gapYet, node.height);
      }
    });

    System.out.printf("  Flow after global relabel: %.0f\n", sink.getData().excess);
  }

  private void globalRelabel(final Counter<GNode<Node>>[] gapYet, final ForeachContext<GNode<Node>> octx)
      throws ExecutionException {

    GaloisRuntime.foreach(graph, new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData(MethodFlag.NONE);

        // Max distance
        node.height = numNodes;
        node.current = 0;

        if (node.isSink) {
          node.height = 0;
        }
      }
    });

    if (gapYet != null) {
      GaloisRuntime.foreach(Mappables.range(0, numNodes), new LambdaVoid<Integer>() {
        @Override
        public void call(Integer arg0) {
          gapYet[arg0].reset();
        }
      });
    }

    // Do walk on reverse residual graph!
    final Lambda3Void<GNode<Node>, GNode<Node>, ForeachContext<GNode<Node>>> body = new Lambda3Void<GNode<Node>, GNode<Node>, ForeachContext<GNode<Node>>>() {
      @Override
      public void call(GNode<Node> dst, GNode<Node> src, ForeachContext<GNode<Node>> ctx) {
        if (graph.getEdgeData(dst, src, MethodFlag.NONE).cap > 0) {
          Node node = dst.getData(MethodFlag.NONE);
          int newHeight = src.getData(MethodFlag.NONE).height + 1;
          if (newHeight < node.height) {
            node.height = newHeight;
            ctx.add(dst, MethodFlag.NONE);
          }
        }
      }
    };

    GaloisRuntime.foreach(Collections.singleton(sink), new Lambda2Void<GNode<Node>, ForeachContext<GNode<Node>>>() {
      @Override
      public void call(GNode<Node> src, ForeachContext<GNode<Node>> ctx) {
        src.map(body, src, ctx);
      }
    }, Priority.first(ChunkedFIFO.class, 8));

    final Collection<GNode<Node>> toAdd = new BagBuilder<GNode<Node>>().create();

    GaloisRuntime.foreach(graph, new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData(MethodFlag.NONE);

        if (node.isSink || node.isSource || node.height >= numNodes) {
          return;
        }

        if (node.excess > 0) {
          toAdd.add(src);
          // XXX(ddn): Would like to add the worklist here
          // to get full fun of parallelism, but octx can
          // only be used serially. Thankfully, the number
          // of nodes added here is small for most inputs
          // so fixing this doesn't matter much
          // octx.add(src);
        }

        incrementGap(gapYet, node.height);
      }
    });

    for (GNode<Node> n : toAdd) {
      octx.add(n);
    }

    System.out.printf("  Flow after global relabel: %.0f\n", sink.getData().excess);
  }

  private final Collection<GNode<Node>> initializePreflow(final Counter<GNode<Node>>[] gapYet) {
    final List<GNode<Node>> retval = new ArrayList<GNode<Node>>();

    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> src) {
        Node node = src.getData();
        if (!node.isSink && !node.isSource) {
          incrementGap(gapYet, node.height);
        }
      }
    });

    source.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> dst) {
        int cap = graph.getEdgeData(source, dst).cap;
        reduceCapacity(source, dst, cap);
        Node node = dst.getData();
        node.excess += cap;
        if (cap > 0)
          retval.add(dst);
      }
    });

    return retval;
  }

  private void read(File file) throws IOException {
    if (file.getName().endsWith(".txt") || file.getName().endsWith(".txt.gz")) {
      readOldFile(openFile(file));
    } else {
      read(openFile(file));
    }

    LocalComputationGraph.ObjectGraphBuilder builder = new LocalComputationGraph.ObjectGraphBuilder();
    graph = builder.from(graph).create();
    graph.map(new LambdaVoid<GNode<Node>>() {
      @Override
      public void call(GNode<Node> node) {
        if (node.getData().isSink) {
          sink = node;
        } else if (node.getData().isSource) {
          source = node;
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void read(InputStream input) throws IOException {
    MorphGraph.ObjectGraphBuilder builder = new MorphGraph.ObjectGraphBuilder();
    graph = builder.directed(true).create();
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    GNode<Node> nodes[] = null;

    try {
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("c "))
          continue;

        StringTokenizer tok = new StringTokenizer(line);
        String first = tok.nextToken();
        if (first.equals("p")) {
          tok.nextToken(); // "max"
          // Indexes are zero-based instead of one-based
          this.numNodes = Integer.parseInt(tok.nextToken()) + 1;
          this.numEdges = Integer.parseInt(tok.nextToken()); // numEdges
          nodes = new GNode[numNodes];
          for (int i = 0; i < numNodes; i++) {
            nodes[i] = graph.createNode(new Node(i));
            graph.add(nodes[i]);
          }
        } else if (first.equals("n")) {
          int id = Integer.parseInt(tok.nextToken());
          if (tok.nextToken().equals("s")) {
            source = nodes[id];
            source.getData().setSource(numNodes);
          } else {
            sink = nodes[id];
            sink.getData().setSink();
          }
        } else if (first.equals("a")) {
          int src = Integer.parseInt(tok.nextToken());
          int dst = Integer.parseInt(tok.nextToken());
          int cap = Integer.parseInt(tok.nextToken());

          addEdge(nodes[src], nodes[dst], cap, 0);
        } else {
          throw new Error("Not DIMACS file");
        }
      }
    } finally {
      reader.close();
    }
  }

  @SuppressWarnings("unchecked")
  private void readOldFile(InputStream input) throws IOException {
    MorphGraph.ObjectGraphBuilder builder = new MorphGraph.ObjectGraphBuilder();
    graph = builder.directed(true).create();
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    GNode<Node> nodes[] = null;
    try {
      String line = null;

      int width = Integer.parseInt(reader.readLine());
      int height = Integer.parseInt(reader.readLine());
      reader.readLine(); // startFlow, not used

      numNodes = width * height;
      int sourceId = numNodes++;
      int sinkId = numNodes++;

      nodes = new GNode[numNodes];
      for (int i = 0; i < numNodes; i++) {
        Node n = new Node(i);
        if (i == sourceId)
          n.setSource(numNodes);
        if (i == sinkId)
          n.setSink();
        nodes[i] = graph.createNode(n);
        graph.add(nodes[i]);
      }

      source = nodes[sourceId];
      sink = nodes[sinkId];

      line = reader.readLine();
      while (line != null && line.startsWith("N: ")) {
        StringTokenizer tok = new StringTokenizer(line);
        tok.nextToken(); // "N: "

        int src = Integer.parseInt(tok.nextToken());
        int cap = Integer.parseInt(tok.nextToken());

        if (cap > 0) {
          addEdge(source, nodes[src], cap, 0);
        } else {
          addEdge(nodes[src], sink, -cap, 0);
        }

        // Loop through the edges
        while ((line = reader.readLine()) != null) {
          if (!line.startsWith("->")) {
            break;
          }

          StringTokenizer tok2 = new StringTokenizer(line);
          tok2.nextToken(); // "->"

          int dst = Integer.parseInt(tok2.nextToken());
          int scap = Integer.parseInt(tok2.nextToken());
          int dcap = Integer.parseInt(tok2.nextToken());

          // Edges exist twice in file, but only process first instance
          if (!graph.hasNeighbor(nodes[src], nodes[dst])) {
            addEdge(nodes[src], nodes[dst], scap, dcap);
          }
        }
      }
    } finally {
      reader.close();
    }
  }

  private void addEdge(GNode<Node> src, GNode<Node> dst, int scap, int dcap) {
    if (graph.hasNeighbor(src, dst)) {
      graph.getEdgeData(src, dst).addOriginalCapacity(scap);
    } else {
      graph.addEdge(src, dst, new Edge(scap));
    }

    if (graph.hasNeighbor(dst, src)) {
      graph.getEdgeData(dst, src).addOriginalCapacity(dcap);
    } else {
      graph.addEdge(dst, src, new Edge(dcap));
    }
  }

  private void reduceCapacity(GNode<Node> src, GNode<Node> dst, int amount) {
    Edge e1 = graph.getEdgeData(src, dst, MethodFlag.NONE);
    Edge e2 = graph.getEdgeData(dst, src, MethodFlag.NONE);
    e1.cap -= amount;
    e2.cap += amount;
  }

  private final Lambda3Void<GNode<Node>, GNode<Node>, Local<Node>> rbody = new Lambda3Void<GNode<Node>, GNode<Node>, Local<Node>>() {
    @Override
    public void call(GNode<Node> dst, GNode<Node> src, Local<Node> l) {
      int cap = graph.getEdgeData(src, dst, MethodFlag.NONE).cap;
      if (cap > 0) {
        Node dnode = dst.getData(MethodFlag.NONE);
        if (dnode.height < l.minHeight) {
          l.minHeight = dnode.height;
          l.minEdge = l.relabelCur;
        }
      }
      l.relabelCur++;
    }
  };

  private void relabel(GNode<Node> src, Local<Node> l) {
    l.resetForRelabel();
    src.map(rbody, src, l, MethodFlag.NONE);

    assert l.minHeight != Integer.MAX_VALUE;

    l.minHeight++;

    Node node = src.getData(MethodFlag.NONE);
    if (l.minHeight < numNodes) {
      node.height = l.minHeight;
      node.current = l.minEdge;
    } else {
      node.height = numNodes;
    }
  }

  public void run(String args[]) throws IOException, ExecutionException {
    if (args.length < 1) {
      showUsage();
    }

    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println();
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/preflowpush.html\n");
      if (GaloisRuntime.getRuntime().useSerial()) {
        System.err.printf("application: Preflow Push (serial version)\n");
      } else {
        System.err.printf("application: Preflow Push (Galois version)\n");
      }
      System.err.println("Maximum flow algorithm using preflows");
      System.err.println("http://iss.ices.utexas.edu/lonestar/preflowpush.html\n");
      System.err.printf("number of threads: %d\n", GaloisRuntime.getRuntime().getMaxThreads());
      System.err.printf("configuration: %s\n", args[0]);
    }

    setup(args);

    if (Launcher.getLauncher().isFirstRun()) {
      System.err.printf("global relabel interval: %d\n", globalRelabelInterval);
    }

    Launcher.getLauncher().startTiming();
    runBody();
    Launcher.getLauncher().stopTiming();

    System.out.printf("Flow is %.0f\n", sink.getData().excess);
    System.out.printf("time in global relabels: %d ms\n", elapsedTimeInGlobalRelabel);

    if (Launcher.getLauncher().isFirstRun()) {
      checkMaxFlow();
      System.out.println("Flow OK");
    }
  }

  @SuppressWarnings("unchecked")
  private void runBody() throws ExecutionException {
    final Counter<GNode<Node>>[] gapYet = new Counter[numNodes];
    for (int i = 0; i < numNodes; i++) {
      final int height = i;
      gapYet[height] = new CounterToSuspendWithBuilder().create(0, true, new LambdaVoid<ForeachContext<GNode<Node>>>() {
        @Override
        public void call(ForeachContext<GNode<Node>> x) {
          System.out.println("Check gap at :" + height);
          try {
            if (useSerialPeriodicActions)
              gapAtSerial(gapYet, height);
            else
              gapAt(gapYet, height);
          } catch (ExecutionException e) {
            throw new Error(e);
          }
        }
      });
    }

    final Counter<GNode<Node>> relabelYet = new CounterToSuspendWithBuilder().create(globalRelabelInterval, false,
        new LambdaVoid<ForeachContext<GNode<Node>>>() {
          @Override
          public void call(ForeachContext<GNode<Node>> ctx) {
            startGlobalRelabel();
            try {
              if (useSerialPeriodicActions)
                globalRelabelSerial(gapYet, ctx);
              else
                globalRelabel(gapYet, ctx);
            } catch (ExecutionException e) {
              throw new Error(e);
            }
            endGlobalRelabel();
          }
        });

    Lambda<GNode<Node>, Integer> indexer = new Lambda<GNode<Node>, Integer>() {
      @Override
      public Integer call(GNode<Node> x) {
        return x.getData(MethodFlag.NONE).height;
      }
    };

    GaloisRuntime.foreach(initializePreflow(gapYet), new Lambda2Void<GNode<Node>, ForeachContext<GNode<Node>>>() {
      @Override
      public void call(GNode<Node> item, ForeachContext<GNode<Node>> ctx) {
        int increment = 1;
        if (discharge(ctx, gapYet, item)) {
          increment += BETA;
        }

        relabelYet.increment(ctx, increment);
      }
    }, Priority.first(Bucketed.class, numNodes + 1, false, indexer).then(FIFO.class));
  }

  private void setup(String[] args) throws IOException {
    read(new File(args[0]));

    globalRelabelInterval = SystemProperties.getIntProperty("preflowpush.main.globalRelabelInterval", numNodes * ALPHA
        + numEdges);
  }

  private void startGlobalRelabel() {
    globalRelabelId = ThreadTimer.tick();
  }

  private String toStringEdge(GNode<Node> src, GNode<Node> dst) {
    return String.format("(%s[cap: %d]->id: %s[cap: %d])", toStringNode(src), graph.getEdgeData(src, dst),
        toStringNode(dst), graph.getEdgeData(dst, src));
  }

  private String toStringNode(GNode<Node> src) {
    Node node = src.getData();
    return String.format("(id: %d ex: %.0f h: %d)", node, node.excess, node.height);
  }

  private static class Local<N> {
    GNode<N> src;
    int cur;
    boolean finished;
    int minHeight;
    int minEdge;
    int relabelCur;

    public void resetForRelabel() {
      minHeight = Integer.MAX_VALUE;
      minEdge = 0;
      relabelCur = 0;
    }
  }

  private static class Edge {
    private int cap;
    private int ocap;

    public Edge(int ocap) {
      this.cap = ocap;
      this.ocap = ocap;
    }

    public void addOriginalCapacity(int delta) {
      cap += delta;
      ocap += delta;
    }
  }

  private static class Node extends AbstractNoConflictBaseObject {
    private int current;
    private double excess;
    private int height;
    private boolean isSink;
    private boolean isSource;
    private final int id;

    public Node(int id) {
      this.id = id;
      height = 1;
      excess = 0;
      current = 0;
    }

    public void setSink() {
      height = 1;
      isSink = true;
    }

    public void setSource(int numNodes) {
      height = numNodes;
      isSource = true;
    }

    @Override
    public Object gclone() {
      Node n = new Node(id);
      n.current = current;
      n.excess = excess;
      n.height = height;
      n.isSink = isSink;
      n.isSource = isSource;
      return n;
    }

    @Override
    public void restoreFrom(Object copy) {
      Node n = (Node) copy;
      current = n.current;
      excess = n.excess;
      height = n.height;
      isSink = n.isSink;
      isSource = n.isSource;
    }
  }
}
