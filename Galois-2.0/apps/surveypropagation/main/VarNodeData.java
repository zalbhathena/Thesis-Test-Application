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

File: VarNodeData.java 

 */
package surveypropagation.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.Callback;
import galois.runtime.GaloisRuntime;
import galois.runtime.Iteration;
import surveypropagation.main.SurveyPropagationClosures.ComputePiClosure;
import surveypropagation.main.SurveyPropagationClosures.ComputeWiDiffClosure;
import surveypropagation.main.SurveyPropagationClosures.PurgeForzenVariableClosure;

// 
/**
 * The Class VarNodeData.
 */
public class VarNodeData extends NodeData {

  /** The frozen value, -1 if not yet frozen. 0 if frozen to false, 1 if frozen to true. */
  protected int frozenValue; // -1: not yet frozen, 0: frozen to false, 1:
  // frozen to true
  /** The walksat id. */
  protected int walksatId; // walksat works correctly only if the user ids are
  // successive starting from 1
  /** The pi pzero. */
  protected int piPzero;

  /** The pi mzero. */
  protected int piMzero;

  /** The pi p. */
  protected double piP;

  /** The pi m. */
  protected double piM;

  /** The number of successive updates. */
  private long nSu;

  /** The hmax. */
  private double wiZero, hmaxAllA;

  /** The compute PI closure. */
  public ComputePiClosure cpClosure;

  /** The Compute Wi Difference closure. */
  public ComputeWiDiffClosure cwdClosure;

  /** The Purge Frozen Closure closure. */
  public PurgeForzenVariableClosure pfvClosure;

  /**
   * Instantiates a new variable node data instance associated with a graph object.
   *
   * @param g the Graph object with which the node is associated.
   */
  public VarNodeData(Graph<NodeData> g) {
    super(g);
    cpClosure = new ComputePiClosure(g);
    cwdClosure = new ComputeWiDiffClosure(g);
    pfvClosure = new PurgeForzenVariableClosure(g);
    frozenValue = -1;
    nSu = 0;
    walksatId = -1;
  }

  /* (non-Javadoc)
   * @see surveypropagation.main.NodeData#update(galois.objects.graph.ObjectGraph, galois.objects.graph.GNode)
   */
  @Override
  public boolean update(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node) {
    //No reference to this here!
    final VarNodeData d = (VarNodeData) node.getData(MethodFlag.NONE);
    if (d.frozenValue != -1) {
      return true;
    }
    boolean bRemove = false;
    final double wiDiff = computeWiDiff(graph, node);
    computePi(graph, node);
    if (wiDiff != 0) {
      bRemove = computeHMaxAndFreeze(graph, node, wiDiff, d.wiZero, d.hmaxAllA);
      d.tau.getAndIncrement();
      if (d.tau.get() == InputParameters.tau) {
        d.tau.set(0);
        time.getAndIncrement();
        GaloisRuntime.getRuntime().onUndo(Iteration.getCurrentIteration(), new Callback() {
          public void call() {
            d.tau.set(InputParameters.tau - 1);
            time.getAndDecrement();

          }
        });
      }
    }
    node.setData(d, MethodFlag.NONE);
    return bRemove;
  }

  /**
   * Compute pi.
   * Computes the pi for the current node. 
   * @param currEeta the curr eeta
   * @param newEeta the new eeta
   * @param litTypeOfIA the literal type
   */
  protected void computePi(final double currEeta, final double newEeta, final int litTypeOfIA) {
    if (litTypeOfIA == -1) {
      if ((1 - currEeta) > nEps) {
        if ((1 - newEeta) > nEps) {
          piP *= (1 - newEeta) / (1 - currEeta);
        } else {
          piP /= (1 - currEeta);
          piPzero++;
        }
      } else {
        if ((1 - newEeta) > nEps) {
          piP *= (1 - newEeta);
          piPzero--;
        }
      }
    } else {
      if ((1 - currEeta) > nEps) {
        if ((1 - newEeta) > nEps) {
          piM *= (1 - newEeta) / (1 - currEeta);
        } else {
          piM /= (1 - currEeta);
          piMzero++;
        }
      } else {
        if ((1 - newEeta) > nEps) {
          piM *= (1 - newEeta);
          piMzero--;
        }
      }
    }
  }

  /**
   * Compute pi.
   *
   * @param graph the graph
   * @param node the node
   */
  private void computePi(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node) {
    cpClosure.reset();
    node.map(cpClosure, node, MethodFlag.NONE);
    VarNodeData d = (VarNodeData) node.getData(MethodFlag.NONE);
    d.piP = cpClosure.lPiP;
    d.piM = cpClosure.lPiM;
    d.piPzero = cpClosure.lPiPzero;
    d.piMzero = cpClosure.lPiMzero;
    node.setData(d, MethodFlag.NONE);
  }

