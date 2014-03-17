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

File: SurveyPropagationClosures.java 

 */
package surveypropagation.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.ObjectGraph;

import java.util.ArrayList;

import util.Pair;
import util.fn.Lambda2Void;

/**
 * The Class SurveyPropagationClosures.
 * A collection of all the closures used in the survey propagation benchmark.
 */
public class SurveyPropagationClosures {

  /**
   * The Class TouchNeighborhood. Makes the algorithm one-shot.
   */
  public static class TouchNeighborhood implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The ictn. */
    InnerClassTouchNeighborhood ictn = new InnerClassTouchNeighborhood();

    /* 
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      dst.map(ictn, dst);
    }
  }

  /**
   * The Class InnerClassTouchNeighborhood. Does nothing other than traverse over the neighbors.
   */
  public static class InnerClassTouchNeighborhood implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {
    /* 
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {

    }
  }

  /**
   * The Class PurgeClauseClosure, iterates over the neighbors of a clause and 
   * purges them one by one.  
   */
  public static class PurgeClauseClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new purge clause closure associated with a given graph object.
     *
     * @param g the graph.
     */
    public PurgeClauseClosure(Graph<NodeData> g) {
      graph = g;
    }

    /* 
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      if (graph.outNeighborsSize(dst) == 0) {
        VarNodeData v = (VarNodeData) dst.getData(MethodFlag.NONE);
        v.belongsToGraph = false;
        dst.setData(v, MethodFlag.NONE);
        graph.remove(dst, MethodFlag.NONE);
      }
      // Remove self from graph if this is the last iteration.
      if (graph.outNeighborsSize(src) == 0) {
        ClauseNodeData n = (ClauseNodeData) src.getData(MethodFlag.NONE);
        n.belongsToGraph = false;
        src.setData(n, MethodFlag.NONE);
        graph.remove(src, MethodFlag.NONE);
      }
      return;
    }
  };

  /**
   * The Class PurgeForzenVariableClosure is used to purge variables from a factor graph.
   * This is done by traversing over the neighbors of the variable, if the edges match, 
   * the clause is satisfied, and is also marked for removal.
   */
  public static class PurgeForzenVariableClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The to remove. */
    ArrayList<Pair<GNode<NodeData>, GNode<NodeData>>> toRemove;//= new ArrayList<Pair<GNode,GNode>>();

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new purge frozen variable closure associated with the graph.
     *
     * @param g the graph.
     */
    public PurgeForzenVariableClosure(Graph<NodeData> g) {
      toRemove = new ArrayList<Pair<GNode<NodeData>, GNode<NodeData>>>();
      graph = g;
    }

    /* 
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {

      EdgeData eData = (EdgeData) ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      int edgeType = eData.litType;
      int frozenValue = ((VarNodeData) src.getData()).frozenValue;
      ClauseNodeData clauseNodeData = (ClauseNodeData) dst.getData();
      if (((frozenValue == 1) && (edgeType == 1)) || ((frozenValue == 0) && (edgeType == -1))) {
        clauseNodeData.markForRemoval();
        dst.setData(clauseNodeData);
      } else {
        toRemove.add(new Pair<GNode<NodeData>, GNode<NodeData>>(src, dst));
        // check edge count of clause node...
        if (graph.outNeighborsSize(dst) == 0) {
          // Cannot be satisfied since no variables in clause remain
          // un-fixed.         
          System.exit(-1);
        }
      }
      return;
    }

    /**
     * Do remove. Removes the neighbors that were frozen in the application of the closure.
     *
     * @param graph the graph.
     */
    public void doRemove(ObjectGraph<NodeData, EdgeData> graph) {
      for (Pair<GNode<NodeData>, GNode<NodeData>> p : toRemove)
        graph.removeNeighbor(p.getFirst(), p.getSecond());
    }
  };

  /**
   * The Class GetResultClosure, creates a comma separated string representation
   * of each clause.
   */
  public static class GetResultClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The result. */
    protected String result;

    /** The graph. */
    public Graph<NodeData> graph = null;

    /** The curr walksat id. */
    protected int currWalksatId;

    /**
     * Instantiates a new closure to get the results.
     *
     * @param g the graph
     * @param currWalksatIdP the current walksat id
     */
    public GetResultClosure(Graph<NodeData> g, int currWalksatIdP) {
      graph = g;
      result = "";
      currWalksatId = currWalksatIdP;
    }

    /**
     * Gets the walksat id.
     *
     * @return the walksat id
     */
    public int getWalksatId() {
      return currWalksatId;
    }

