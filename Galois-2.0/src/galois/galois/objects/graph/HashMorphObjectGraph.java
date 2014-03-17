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

File: HashMorphObjectGraph.java 

 */

package galois.objects.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import fn.Lambda2Void;
import fn.Lambda3Void;
import fn.LambdaVoid;
import galois.objects.GObject;
import galois.objects.MethodFlag;
import galois.runtime.IterationAbortException;
import galois.runtime.MapInternalContext;

final class HashMorphObjectGraph<N extends GObject, E> implements ObjectGraph<N, E> {
  private final AtomicReference<LinkedNode> head;
  private final AtomicInteger size;
  protected final boolean isDirected;

  // XXX(ddn): Vulnerable to ABAB problem when we wrap integers
  private int mapVersionNumber = 1;
  private static final int chunkSize = 64;

  HashMorphObjectGraph() {
    this(true);
  }

  HashMorphObjectGraph(boolean isDirected) {
    this.isDirected = isDirected;
    head = new AtomicReference<LinkedNode>();
    size = new AtomicInteger(0);
  }

  @SuppressWarnings("unchecked")
  private EdgeGraphNode downcast(GNode n) {
    return (EdgeGraphNode) n;
  }

  @Override
  public GNode<N> createNode(final N n) {
    return createNode(n, MethodFlag.ALL);
  }

  @Override
  public GNode<N> createNode(final N n, byte flags) {
    GNode<N> ret = new EdgeGraphNode(n, isDirected);
    ObjectGraphLocker.createNodeEpilog(ret, flags);
    return ret;
  }

  @Override
  public boolean add(GNode<N> src) {
    return add(src, MethodFlag.ALL);
  }

  @Override
  public boolean add(GNode<N> src, byte flags) {
    ObjectGraphLocker.addNodeProlog(src, flags);
    EdgeGraphNode gsrc = downcast(src);
    if (gsrc.add(this)) {
      size.incrementAndGet();
      ObjectGraphLocker.addNodeEpilog(this, src, flags);
      return true;
    }
    return false;
  }

  @Override
  public boolean remove(GNode<N> src) {
    return remove(src, MethodFlag.ALL);
  }

  @Override
  public boolean remove(GNode<N> src, byte flags) {
    // grab a lock on src if needed
    if (!contains(src, flags)) {
      return false;
    }
    // grab a lock on the neighbors + store undo information if needed
    ObjectGraphLocker.removeNodeProlog(this, src, flags);
    size.decrementAndGet();
    EdgeGraphNode gsrc = downcast(src);
    for (EdgeGraphNode gdst : gsrc.outEdges.keySet()) {
      assert gdst.inEdges.containsKey(gsrc);
      // if source == destination (self-edge), and outEdges == inEdges
      // (undirected),
      // we would get a concurrent modification exception
      if (gdst != gsrc || isDirected) {
        gdst.inEdges.remove(gsrc);
      }
    }
    if (isDirected) {
      for (EdgeGraphNode s : gsrc.inEdges.keySet()) {
        assert s.outEdges.containsKey(gsrc);
        s.outEdges.remove(gsrc);
      }
      gsrc.outEdges.clear();
    }
    gsrc.inEdges.clear();
    boolean ret = gsrc.remove(this);
    // has to be there, because containsNode returned true and we have the lock
    // on the node
    assert ret;
    return true;
  }

  @Override
  public boolean contains(GNode<N> src) {
    return contains(src, MethodFlag.ALL);
  }

  @Override
  public boolean contains(GNode<N> src, byte flags) {
    ObjectGraphLocker.containsNodeProlog(src, flags);
    EdgeGraphNode gsrc = downcast(src);
    return gsrc.inGraph(this);
  }

  @Override
  public int size() {
    return size(MethodFlag.ALL);
  }

  @Override
  public int size(byte flags) {
    ObjectGraphLocker.sizeProlog(flags);
    int ret = size.get();
    assert ret >= 0;
    return ret;
  }