  /**
   * Compute wi diff.
   *
   * @param graph the graph
   * @param node the node
   * @return the double
   */
  private double computeWiDiff(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node) {
    double accumPosEeta = 1.0, accumNegEeta = 1.0;
    VarNodeData d = (VarNodeData) node.getData(MethodFlag.NONE);
    d.hmaxAllA = 0.0;

    cwdClosure.reset();
    node.map(cwdClosure, node, MethodFlag.NONE);
    cwdClosure.removeMarkedEdges(graph);
    accumPosEeta = cwdClosure.accumPosEeta;
    accumNegEeta = cwdClosure.accumNegEeta;
    d.hmaxAllA = cwdClosure.hmaxAllA;
    final double piPlus = (1 - accumPosEeta) * accumNegEeta;
    final double piMinus = (1 - accumNegEeta) * accumPosEeta;
    final double piZero = accumPosEeta * accumNegEeta;

    if ((piPlus + piMinus + piZero) == 0) {
      // Contradiction arrived; probably UNSAT
      return 0;
    }

    final double wiPlus = piPlus / (piPlus + piMinus + piZero);
    final double wiMinus = piMinus / (piPlus + piMinus + piZero);
    d.wiZero = 1 - wiPlus - wiMinus;
    node.setData(d, MethodFlag.NONE);
    return wiPlus - wiMinus;
  }

  /**
   * Compute h max and freeze.
   *
   * @param graph the graph
   * @param node the node
   * @param wiDiff the wi diff
   * @param wiZero the wi zero
   * @param hmaxAllA the hmax all a
   * @return true, if successful
   */
  private static boolean computeHMaxAndFreeze(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node,
      final double wiDiff, final double wiZero, final double hmaxAllA) {
    double absWiDiff = wiDiff;

    if (absWiDiff < 0) {
      absWiDiff = -wiDiff;
    }

    boolean bRemove = false;
    VarNodeData d = (VarNodeData) node.getData(MethodFlag.NONE);
    double hmaxI = d.hmax;
    if (hmaxAllA < absWiDiff + InputParameters.epsilon) {
      hmaxI = absWiDiff;
      d.nSu++;

      if (d.nSu > InputParameters.nsu) {
        if (wiDiff == 0) {
          // Contradiction arrived; probably UNSAT
        } else {

          d.frozenValue = 0;
          if (wiDiff > 0) {
            d.frozenValue = 1;
          }
        }
        bRemove = true;
      }
    } else {
      double maxMax = hmaxAllA;
      if (hmaxI > maxMax) {
        maxMax = hmaxI;
      }
      hmaxI = maxMax * (1 - InputParameters.delta);
      d.nSu = 0;
    }
    d.hmax = hmaxI;
    node.setData(d, MethodFlag.NONE);

    return bRemove;
  }

  @Override
  public Object gclone()
  {
  	VarNodeData copy  = new VarNodeData(graph);
  	copy.belongsToGraph = belongsToGraph;
  	copy.hmax = hmax;
  	copy.id = id;
  	copy.rid = rid;
  	copy.tau = tau;
  	copy.ownerRef = ownerRef;
  	copy.frozenValue = frozenValue;
  	copy.walksatId = walksatId;
  	copy.piPzero = piPzero;
  	copy.piMzero = piMzero;
  	copy.piP= piP;  	
  	copy.piM = piM;
  	copy.nSu = nSu;
  	copy.wiZero = wiZero;
  	copy.hmaxAllA = hmaxAllA;
  	copy.cpClosure =cpClosure;
  	copy.cwdClosure = cwdClosure;

  	return copy;
  }

  @Override
  public void restoreFrom(Object c)
  {	
	if(true==true)
	throw new UnsupportedOperationException("YAYA");
	VarNodeData copy  = (VarNodeData)c;
  	belongsToGraph = copy.belongsToGraph;
  	hmax = copy.hmax;
  	id = copy.id;
  	rid = copy.rid;
  	tau = copy.tau;
  	ownerRef = copy.ownerRef;
  	frozenValue = copy.frozenValue;
  	walksatId = copy.walksatId;
  	piPzero = copy.piPzero;
  	piMzero = copy.piMzero;
  	piP= copy.piP;  	
  	piM = copy.piM;
  	nSu = copy.nSu;
  	wiZero = copy.wiZero;
  	hmaxAllA = copy.hmaxAllA;
  	cpClosure = copy.cpClosure;
  	cwdClosure = copy.cwdClosure;
  }
}