    /**
     * Gets the result of the closure.
     *
     * @return the result
     */
    public String getResult() {
      return result;
    }

    /* 
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      VarNodeData varNode = (VarNodeData) dst.getData();
      if (varNode.walksatId == -1) {
        varNode.walksatId = ++currWalksatId;
      }
      EdgeData edge = ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      result += edge.litType * varNode.walksatId + ",";
      return;
    }

  }

  /**
   * The Class ComputeEetaClauseFirstClosure.
   * Since computing the Eeta for each clause node requires a two phase traversal of
   * all the connected variables, it is broken into two closures. This is the first of 
   * the two closures. The results from this closure are used by the second closure 
   * {@link #ComputeEetaClauseSecondClosure ComputeEetaClauseSecondClosure} . 
   */
  public static class ComputeEetaClauseFirstClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The allprod. */
    double allprod;// = 1;

    /** The zeroes. */
    int zeroes;// = 0;

    /** The prod. */
    final double[] prod;// = new double[maxNumLits];

    /** The i. */
    int i;

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new compute eeta clause first closure.
     *
     * @param g the graph
     */
    public ComputeEetaClauseFirstClosure(Graph<NodeData> g) {
      graph = g;
      prod = new double[ClauseNodeData.getMaxNumLits()];
      reset();
    }

    /**
     * Resets the closure to initial state, helps in reusing the closure instead of creating new ones.
     */
    public void reset() {
      allprod = 1;
      zeroes = 0;
      i = 0;
    }

    /* (non-Javadoc)
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      final EdgeData edge = ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      final VarNodeData nodeData = ((VarNodeData) dst.getData());
      final EdgeData edgeData = edge;
      final double eeta = edgeData.eeta;

      final double piP = nodeData.piP;
      final double piM = nodeData.piM;
      final double piPzero = nodeData.piPzero;
      final double piMzero = nodeData.piMzero;

      if (edgeData.litType == -1) {
        final double m = (piMzero != 0) ? 0 : piM;
        double p = 0;
        if (piPzero == 0) {
          p = piP / (1 - eeta);
        } else if ((piPzero == 1) && ((1 - eeta) < ClauseNodeData.nEps)) {
          p = piP;
        }

        final double wn = p * (1 - m);
        prod[i] = wn / (wn + m);
      } else {
        final double omeeta = 1 - eeta;
        double pr = 1;
        if (piMzero == 0) {
          if (piPzero == 0) {
            final double wn = piM * (1 - piP) / omeeta;
            pr = wn / (wn + piP);
          }
        } else {
          if ((piMzero != 1) || (omeeta >= ClauseNodeData.nEps)) {
            pr = 0;
          } else {
            if (piPzero == 0) {
              final double wn = piM * (1 - piP);
              pr = wn / (wn + piP);
            }
          }
        }
        prod[i] = pr;
      }

      if (prod[i] < ClauseNodeData.nEps) {
        zeroes++;
        if (zeroes == 2) {
          i++;
          //TODO : how to break from closure.
          return;
        }
      } else {
        allprod *= prod[i];
      }
      i++;
      return;
    }
  }

  /**
   * The Class ComputeEetaClauseSecondClosure.
   * This is the second part of the two-closure method to compute
   * Eeta for each clause. The first part is {@link #ComputeEetaClauseFirstClosure ComputeEetaClauseFirstClosure} 
   */
  public static class ComputeEetaClauseSecondClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The hmax all i. */
    double eps = 0, hmaxAllI = 0.0;

    /** The i. */
    int i;

    /** The prod. */
    double[] prod;

    /** The zeroes. */
    int zeroes;

    /** The allprod. */
    double allprod;

    /** The my c. */
    ComputeEetaClauseFirstClosure myC;

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new compute eeta clause second closure.
     *
     * @param c the c
     */
    public ComputeEetaClauseSecondClosure(ComputeEetaClauseFirstClosure c) {
      i = 0;
      myC = c;
      reset();
    }

    /**
     * Reset, initializes the closure to inital state, helps in reusing the closure instead of creating new ones.
     */
    public void reset()

    {
      eps = 0;
      hmaxAllI = 0.0;
      i = 0;
      prod = myC.prod;
      zeroes = myC.zeroes;
      allprod = myC.allprod;
      graph = myC.graph;
    }