  @Override
  public boolean addNeighbor(GNode<N> src, GNode<N> dst) {
    throw new UnsupportedOperationException("addNeighbor not supported in EdgeGraphs. Use createEdge/addEdge instead");
  }

  @Override
  public boolean addNeighbor(GNode<N> src, GNode<N> dst, byte flags) {
    throw new UnsupportedOperationException("addNeighbor not supported in EdgeGraphs. Use createEdge/addEdge instead");
  }

  @Override
  public boolean removeNeighbor(GNode<N> src, GNode<N> dst) {
    return removeNeighbor(src, dst, MethodFlag.ALL);
  }

  @Override
  public boolean removeNeighbor(GNode<N> src, GNode<N> dst, byte flags) {
    ObjectGraphLocker.removeNeighborProlog(src, dst, flags);
    EdgeGraphNode gsrc = downcast(src);
    EdgeGraphNode gdst = downcast(dst);
    boolean ret = gsrc.outEdges.containsKey(gdst);
    if (ret) {
      E edgeData = gsrc.outEdges.remove(gdst);
      E sameEdgeData = gdst.inEdges.remove(gsrc);
      assert edgeData == sameEdgeData || (gsrc == gdst && !isDirected);
      assert isDirected || gsrc.outEdges.size() == gsrc.inEdges.size();
      ObjectGraphLocker.removeNeighborEpilog(this, src, dst, edgeData, flags);
    }
    return ret;
  }

  @Override
  public boolean hasNeighbor(GNode<N> src, GNode<N> dst) {
    return hasNeighbor(src, dst, MethodFlag.ALL);
  }

  @Override
  public boolean hasNeighbor(GNode<N> src, GNode<N> dst, byte flags) {
    ObjectGraphLocker.hasNeighborProlog(src, dst, flags);
    EdgeGraphNode gsrc = downcast(src);
    EdgeGraphNode gdst = downcast(dst);
    boolean ret = gsrc.outEdges.containsKey(gdst);
    assert ret == gdst.inEdges.containsKey(gsrc);
    return ret;
  }

  @Override
  public void mapInNeighbors(GNode<N> src, LambdaVoid<GNode<N>> lambda) {
    mapInNeighbors(src, lambda, MethodFlag.ALL);
  }

  @Override
  public void mapInNeighbors(GNode<N> src, LambdaVoid<GNode<N>> lambda, byte flags) {
    ObjectGraphLocker.mapInNeighborsProlog(this, src, flags);
    EdgeGraphNode gsrc = downcast(src);
    for (EdgeGraphNode node : gsrc.inEdges.keySet()) {
      lambda.call(node);
    }
  }

  @Override
  public int inNeighborsSize(GNode<N> src) {
    return inNeighborsSize(src, MethodFlag.ALL);
  }

  @Override
  public int inNeighborsSize(GNode<N> src, byte flags) {
    ObjectGraphLocker.inNeighborsSizeProlog(this, src, flags);
    EdgeGraphNode gsrc = downcast(src);
    return gsrc.inEdges.size();
  }

  @Override
  public int outNeighborsSize(GNode<N> src) {
    return outNeighborsSize(src, MethodFlag.ALL);
  }

  @Override
  public int outNeighborsSize(GNode<N> src, byte flags) {
    ObjectGraphLocker.outNeighborsSizeProlog(src, flags);
    EdgeGraphNode gsrc = downcast(src);
    return gsrc.outEdges.size();
  }

  @Override
  public boolean addEdge(GNode<N> src, GNode<N> dst, E data) {
    return addEdge(src, dst, data, MethodFlag.ALL);
  }

