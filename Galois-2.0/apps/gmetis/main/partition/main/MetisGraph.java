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

File: MetisGraph.java 

 */

package partition.main;

import fn.Lambda2Void;
import fn.LambdaVoid;
import galois.objects.Accumulator;
import galois.objects.AccumulatorBuilder;
import galois.objects.GMutableInteger;
import galois.objects.GMutableIntegerBuilder;
import galois.objects.GSet;
import galois.objects.GSetBuilder;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.IntGraph;

import java.util.Set;

/**
 * a wrapper for the graph to contain the data of the graph 
 */
public class MetisGraph {

  private GMutableInteger[] partWeights;
  private Accumulator mincut;
  private Accumulator numEdges;
  private MetisGraph finerGraph;
  private IntGraph<MetisNode> graph;
  private GSet<GNode<MetisNode>> boundaryNodes;
  static int nparts;

  public MetisGraph() {
  	AccumulatorBuilder accuBuilder=new AccumulatorBuilder();
    mincut = accuBuilder.create();
    numEdges =accuBuilder.create();
    boundaryNodes =new GSetBuilder<GNode<MetisNode>>().create();
  }

  /**
   * add weight to the weight of a partition
   * @param index the index of the partition
   * @param weight the weight to increase
   */
  public void incPartWeight(int index, int weight) {
    int oldWeight = partWeights[index].get(MethodFlag.NONE);
    partWeights[index].set(oldWeight + weight, MethodFlag.NONE);
  }

  /**
   * initialize the partition weights variable
   */
  public void initPartWeight() {
    if (partWeights == null) {
      partWeights = new GMutableInteger[nparts];
      GMutableIntegerBuilder intBuilder=new GMutableIntegerBuilder();
      for (int i = 0; i < partWeights.length; i++) {
        partWeights[i] = intBuilder.create();
      }

    }
  }

  /**
   * Set the weight of a partition
   * @param index the index of the partition
   * @param weight the weight to set
   */
  public void setPartWeight(int index, int weight) {
    partWeights[index].set(weight, MethodFlag.NONE);
  }

  /**
   * get the weight of a partition
   * @param part the index of the partition
   * @return the weight of a partition
   */
  public int getPartWeight(int part, byte flags) {
    return partWeights[part].get(flags);
  }

  /**
   * get the weight of a partition, no flag version, used for serial
   * @param part the index of the partition
   * @return the weight of a partition
   */
  public int getPartWeight(int part) {
    return partWeights[part].get();
  }

  /**
   * increase the num of edges by 1 in the graph
   */
  public void incNumEdges() {
    numEdges.add(1, MethodFlag.NONE);
  }

  /**
   * return the num of edges in the graph
   */
  public int getNumEdges() {
      return numEdges.get();
  }

  public void setNumEdges(int num) {
      numEdges.set(num);
  }

  /**
   * compute the parameters for two-way refining
   */
  public void computeTwoWayPartitionParams() {
    partWeights = new GMutableInteger[2];
    GMutableIntegerBuilder builder=new GMutableIntegerBuilder();
    for (int i = 0; i < partWeights.length; i++) {
      partWeights[i] = builder.create(MethodFlag.NONE);
    }

    unsetAllBoundaryNodes();
    ComputeTwoWayPartitionParamsClosure closure = new ComputeTwoWayPartitionParamsClosure();
    graph.map(closure, MethodFlag.NONE);
    mincut.set(closure.getMinCut() / 2);
  }

  class ComputeTwoWayPartitionParamsClosure implements LambdaVoid<GNode<MetisNode>> {
    int mincut;

    public ComputeTwoWayPartitionParamsClosure() {
      mincut = 0;
    }

    @Override
    public void call(GNode<MetisNode> node) {
      MetisNode nodeData = node.getData(MethodFlag.NONE);
      int me = nodeData.getPartition();
      partWeights[me].set(partWeights[me].get() + nodeData.getWeight(), MethodFlag.NONE);
      updateNodeEdAndId(node);
      if (nodeData.getEdegree() > 0 || graph.outNeighborsSize(node, MethodFlag.NONE) == 0) {
        mincut += nodeData.getEdegree();
        setBoundaryNode(node);
      }
    }

    public int getMinCut() {
      return mincut;
    }
  }

  /**
   * get the maximal adjsum(the sum of the outgoing edge weights of a node) among all the nodes
   */
  public int getMaxAdjSum() {
    MaxAdjSumClosure closure = new MaxAdjSumClosure();
    graph.map(closure, MethodFlag.NONE);
    return closure.getMaxAdjSum();
  }

  class MaxAdjSumClosure implements LambdaVoid<GNode<MetisNode>> {
    int maxAdjSum;

    public MaxAdjSumClosure() {
      maxAdjSum = -1;
    }

    @Override
    public void call(GNode<MetisNode> node) {
      int adjwgtsum = node.getData(MethodFlag.NONE).getAdjWgtSum();
      if (maxAdjSum < adjwgtsum) {
        maxAdjSum = adjwgtsum;
      }
    }

