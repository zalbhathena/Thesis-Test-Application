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

File: KNode.java 

 */

package kruskal.main;

import galois.objects.AbstractBaseObject;
import galois.objects.graph.GNode;

/**
 * The Class KNode represents the data stored at each node of the Graph.
 */
public class KNode extends AbstractBaseObject {
  /** The representative of the component, to which this node belongs. */
  private GNode<KNode> rep = null;

  /** The rank. */
  int rank = 0;

  /** The node in the graph that has this as its node data. */
  private GNode<KNode> myNode = null;

  /**
   * Instantiates a new k node.
   */
  public KNode() {
    this(null, null, 0);
  }

  /**
   * Instantiates a new k node.
   * copy constructor
   *
   * @param that the that
   */
  public KNode(KNode that) {
    this(that.myNode, that.rep, that.rank);
  }

  /**
   * Instantiates a new k node.
   *
   * @param myNode the my node
   * @param rep the rep
   * @param rank the rank
   */
  public KNode(GNode<KNode> myNode, GNode<KNode> rep, int rank) {
    this.myNode = myNode;
    this.rep = rep;
    this.rank = rank;
  }

  /**
   * Inc rank.
   */
  public void incRank() {
    this.rank += 1;
  }

  /**
   * Gets the rank.
   *
   * @return the rank
   */
  public int getRank() {
    return this.rank;
  }

  /**
   * Make set.
   *
   * @param myNode the corresponding GNode in the graph, so that graph.getData(myNode) == this
   * equivalent to initializing rep to this
   * 
   * Necessary for proper initialization
   */
  public void makeSet(GNode<KNode> myNode) {
    this.rep = myNode;
    this.myNode = myNode;
  }

  /**
   * Gets the rep.
   *
   * @return the rep
   */
  public GNode<KNode> getRep() {
    return rep;
  }

  /**
   * Sets the rep.
   *
   * @param rep the new rep
   */
  public void setRep(GNode<KNode> rep) {
    this.rep = rep;
  }

  @Override
  public String toString() {
    return String.format("(%x,%x,%d)", myNode.hashCode(), this.rep.hashCode(), this.rank);
  }

  @Override
  public Object gclone() {
    KNode copy = new KNode();
    copy.rank = this.rank;
    copy.rep = this.rep;
    return copy;
  }

  @Override
  public void restoreFrom(Object copy) {
    KNode that = (KNode) copy;
    this.rank = that.rank;
    this.rep = that.rep;
  }
}