  @Override
  public boolean addEdge(GNode<N> src, GNode<N> dst, final E data, byte flags) {
    ObjectGraphLocker.addEdgeProlog(src, dst, flags);
    EdgeGraphNode gsrc = downcast(src);
    EdgeGraphNode gdst = downcast(dst);
    boolean ret = gsrc.outEdges.containsKey(gdst);
    // if the edge is already there, do not allow overwriting of data (use
    // setEdgeData instead)
    if (!ret) {
      E oldData = gsrc.outEdges.put(gdst, data);
      E sameOldData = gdst.inEdges.put(gsrc, data);
      assert sameOldData == oldData || (!isDirected && gsrc == gdst) : String.format("%s %s %s %s %s %s", sameOldData,
          oldData, isDirected, gsrc, gdst.getRid(), gsrc.getRid());
      assert isDirected || gsrc.outEdges.size() == gsrc.inEdges.size();
      ObjectGraphLocker.addEdgeEpilog(this, src, dst, flags);
      return true;
    }
    return false;
  }

  @Override
  public E getEdgeData(GNode<N> src, GNode<N> dst) {
    return getEdgeData(src, dst, MethodFlag.ALL);
  }

  @Override
  public E getEdgeData(GNode<N> src, GNode<N> dst, byte flags) {
    return getEdgeData(src, dst, flags, flags);
  }

  @Override
  public E getEdgeData(GNode<N> src, GNode<N> dst, byte edgeFlags, byte dataFlags) {
    ObjectGraphLocker.getEdgeDataProlog(src, dst, edgeFlags);
    EdgeGraphNode gsrc = downcast(src);
    EdgeGraphNode gdst = downcast(dst);
    E ret = gsrc.outEdges.get(gdst);
    ObjectGraphLocker.getEdgeDataEpilog(ret, dataFlags);
    return ret;
  }

  @Override
  public E setEdgeData(GNode<N> src, GNode<N> dst, E d) {
    return setEdgeData(src, dst, d, MethodFlag.ALL);
  }

  @Override
  public E setEdgeData(GNode<N> src, GNode<N> dst, final E data, byte flags) {
    ObjectGraphLocker.setEdgeDataProlog(src, dst, flags);
    EdgeGraphNode gsrc = downcast(src);
    EdgeGraphNode gdst = downcast(dst);
    if (!gsrc.outEdges.containsKey(gdst)) {
      return null;
    }
    E oldData = gsrc.outEdges.put(gdst, data);
    // fast check to avoid redundant work
    if (oldData != data) {
      if (gsrc == gdst && !isDirected) {
        ; // skip useless inEdge.put
      } else {
        E otherOldData = gdst.inEdges.put(gsrc, data);
        assert oldData == otherOldData;
      }
      ObjectGraphLocker.setEdgeDataEpilog(this, src, dst, oldData, flags);
    }

    return oldData;
  }

  @Override
  public boolean isDirected() {
    return isDirected;
  }

  private boolean tryMark(EdgeGraphNode curr) {
    int old = curr.iterateVersion.get();
    if (old == mapVersionNumber) {
      return false;
    }

    return curr.iterateVersion.compareAndSet(old, mapVersionNumber);
  }

  private EdgeGraphNode scanForNode(LinkedNode start) {
    while (start != null) {
      if (start.isDummy()) {
        start = start.getNext();
      } else {
        return (EdgeGraphNode) start;
      }
    }
    return null;
  }

  @Override
  public void mapInternal(LambdaVoid<GNode<N>> body, MapInternalContext ctx) {
    EdgeGraphNode lastSuccess = null;
    EdgeGraphNode begin = scanForNode(head.get());
    int[] holder = new int[1];

    while (true) {
      if (begin == null) {
        break;
      }

      boolean owned = false;
      if (tryMark(begin)) {
        owned = true;
        if (lastSuccess != null) {
          lastSuccess.iterateNext.set(begin, mapVersionNumber);
        }
        lastSuccess = begin;
      } else {
        EdgeGraphNode next = begin.iterateNext.get(holder);
        if (holder[0] == mapVersionNumber) {
          begin = next;
          continue;
        }
      }

      EdgeGraphNode cur = begin;

      for (int i = 0; i < chunkSize; i++) {
        if (owned) {
          while (true) {
            try {
              // Help out GC
              cur.iterateNext.set(null, 0);
              ctx.begin();
              body.call(cur);
              ctx.commit(cur);
              if (i != 0)
                cur.iterateVersion.set(0);
              break;
            } catch (IterationAbortException e) {
              ctx.abort();
            }
          }
        }

        cur = scanForNode(cur.next);
        if (cur == null) {
          break;
        }
      }

      begin = cur;
    }
  }

