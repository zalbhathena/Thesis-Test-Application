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
package surveypropagation.main;

import galois.objects.graph.GNode;
import java.util.List;
import java.util.Random;

import util.Launcher;

/**
 * The Class SerialMain.
 * Runs a serial version of Survey Propagation.
 */
public class SerialMain extends AbstractMain {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    new SerialMain().run(args);
  }

  /* 
   * @see surveypropagation.main.AbstractMain#getVersion()
   */
  @Override
  protected String getVersion() {
    return "Galois";
  }

  /* 
   * @see surveypropagation.main.AbstractMain#run(java.lang.String[])
   */
  public void run(String args[]) throws Exception {
    if (args.length < 1) {
      throw new Error("Arguments: <input file>");
    }

    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.println("application: Survey Propagation (Serial version)");
      System.err.println("A heuristic SAT-solver based on Bayesian inference");
      System.err.println("http://iss.ices.utexas.edu/lonestar/surveypropagation.html");
      System.err.println();
    }

    graph = new FactorGraph();
    // init phase
    InputParameters.Read();
    graph.readIn(args[0]);

    // one time initialization.
    graph.updateAllVarNodes();
    System.out.println("Done one time initializtion");

    // /////////////////////////////////////////////////////////////////////
    Launcher.getLauncher().startTiming();

    List<GNode<NodeData>> worklist = graph.getNodes();
    Random rand = new Random(3);

    while (worklist.size() > 0) {
      GNode<NodeData> m = worklist.remove(Math.abs(rand.nextInt()) % worklist.size());
      NodeData item = m.getData();
      if (NodeData.time.get() < InputParameters.tmax) {
        if (item.belongsToGraph) {
          if (graph.updateNode(m)) {
            worklist.add(m);
          }
        }
      }

    }
    Launcher.getLauncher().stopTiming();
    // //////////////////////////////////////////////////////////////////////////////////
    System.err.println("runtime: " + Launcher.getLauncher().elapsedTime(false) + " ms");

    if (Launcher.getLauncher().isFirstRun()) {      
      graph.printSolution();
    }
  }
}
