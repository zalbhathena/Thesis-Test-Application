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

File: KEdge.java 

*/



package kruskal.main;

import galois.objects.graph.GNode;
import galois.runtime.Callback;
import galois.runtime.Features;
import galois.runtime.GaloisRuntime;
import galois.runtime.Iteration;
import galois.runtime.Replayable;
import util.UnorderedPair;


/**
 * The Class KEdge represents an undirected graph edge.
 */
public class KEdge extends UnorderedPair<GNode<KNode>> implements Replayable {

  /** The edge weight. */
  final int weight;

  /** The flag indicating whether the edge is in mst. */
  boolean inMST = false;

  /**
   * Instantiates a new k edge.
   *
   * @param first the first
   * @param second the second
   * @param weight the weight
   */
  public KEdge(GNode<KNode> first, GNode<KNode> second, int weight) {
    super(first, second); // unordered pair
    this.weight = weight;

    Features.getReplayFeature().onCreateReplayable(this);
  }

  /**
   * Checks if is in mst.
   *
   * @return true, if is in mst
   */
  public boolean isInMST() {

    return inMST;
  }

  // only setInMST is called in the parallel foreach section
  /**
   * Sets the inMST flag.
   */
  public void setInMST() {
    inMST = true;
    final KEdge e = this;

    if (!GaloisRuntime.getRuntime().inRoot()) {
      // we are in parallel foreach execution
      // in case of abort, edge should be unmarked
      GaloisRuntime.getRuntime().onUndo(Iteration.getCurrentIteration(), new Callback() {

        @Override
        public void call() {
          e.clearInMST();

        }
      });
    }
  }

  /**
   * Clear the inMST flag
   */
  public void clearInMST() {
    inMST = false;
  }

  /**
   *returns the edge weight.
   *
   * @return the edge weight
   */
  public int getWeight() {
    return this.weight;
  }

  /* (non-Javadoc)
   * @see util.UnorderedPair#toString()
   */
  @Override
  public String toString() {
    // return Integer.toString(this.weight);
    return String.format("%x::%d%s", this.hashCode(), this.weight, (this.inMST ? "*" : ""));
  }

  /**
   * Detailed string.
   *
   * @return the detailed info about the edge
   */
  public String detailedString() {
    return String.format("((%s,%s),%d,%s)", this.getFirst(), this.getSecond(), weight, (inMST ? "*" : ""));
  }

  /** The rid. */
  private long rid;

  /* (non-Javadoc)
   * @see galois.runtime.Replayable#getRid()
   */
  @Override
  public long getRid() {
    return rid;
  }

  /* (non-Javadoc)
   * @see galois.runtime.Replayable#setRid(long)
   */
  @Override
  public void setRid(long rid) {
    this.rid = rid;
  }

  /* (non-Javadoc)
   * @see util.UnorderedPair#hashCode()
   */
  @Override
  public int hashCode() {
    // including the weight into hashCode yields better performance
    return this.weight ^ super.hashCode();
  }

  /* (non-Javadoc)
   * @see util.UnorderedPair#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    KEdge that = (KEdge) obj;
    // using weight in the comparison because the hashCode uses it
    return super.equals(that) && this.weight == that.weight;
  }

}
