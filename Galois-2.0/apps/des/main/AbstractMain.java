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

package des.main;

import galois.objects.graph.Graph;
import galois.runtime.GaloisRuntime;

import java.util.List;
import java.util.Map;

import util.Launcher;
import des.main.circuitlib.OUTPUT;

/**
 * The Class AbstractMain holds common functionality for {@link Main} and {@link SerialMain}.
 */
public abstract class AbstractMain {

  /** The simulation initializer object. */
  protected SimInit simInit;

  /** The graph. */
  protected Graph<SimObject> graph;

  /**
   * New graph instance.
   *
   * @return a new instance of {@link Graph}
   */
  protected abstract Graph<SimObject> newGraphInstance();

  /**
   * Gets the version.
   *
   * @return the version
   */
  protected abstract String getVersion();

  /**
   * Runs the main loop.
   *
   * @throws Exception the exception
   */
  protected abstract void runLoop() throws Exception;

  /**
   * Run.
   *
   * @param args the command line args
   */
  public void run(String[] args) {
    if (Launcher.getLauncher().isFirstRun()) {
      System.err.println();
      System.err.println("Lonestar Benchmark Suite v3.0");
      System.err.println("Copyright (C) 2007, 2008, 2009, 2010 The University of Texas at Austin");
      System.err.println("http://iss.ices.utexas.edu/lonestar/");
      System.err.println();
      System.err.printf("application: Discrete Event Simulation (%s version)\n", getVersion());
      System.err.println("Logic Circuit Simulation");
      System.err.println("Simulates combinational logic circuits");
      System.err.println("http://iss.ices.utexas.edu/lonestar/des.html");
      System.err.println();
    }

    if (args.length < 1) {
      System.err.println("arguments: input_file_name");
      System.exit(-1);
    }

    graph = newGraphInstance();
    String netlistFile = args[0];
    simInit = new SimInit(graph, netlistFile);

    if (Launcher.getLauncher().isFirstRun()) {
      System.err.printf("configuration: %d nodes, %d edges\n", graph.size(), simInit.getNumEdges());
      System.err.println("Numober of initial events = " + simInit.getInitEvents().size());
      System.err.println("number of threads: " + GaloisRuntime.getRuntime().getMaxThreads());
      System.err.println();
    }

    Launcher.getLauncher().startTiming();
    try {
      runLoop();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Launcher.getLauncher().stopTiming();

    verify();

  }

  /**
   * Verify.
   */
  protected void verify() {
    List<SimObject> outputObjs = simInit.getOutputObjs();
    Map<String, Character> outValues = simInit.getOutValues();

    int exitStatus = 0;
    for (SimObject so : outputObjs) {
      char simulated = ((OUTPUT) so).getOutputVal();
      char expected = outValues.get(((OUTPUT) so).getInputName());

      if (simulated != expected) {
        exitStatus = 1;
        System.err.println("Wrong output value for " + ((OUTPUT) so).getOutputName() + ", simulated : " + simulated
            + " expected : " + expected);
      }
    }

    if (exitStatus != 0) {
      System.out.println("-----------------------------------------------------------");
      for (SimObject so : outputObjs) {
        System.out.println(so);
      }
      throw new IllegalStateException(" Simulated output does not match the expected output\n");
    } else {
      System.out.println("Simulation verified as correct");
    }
  }

}