    public int getMaxAdjSum() {
      return maxAdjSum;
    }
  }

  /**
   * compute the parameters for kway refining
   */
  public void computeKWayPartitionParams(int nparts) {
    unsetAllBoundaryNodes();
    GMutableIntegerBuilder intBuilder=new GMutableIntegerBuilder();
    partWeights = new GMutableInteger[nparts];
    for (int i = 0; i < partWeights.length; i++) {
      partWeights[i] = intBuilder.create();
    }
    ComputeKWayPartitionParamsClosure closure = new ComputeKWayPartitionParamsClosure();
    graph.map(closure, MethodFlag.NONE);
    setMinCut(closure.getMinCut() / 2);
  }

  class ComputeKWayPartitionParamsClosure implements LambdaVoid<GNode<MetisNode>> {
    int mincut;

    @Override
    public void call(GNode<MetisNode> node) {
      final MetisNode nodeData = node.getData(MethodFlag.NONE);
      final int me = nodeData.getPartition();
      partWeights[me].set(partWeights[me].get() + nodeData.getWeight(), MethodFlag.NONE);
      updateNodeEdAndId(node);
      if (nodeData.getEdegree() > 0) {
        mincut += nodeData.getEdegree();
        int numEdges = graph.outNeighborsSize(node, MethodFlag.NONE);
        nodeData.partIndex = new int[numEdges];
        nodeData.partEd = new int[numEdges];
        node.map(new Lambda2Void<GNode<MetisNode>, GNode<MetisNode>>() {
          public void call(GNode<MetisNode> neighbor, GNode<MetisNode> node) {
            MetisNode neighborData = neighbor.getData(MethodFlag.NONE);
            if (me != neighborData.getPartition()) {
              int edgeWeight = (int) graph.getEdgeData(node, neighbor, MethodFlag.NONE);
              int k = 0;
              for (; k < nodeData.getNDegrees(); k++) {
                if (nodeData.partIndex[k] == neighborData.getPartition()) {
                  nodeData.partEd[k] += edgeWeight;
                  break;
                }
              }
              if (k == nodeData.getNDegrees()) {
                nodeData.partIndex[nodeData.getNDegrees()] = neighborData.getPartition();
                nodeData.partEd[nodeData.getNDegrees()] = edgeWeight;
                nodeData.setNDegrees(nodeData.getNDegrees() + 1);
              }
            }
          }
        }, node, MethodFlag.NONE);
      }
      if (nodeData.getEdegree() - nodeData.getIdegree() > 0) {
        setBoundaryNode(node);
      }
      return;
    }

    public int getMinCut() {
      return mincut;
    }
  }

  /**
   * update the external and internal degree for every node in the graph
   */
  public void updateNodeEdAndId(GNode<MetisNode> n) {
    ComputeEdAndIdClosure closure = new ComputeEdAndIdClosure();
    n.map(closure, n, MethodFlag.NONE);
    MetisNode nodeData = n.getData(MethodFlag.NONE);
    nodeData.setEdegree(closure.getEd());
    nodeData.setIdegree(closure.getId());
  }

  class ComputeEdAndIdClosure implements Lambda2Void<GNode<MetisNode>, GNode<MetisNode>> {
    int ed, id;

    @Override
    public void call(GNode<MetisNode> neighbor, GNode<MetisNode> node) {
      int weight = (int) graph.getEdgeData(node, neighbor, MethodFlag.NONE);
      if (node.getData(MethodFlag.NONE).getPartition() != neighbor.getData(MethodFlag.NONE).getPartition()) {
        ed = ed + weight;
      } else {
        id = id + weight;
      }
    }

    public int getEd() {
      return ed;
    }

    public int getId() {
      return id;
    }
  }

  /**
   * return the intgraph in the wrapper
   */
  public IntGraph<MetisNode> getGraph() {
    return graph;
  }

  /**
   * set the graph for the wrapper
   */
  public void setGraph(IntGraph<MetisNode> graph) {
    this.graph = graph;
  }

  /**
   * return the finer metisGraph
   */
  public MetisGraph getFinerGraph() {
    return finerGraph;
  }

  /**
   * set the finer metisGraph
   */
  public void setFinerGraph(MetisGraph finer) {
    finerGraph = finer;
  }

  /**
   * return the graphcut
   */
  public int getMinCut() {
    int retval = 0;
    retval = mincut.get();

    return retval;
  }

  /**
   * set the graphcut
   */
  public void setMinCut(int cut) {
    mincut.set(cut);
  }

  /**
   * increase the graphcut
   */
  public void incMinCut(int cut) {
    mincut.add(cut, MethodFlag.NONE);
  }

  //methods for dealing with boundary nodes

  /**
   * return the number of boundary nodes in the graph
   */
  public int getNumOfBoundaryNodes() {
    return boundaryNodes.size();
  }

  /**
   * mark a node as a boundary node
   */
  public void setBoundaryNode(GNode<MetisNode> node) {
    node.getData(MethodFlag.NONE).setBoundary(true);
    boundaryNodes.add(node, MethodFlag.NONE);
  }