    /* (non-Javadoc)
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      final EdgeData edge = ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      double neweta = 0;
      if (zeroes == 0) {
        neweta = allprod / prod[i];
      } else if ((zeroes == 1) && (prod[i] < ClauseNodeData.nEps)) {
        neweta = allprod;
      }
      final EdgeData edgeData = edge;
      final double eeta = edgeData.eeta;
      final VarNodeData nodeData = (VarNodeData) dst.getData();
      nodeData.computePi(eeta, neweta, edgeData.litType);
      dst.setData(nodeData);
      final double eetaDiff = Math.abs(eeta - neweta);
      if (eps < eetaDiff) {
        eps = eetaDiff;
      }
      edgeData.eeta = neweta;
      final double tmp = nodeData.hmax;
      if (hmaxAllI < tmp) {
        hmaxAllI = tmp;
      }
      i++;
      dst.setData(nodeData);
      return;
    }

  }

  /**
   * The Class ComputePiClosure, computes Pi for each variable. This is done by traversing over
   * the edges and determining the bias of the variable to take a positive value versus a negative
   * value.
   */
  public static class ComputePiClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The l pi m. */
    double lPiP, lPiM;

    /** The l pi mzero. */
    int lPiPzero, lPiMzero;

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new compute pi closure.
     *
     * @param g the graph object.
     */
    public ComputePiClosure(Graph<NodeData> g) {
      graph = g;
      reset();
    }

    /**
     * Reset, initializes the closure to initial state, helps in reusing the closure instead of creating new ones.
     */
    public void reset() {
      lPiP = 1;
      lPiM = 1;
      lPiPzero = 0;
      lPiMzero = 0;
    }

    /* (non-Javadoc)
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      final EdgeData edgeData = ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      final double eeta = edgeData.eeta;

      if (edgeData.litType == -1) {
        if ((1 - eeta) > ClauseNodeData.nEps) {
          lPiP *= (1 - eeta);
        } else {
          lPiPzero++;
        }
      } else {
        if ((1 - eeta) > ClauseNodeData.nEps) {
          lPiM *= (1 - eeta);
        } else {
          lPiMzero++;
        }
      }
      return;
    }
  };

  /**
   * The Class ComputeWiDiffClosure, computes the Wi Difference over each variable.
   */
  public static class ComputeWiDiffClosure implements Lambda2Void<GNode<NodeData>, GNode<NodeData>> {

    /** The accum neg eeta. */
    double accumPosEeta = 1.0, accumNegEeta = 1.0;

    /** The edges to remove. */
    ArrayList<Pair<GNode<NodeData>, GNode<NodeData>>> edgesToRemove;

    /** The hmax all a. */
    double hmaxAllA = 0.0;

    /** The graph. */
    public Graph<NodeData> graph = null;

    /**
     * Instantiates a new compute wi difference closure.
     *
     * @param g the graph
     */
    public ComputeWiDiffClosure(Graph<NodeData> g) {
      graph = g;
      edgesToRemove = new ArrayList<Pair<GNode<NodeData>, GNode<NodeData>>>();
    }

    /**
     * Reset, initializes the closure to initial state, helps in reusing the closure instead of creating new ones.
     */
    public void reset() {
      accumPosEeta = 1.0;
      accumNegEeta = 1.0;
      edgesToRemove.clear();
      hmaxAllA = 0.0;
    }

    /* (non-Javadoc)
     * @see util.fn.Lambda2Void#call(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void call(GNode<NodeData> dst, GNode<NodeData> src) {
      final EdgeData edgeData = ((ObjectGraph<NodeData, EdgeData>) graph).getEdgeData(src, dst);
      final ClauseNodeData clauseNodeData = (ClauseNodeData) dst.getData();
      if (clauseNodeData.isMarkedForRemoval()) {
        edgesToRemove.add(new Pair<GNode<NodeData>, GNode<NodeData>>(src, dst));
      } else {
        final double eeta = edgeData.eeta;

        if (edgeData.litType == 1) {
          accumPosEeta *= (1 - eeta);
        } else {
          accumNegEeta *= (1 - eeta);
        }
        final double tmp = clauseNodeData.hmax;
        if (hmaxAllA < tmp) {
          hmaxAllA = tmp;
        }
      }
      return;
    }

    /**
     * Removes the marked edges.
     *
     * @param graph the graph
     */
    public void removeMarkedEdges(Graph<NodeData> graph) {
      if (edgesToRemove != null) {
        for (Pair<GNode<NodeData>, GNode<NodeData>> e : edgesToRemove) {
          graph.removeNeighbor(e.getFirst(), e.getSecond());
        }
      }
    }
  };
}
