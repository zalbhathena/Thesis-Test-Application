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

File: SerialMain.java 

 */

package kruskal.main;

import galois.objects.graph.GNode;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;

import java.io.IOException;
import java.util.Arrays;

// TODO: Auto-generated Javadoc
/**
 * The Class SerialMain.
 */
public class SerialMain extends AbstractMain {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args) throws IOException {
    new SerialMain().run(args);
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return "handwritten serial";
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#newGraphInstance()
   */
  @Override
  protected ObjectGraph<KNode, Integer> newGraphInstance() {
    return new MorphGraph.ObjectGraphBuilder().serial(true).create();
  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#runLoop()
   */
  @Override
  public void runLoop() {
    KEdge[] edgeArray = edges.toArray(new KEdge[0]);
    Arrays.sort(edgeArray, new EdgeCmp());

    int numNodes = graph.size();

    int i = 0;
    for (int numUnions = 0; numUnions < numNodes - 1 && i < edgeArray.length; ++i) { // at most |V|-1 unions
      KEdge e = edgeArray[i];

      GNode<KNode> rep1 = UFHelper.find(e.getFirst());
      GNode<KNode> rep2 = UFHelper.find(e.getSecond());

      assert rep1 != null && rep2 != null;

      if (rep1 != rep2) {
        UFHelper.union(rep1, rep2);
        e.setInMST();
        ++numUnions;
      }

    }

    System.out.println(getVersion() + " iterations = " + i);

  }

  /* (non-Javadoc)
   * @see kruskal.main.AbstractMain#verify()
   */
  @Override
  public void verify() {
    System.err.println("Serial version doesn't have cross verification, MST weight = " + this.mstWeight);
  }

}
