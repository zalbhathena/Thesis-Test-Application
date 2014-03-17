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

File: AbstractMain.java 

 */
package surveypropagation.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.runtime.ForeachContext;
import galois.runtime.GaloisRuntime;
import galois.runtime.wl.FIFO;
import galois.runtime.wl.Priority;

import java.util.List;

import surveypropagation.main.SurveyPropagationClosures.TouchNeighborhood;
import util.Launcher;
import util.fn.Lambda2Void;

/**
 * The Class AbstractMain. Runs Survey Propagation over a given input CNF file.
 */
public abstract class AbstractMain {

  /** The graph. */
  protected FactorGraph graph;

  /**
   * Gets the version.
   *
   * @return the version
   */
  protected abstract String getVersion();

  /**
   * The main method, to run Survey Propagation over a problem
   * as specified in the input CNF filename.
   *
   * @param args The filename containing the 3-SAT problem to be solved.
   * @throws Exception the exception
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
      System.err.printf("application: Survey Propagation (%s version)\n",
          GaloisRuntime.getRuntime().useSerial() ? "serial" : "Galois");
      System.err.println("A heuristic SAT-solver based on Bayesian inference");
      System.err.println("http://iss.ices.utexas.edu/lonestar/surveypropagation.html");
      System.err.println();
    }

    // Var declarations
    graph = new FactorGraph();
    // init phase
    InputParameters.Read();
    graph.readIn(args[0]);

    // one time initialization.
    graph.updateAllVarNodes();

    Launcher.getLauncher().startTiming();
    final List<GNode<NodeData>> worklist = graph.getNodes();
    final TouchNeighborhood tn = new TouchNeighborhood();
    GaloisRuntime.foreach(worklist, new Lambda2Void<GNode<NodeData>, ForeachContext<GNode<NodeData>>>() {

      /**
       * Call.
       *
       * @param item The Node to be updated in this iteration.
       * @param ctx The execution context {@link #galois.runtime.ForeachContext ForeachContext} object.
       */
      @Override
      public void call(final GNode<NodeData> item, final ForeachContext<GNode<NodeData>> ctx) {
        item.map(tn, item);
        if (NodeData.time.get() < InputParameters.tmax) {
          final NodeData nodeData = (NodeData) item.getData(MethodFlag.NONE);
          // Fail safe
          if (nodeData.belongsToGraph) {
            if (graph.updateNode(item)) {
              if (NodeData.time.get() < InputParameters.tmax) {
                ctx.add(item);
              }
            }
          }
        }
      }
    }, Priority.first(FIFO.class));
    Launcher.getLauncher().stopTiming();
    if (Launcher.getLauncher().isFirstRun()) {
      graph.printSolution();
    }
    graph = null;
  }

}
