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

File: SimInit.java 

 */

package des.main;

import galois.objects.graph.GNode;
import galois.objects.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Pair;
import util.fn.LambdaVoid;
import des.main.NetlistParser.GateRec;
import des.main.circuitlib.AND2;
import des.main.circuitlib.INPUT;
import des.main.circuitlib.INV;
import des.main.circuitlib.LogicEvent;
import des.main.circuitlib.LogicGate;
import des.main.circuitlib.NAND2;
import des.main.circuitlib.NOR2;
import des.main.circuitlib.OR2;
import des.main.circuitlib.OUTPUT;
import des.main.circuitlib.OneInputGate;
import des.main.circuitlib.TwoInputGate;
import des.main.circuitlib.XNOR2;
import des.main.circuitlib.XOR2;

/**
 * The Class SimInit encapsulates the initialization related state and functionality.
 */
public class SimInit {

  /** The parser. */
  private NetlistParser parser;

  /** The circuit. */
  private Graph<SimObject> circuit; // should contain all the gates, inputs and outputs

  /** The input objs. */
  private List<SimObject> inputObjs = new ArrayList<SimObject>();

  /** The input nodes. */
  private List<GNode<SimObject>> inputNodes = new ArrayList<GNode<SimObject>>();

  /** The output objs. */
  private List<SimObject> outputObjs = new ArrayList<SimObject>();

  /** The init events. */
  private List<Event<LogicEvent>> initEvents = new ArrayList<Event<LogicEvent>>();

  /** The gates. */
  private List<SimObject> gates = new ArrayList<SimObject>();

  /** The one input gates map. */
  protected Map<String, OneInputGate> oneInputGatesMap = new HashMap<String, OneInputGate>();

  /** The two input gates map. */
  protected Map<String, TwoInputGate> twoInputGatesMap = new HashMap<String, TwoInputGate>();

  /** The number of edges. */
  private int numEdges = 0;

  /** The number of nodes. */
  private int numNodes = 0;

  // initializer block
  {
    oneInputGatesMap.put("INV".toLowerCase(), new INV());

    twoInputGatesMap.put("AND2".toLowerCase(), new AND2());
    twoInputGatesMap.put("NAND2".toLowerCase(), new NAND2());
    twoInputGatesMap.put("OR2".toLowerCase(), new OR2());
    twoInputGatesMap.put("NOR2".toLowerCase(), new NOR2());

    twoInputGatesMap.put("XOR2".toLowerCase(), new XOR2());
    twoInputGatesMap.put("XNOR2".toLowerCase(), new XNOR2());
  }

  /**
   * Instantiates a new instance
   *
   * @param circuit the circuit
   * @param netlistFile the netlist file
   */
  public SimInit(Graph<SimObject> circuit, String netlistFile) {
    this.circuit = circuit;
    parser = new NetlistParser(netlistFile);

    initialize();
  }

  /*
   * Processing steps
   * create the input and output objects and add to netlistArrays
   * create the gate objects
   * connect the netlists by populating the fanout lists
   * create a list of initial events
   *
   */

  /**
   * Initialize.
   */
  protected void initialize() {
    // create input and output objects
    createInputObjs();
    createOutputObjs();

    createGateObjs();

    // add all gates, inputs and outputs to the Graph circuit
    for (SimObject so : inputObjs) {
      GNode<SimObject> n = circuit.createNode(so);
      circuit.add(n);
      ++numNodes;
      inputNodes.add(n);
    }

    createInitEvents();

    // create nodes for outputObjs
    for (SimObject so : outputObjs) {
      GNode<SimObject> n = circuit.createNode(so);
      circuit.add(n);
      ++numNodes;
    }

    // create nodes for all gates
    for (SimObject so : gates) {
      GNode<SimObject> n = circuit.createNode(so);
      circuit.add(n);
      ++numNodes;
    }

    // create the connections based on net names
    createConnections();
  }

  /**
   * Creates the input objs.
   */
  protected void createInputObjs() {
    for (int i = 0; i < parser.getInputNames().size(); ++i) {
      String out = parser.getInputNames().get(i);
      String in = addPrefix("in_", out);

      inputObjs.add(new INPUT(out, in));
    }
  }

  /**
   * Creates the output objs.
   */
  protected void createOutputObjs() {
    List<String> outputNames = parser.getOutputNames();
    for (int i = 0; i < outputNames.size(); ++i) {
      String in = outputNames.get(i);
      String out = addPrefix("out_", in);

      outputObjs.add(new OUTPUT(out, in));
    }
  }