  @Override
  public <A1> void mapInternal(Lambda2Void<GNode<N>, A1> body, MapInternalContext ctx, A1 arg1) {
    EdgeGraphNode lastSuccess = null;
    EdgeGraphNode begin = scanForNode(head.get());
    int[] holder = new int[1];

    while (true) {
      if (begin == null) {
        break;
      }

      boolean owned = false;
      if (tryMark(begin)) {
        owned = true;
        if (lastSuccess != null) {
          lastSuccess.iterateNext.set(begin, mapVersionNumber);
        }
        lastSuccess = begin;
      } else {
        EdgeGraphNode next = begin.iterateNext.get(holder);
        if (holder[0] == mapVersionNumber) {
          begin = next;
          continue;
        }
      }

      EdgeGraphNode cur = begin;

      for (int i = 0; i < chunkSize; i++) {
        if (owned) {
          while (true) {
            try {
              // Help out GC
              cur.iterateNext.set(null, 0);
              ctx.begin();
              body.call(cur, arg1);
              ctx.commit(cur);
              if (i != 0)
                cur.iterateVersion.set(0);
              break;
            } catch (IterationAbortException e) {
              ctx.abort();
            }
          }
        }

        cur = scanForNode(cur.next);
        if (cur == null) {
          break;
        }
      }

      begin = cur;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public <A1, A2> void mapInternal(Lambda3Void<GNode<N>, A1, A2> body, MapInternalContext ctx, A1 arg1, A2 arg2) {
    EdgeGraphNode lastSuccess = null;
    EdgeGraphNode begin = scanForNode(head.get());
    int[] holder = new int[1];

    while (true) {
      if (begin == null) {
        break;
      }

      boolean owned = false;
      if (tryMark(begin)) {
        owned = true;
        if (lastSuccess != null) {
          lastSuccess.iterateNext.set(begin, mapVersionNumber);
        }
        lastSuccess = begin;
      } else {
        EdgeGraphNode next = begin.iterateNext.get(holder);
        if (holder[0] == mapVersionNumber) {
          begin = next;
          continue;
        }
      }

      EdgeGraphNode cur = begin;

      for (int i = 0; i < chunkSize; i++) {
        if (owned) {
          while (true) {
            try {
              // Help out GC
              cur.iterateNext.set(null, 0);
              ctx.begin();
              body.call(cur, arg1, arg2);
              ctx.commit(cur);
              if (i != 0)
                cur.iterateVersion.set(0);
              break;
            } catch (IterationAbortException e) {
              ctx.abort();
            }
          }
        }

        cur = scanForNode(cur.next);
        if (cur == null) {
          break;
        }
      }

      begin = cur;
    }
  }

  @Override
  public void map(LambdaVoid<GNode<N>> body) {
    map(body, MethodFlag.ALL);
  }

  @Override
  public void map(LambdaVoid<GNode<N>> body, byte flags) {
    ObjectGraphLocker.mapProlog(flags);
    LinkedNode curr = head.get();
    while (curr != null) {
      if (!curr.isDummy()) {
        EdgeGraphNode gsrc = (EdgeGraphNode) curr;
        assert gsrc.in;
        body.call(gsrc);
      }
      curr = curr.getNext();
    }
  }

  @Override
  public <A1> void map(Lambda2Void<GNode<N>, A1> body, A1 arg1) {
    map(body, arg1, MethodFlag.ALL);
  }

  @Override
  public <A1> void map(Lambda2Void<GNode<N>, A1> body, A1 arg1, byte flags) {
    ObjectGraphLocker.mapProlog(flags);
    LinkedNode curr = head.get();
    while (curr != null) {
      if (!curr.isDummy()) {
        EdgeGraphNode gsrc = (EdgeGraphNode) curr;
        assert gsrc.in;
        body.call(gsrc, arg1);
      }
      curr = curr.getNext();
    }
  }

  @Override
  public <A1, A2> void map(Lambda3Void<GNode<N>, A1, A2> body, A1 arg1, A2 arg2) {
    map(body, arg1, arg2, MethodFlag.ALL);
  }

  @Override
  public <A1, A2> void map(Lambda3Void<GNode<N>, A1, A2> body, A1 arg1, A2 arg2, byte flags) {
    ObjectGraphLocker.mapProlog(flags);
    LinkedNode curr = head.get();
    while (curr != null) {
      if (!curr.isDummy()) {
        EdgeGraphNode gsrc = (EdgeGraphNode) curr;
        assert gsrc.in;
        body.call(gsrc, arg1, arg2);
      }
      curr = curr.getNext();
    }
  }

  @Override
  public void mapInternalDone() {
    if (++mapVersionNumber == 0) {
      mapVersionNumber = 1;
    }
  }

  private static interface LinkedNode {
    public void setNext(LinkedNode next);

    public LinkedNode getNext();

    public boolean isDummy();
  }

  private static class DummyLinkedNode implements LinkedNode {
    private LinkedNode next;

    @Override
    public void setNext(LinkedNode next) {
      this.next = next;
    }

    @Override
    public LinkedNode getNext() {
      return next;
    }

    @Override
    public boolean isDummy() {
      return true;
    }
  }

  private final class EdgeGraphNode extends ConcurrentGNode<N> implements LinkedNode {
    private final AtomicStampedReference<EdgeGraphNode> iterateNext = new AtomicStampedReference<EdgeGraphNode>(null, 0);
    private final AtomicInteger iterateVersion = new AtomicInteger();
    private final Map<EdgeGraphNode, E> inEdges;
    private final Map<EdgeGraphNode, E> outEdges;
    protected N data;
    private boolean in;
    private LinkedNode dummy;
    private LinkedNode next;
    private static final int NUM_NEIGHBORS = 8;

    private EdgeGraphNode(N d, boolean isDirected) {
      outEdges = new HashMap<EdgeGraphNode, E>(NUM_NEIGHBORS);
      if (isDirected) {
        inEdges = new HashMap<EdgeGraphNode, E>(NUM_NEIGHBORS);
      } else {
        inEdges = outEdges;
      }
      data = d;
    }

    private boolean inGraph(HashMorphObjectGraph<N, E> g) {
      return HashMorphObjectGraph.this == g && in;
    }

    private boolean add(HashMorphObjectGraph<N, E> g) {
      if (HashMorphObjectGraph.this != g) {
        // XXX(ddn): Nodes could belong to more than 1 graph, but since
        // this rarely happens in practice, simplify implementation
        // assuming that this doesn't occur
        throw new IllegalArgumentException("cannot add nodes created by a different graph");
      }

      if (!in) {
        in = true;
        dummy = new DummyLinkedNode();
        dummy.setNext(this);

        LinkedNode currHead;
        do {
          currHead = head.get();
          next = currHead;
        } while (!head.compareAndSet(currHead, dummy));
        return true;
      }
      return false;
    }

    private boolean remove(HashMorphObjectGraph<N, E> g) {
      if (inGraph(g)) {
        in = false;
        iterateNext.set(null, 0);
        iterateVersion.set(0);
        dummy.setNext(next);
        return true;
      }
      return false;
    }

    @Override
    public boolean isDummy() {
      return false;
    }

    @Override
    public LinkedNode getNext() {
      return next;
    }

    @Override
    public void setNext(LinkedNode next) {
      this.next = next;
    }

    @Override
    public N getData() {
      return getData(MethodFlag.ALL);
    }

    @Override
    public N getData(byte flags) {
      return getData(flags, flags);
    }

    @Override
    public N getData(byte nodeFlags, byte dataFlags) {
      ObjectGraphLocker.getNodeDataProlog(this, nodeFlags);
      N ret = this.data;
      ObjectGraphLocker.getNodeDataEpilog(ret, dataFlags);
      return ret;
    }

    @Override
    public N setData(N data) {
      return setData(data, MethodFlag.ALL);
    }

    @Override
    public N setData(N data, byte flags) {
      ObjectGraphLocker.setNodeDataProlog(this, flags);
      N oldData = this.data;
      // fast check to avoid redundant calls to the CM
      if (oldData != data) {
        this.data = data;
        ObjectGraphLocker.setNodeDataEpilog(this, oldData, flags);
      }
      return oldData;
    }

    @Override
    public void map(LambdaVoid<GNode<N>> body) {
      map(body, MethodFlag.ALL);
    }

    @Override
    public void mapInternal(LambdaVoid<GNode<N>> body, MapInternalContext ctx) {
      for (EdgeGraphNode node : outEdges.keySet()) {
        if (tryMark(node)) {
          while (true) {
            try {
              ctx.begin();
              body.call(node);
              ctx.commit(node);
              break;
            } catch (IterationAbortException _) {
              ctx.abort();
            }
          }
        }
      }
    }

    @Override
    public <A1> void mapInternal(Lambda2Void<GNode<N>, A1> body, MapInternalContext ctx, A1 arg1) {
      for (EdgeGraphNode node : outEdges.keySet()) {
        if (tryMark(node)) {
          while (true) {
            try {
              ctx.begin();
              body.call(node, arg1);
              ctx.commit(node);
              break;
            } catch (IterationAbortException _) {
              ctx.abort();
            }
          }
        }
      }
    }

    @Override
    public <A1, A2> void mapInternal(Lambda3Void<GNode<N>, A1, A2> body, MapInternalContext ctx, A1 arg1, A2 arg2) {
      for (EdgeGraphNode node : outEdges.keySet()) {
        if (tryMark(node)) {
          while (true) {
            try {
              ctx.begin();
              body.call(node, arg1, arg2);
              ctx.commit(node);
              break;
            } catch (IterationAbortException _) {
              ctx.abort();
            }
          }
        }
      }
    }

    @Override
    public void mapInternalDone() {
      if (++mapVersionNumber == 0) {
        mapVersionNumber = 1;
      }
    }

    @Override
    public void map(LambdaVoid<GNode<N>> body, byte flags) {
      ObjectGraphLocker.mapOutNeighborsProlog(this, flags);
      for (EdgeGraphNode node : outEdges.keySet()) {
        body.call(node);
      }
    }

    @Override
    public <A1> void map(Lambda2Void<GNode<N>, A1> body, A1 arg1) {
      map(body, arg1, MethodFlag.ALL);
    }

    @Override
    public <A1> void map(Lambda2Void<GNode<N>, A1> body, A1 arg1, byte flags) {
      ObjectGraphLocker.mapOutNeighborsProlog(this, flags);
      for (EdgeGraphNode node : outEdges.keySet()) {
        body.call(node, arg1);
      }
    }

    @Override
    public <A1, A2> void map(Lambda3Void<GNode<N>, A1, A2> body, A1 arg1, A2 arg2) {
      map(body, arg1, arg2, MethodFlag.ALL);
    }

    @Override
    public <A1, A2> void map(Lambda3Void<GNode<N>, A1, A2> body, A1 arg1, A2 arg2, byte flags) {
      ObjectGraphLocker.mapOutNeighborsProlog(this, flags);
      for (EdgeGraphNode node : outEdges.keySet()) {
        body.call(node, arg1, arg2);
      }
    }
  }

  @Override
  public void access(byte flags) {
  }
}