  /**
   * unmark a boundary nodes
   */
  public void unsetBoundaryNode(GNode<MetisNode> node) {
    node.getData(MethodFlag.NONE).setBoundary(false);
    boundaryNodes.remove(node, MethodFlag.NONE);
  }

  /**
   * unmark all the boundary nodes
   */
  public void unsetAllBoundaryNodes() {
    for (GNode<MetisNode> node : boundaryNodes) {
      node.getData(MethodFlag.NONE).setBoundary(false);
    }
    boundaryNodes.clear(MethodFlag.NONE);
  }

  /**
   * return the set of boundary nodes
   */
  public Set<GNode<MetisNode>> getBoundaryNodes() {
    return boundaryNodes;
  }

  /**
   * Compute the sum of the weights of all the outgoing edges for each node in the graph 
   */
  public void computeAdjWgtSums() {
    graph.map(new LambdaVoid<GNode<MetisNode>>() {
      public void call(GNode<MetisNode> node) {
        node.getData(MethodFlag.NONE).setAdjWgtSum(computeAdjWgtSum(node));
      }
    }, MethodFlag.NONE);
  }

  /**
   * compute graph cut
   */
  public int computeCut() {
    ComputeCutClosure closure = new ComputeCutClosure();
    graph.map(closure, MethodFlag.NONE);
    return closure.getCut();
  }

  class ComputeCutClosure implements LambdaVoid<GNode<MetisNode>> {
    int cut = 0;

    @Override
    public void call(GNode<MetisNode> node) {
      node.map(new Lambda2Void<GNode<MetisNode>, GNode<MetisNode>>() {
        public void call(GNode<MetisNode> neighbor, GNode<MetisNode> node) {
          if (neighbor.getData(MethodFlag.NONE).getPartition() != node.getData(MethodFlag.NONE).getPartition()) {
            int edgeWeight = (int) graph.getEdgeData(node, neighbor, MethodFlag.NONE);
            cut = cut + edgeWeight;
          }
        }
      }, node, MethodFlag.NONE);
    }

    public int getCut() {
      return cut / 2;
    }
  }

  /**
   * compute the number of edges in the graph
   */
  public int computeEdges() {
    ComputeEdgesClosure closure = new ComputeEdgesClosure();
    graph.map(closure, MethodFlag.NONE);
    return closure.getNum() / 2;
  }

  class ComputeEdgesClosure implements LambdaVoid<GNode<MetisNode>> {
    int num;

    @Override
    public void call(GNode<MetisNode> node) {
      num += graph.outNeighborsSize(node, MethodFlag.NONE);
    }

    public int getNum() {
      return num;
    }
  }

  /**
   * Compute the sum of the weights of all the outgoing edges for a node 
   */
  public int computeAdjWgtSum(GNode<MetisNode> n) {
    ComputeAdjWgtSumClosure closure = new ComputeAdjWgtSumClosure();
    n.map(closure, n, MethodFlag.NONE);
    return closure.getNum();
  }

  class ComputeAdjWgtSumClosure implements Lambda2Void<GNode<MetisNode>, GNode<MetisNode>> {
    int num;

    @Override
    public void call(GNode<MetisNode> neighbor, GNode<MetisNode> node) {
      int weight = (int) graph.getEdgeData(node, neighbor, MethodFlag.NONE);
      num = num + weight;
    }

    public int getNum() {
      return num;
    }
  }

  /**
   * verify if the partitioning is correctly performed by checking 
   * the internal maintained graph cut is same as the real graph cut
   */
  public boolean verify() {
    if (mincut.get() == computeCut()) {
      return true;
    } else {
      System.out.println("mincut is computed wrongly:" + mincut.get() + " is not equal " + computeCut());
      return false;
    }
  }

  /**
   * check if the partitioning is balanced
   */
  public boolean isBalanced(float[] tpwgts, float ubfactor) {
    int sum = 0;
    for (int i = 0; i < nparts; i++) {
      sum += partWeights[i].get(MethodFlag.NONE);
    }
    for (int i = 0; i < nparts; i++) {
      if (partWeights[i].get(MethodFlag.NONE) > tpwgts[i] * sum * (ubfactor + 0.005)) {
        return false;
      }
    }
    return true;
  }

  public void computeKWayBalanceBoundary() {
    unsetAllBoundaryNodes();
    graph.map(new LambdaVoid<GNode<MetisNode>>() {
      public void call(GNode<MetisNode> node) {
        if (node.getData().getEdegree() > 0) {
          setBoundaryNode(node);
        }
      }
    });
  }

  public void computeKWayBoundary() {
    unsetAllBoundaryNodes();
    graph.map(new LambdaVoid<GNode<MetisNode>>() {
      public void call(GNode<MetisNode> node) {
        MetisNode nodeData = node.getData();
        if (nodeData.getEdegree() - nodeData.getIdegree() >= 0) {
          setBoundaryNode(node);
        }
      }
    });
  }
}