  /**
   * Adds the prefix.
   *
   * @param prefix the prefix
   * @param word the word
   * @return prefix + word
   */
  protected static String addPrefix(String prefix, String word) {
    return prefix + word;
  }

  /**
   * Creates the initial events.
   */
  protected void createInitEvents() {

    Map<String, List<Pair<Long, Character>>> inputStimulusMap = parser.getInputStimulusMap();

    for (GNode<SimObject> n : this.inputNodes) {
      INPUT currInput = (INPUT) n.getData();
      List<Pair<Long, Character>> tvList = inputStimulusMap.get(currInput.getOutputName());

      if (tvList != null) {
        for (int j = 0; j < tvList.size(); ++j) {
          Pair<Long, Character> p = tvList.get(j);
          LogicEvent le = new LogicEvent(currInput.getOutputName(), p.getSecond());

          Event<LogicEvent> e = new Event<LogicEvent>(n, n, p.getFirst(), p.getFirst(), le);

          initEvents.add(e);
        }
      }
    }
  }

  /**
   * Creates the gate objs.
   */
  protected void createGateObjs() {
    List<GateRec> gateRecs = parser.getGates();

    for (GateRec grec : gateRecs) {
      if (oneInputGatesMap.containsKey(grec.getName())) {
        OneInputGate g = (OneInputGate) oneInputGatesMap.get(grec.getName()).newInstance();

        this.gates.add(g);

        // set output name
        assert grec.outputs.size() == 1;
        g.setOutputName(grec.outputs.get(0));

        // set input name
        assert grec.inputs.size() == 1;
        g.setInputName(grec.inputs.get(0));

        // set delay
        g.setDelay(grec.delay);
      } else if (twoInputGatesMap.containsKey(grec.getName())) {
        TwoInputGate g = (TwoInputGate) twoInputGatesMap.get(grec.getName()).newInstance();
        this.gates.add(g);

        // set output name
        assert grec.outputs.size() == 1;
        g.setOutputName(grec.outputs.get(0));

        // set input 1 name
        assert grec.inputs.size() == 2;
        g.setInput1Name(grec.inputs.get(0));
        // set input 2 name
        g.setInput2Name(grec.inputs.get(1));

        // set delay
        g.setDelay(grec.delay);

      } else {
        throw new IllegalStateException("Found a gate with unknown name " + grec.getName());
      }

    }

  }

  /**
   * Creates the connections.
   */
  protected void createConnections() {

    final Set<GNode<SimObject>> allNodes = new HashSet<GNode<SimObject>>();
    circuit.map(new LambdaVoid<GNode<SimObject>>() {

      @Override
      public void call(GNode<SimObject> n) {
        allNodes.add(n);
      }
    });

    for (GNode<SimObject> src : allNodes) {
      LogicGate srcGate = (LogicGate) src.getData();
      String outName = srcGate.getOutputName();

      for (GNode<SimObject> dst : allNodes) {
        LogicGate dstGate = (LogicGate) dst.getData();

        if (dstGate.hasInputName(outName)) {
          assert srcGate != dstGate;
          if (circuit.addNeighbor(src, dst)) {
            ++numEdges;
          }
        }
      }

    }

  }

  /**
   * Gets the circuit.
   *
   * @return the circuit
   */
  public Graph<SimObject> getCircuit() {
    return circuit;
  }

  /**
   * Gets the inits the events.
   *
   * @return the inits the events
   */
  public List<Event<LogicEvent>> getInitEvents() {
    return initEvents;
  }

  /**
   * Gets the input names.
   *
   * @return the input names
   */
  public List<String> getInputNames() {
    return parser.getInputNames();
  }

  /**
   * Gets the input objs.
   *
   * @return the input objs
   */
  public List<SimObject> getInputObjs() {
    return inputObjs;
  }

  /**
   * Gets the output names.
   *
   * @return the output names
   */
  public List<String> getOutputNames() {
    return parser.getOutputNames();
  }

  /**
   * Gets the output objs.
   *
   * @return the output objs
   */
  public List<SimObject> getOutputObjs() {
    return outputObjs;
  }

  /**
   * Gets the out values.
   *
   * @return the out values
   */
  public Map<String, Character> getOutValues() {
    return parser.getOutValues();
  }

  /**
   * Gets the number of edges.
   *
   * @return the number of edges
   */
  public int getNumEdges() {
    return numEdges;
  }

  /**
   * Gets the number of nodes
   *
   * @return the number of nodes
   */
  public int getNumNodes() {
    return numNodes;
  }
}
