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

File: NetlistParser.java 

 */

package des.main;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Pair;

/**
 * The Class NetlistParser parses an input netlist file.
 */
public class NetlistParser {

  /** The netlist file. */
  private String netlistFile;

  /** The input names. */
  private List<String> inputNames = new ArrayList<String>();

  /** The output names. */
  private List<String> outputNames = new ArrayList<String>();

  /** The out values. */
  private Map<String, Character> outValues = new HashMap<String, Character>();

  /** The input stimulus map has a list of (time, value) pairs for each input. */
  private Map<String, List<Pair<Long, Character>>> inputStimulusMap = new HashMap<String, List<Pair<Long, Character>>>();

  /** The gates. */
  private List<GateRec> gates = new ArrayList<GateRec>();

  /** The finish time. */
  private long finishTime = -1;

  /** The Constant FILE_SIZE. */
  private static final int FILE_SIZE = (1 << 22); // 4 meg
  //following is the list of token separators; characters meant to be ignored
  /** The Constant DELIM. */
  private static final String DELIM = " \n\t,;()=";

  /** The one input gates. */
  protected static Set<String> oneInputGates = new HashSet<String>();

  /** The two input gates. */
  protected static Set<String> twoInputGates = new HashSet<String>();

  // initializer block
  static {
    oneInputGates.add("INV".toLowerCase());
    twoInputGates.add("AND2".toLowerCase());
    twoInputGates.add("OR2".toLowerCase());
    twoInputGates.add("NAND2".toLowerCase());
    twoInputGates.add("NOR2".toLowerCase());
    twoInputGates.add("XOR2".toLowerCase());
    twoInputGates.add("XNOR2".toLowerCase());

  }

  /**
   * Instantiates a new netlist parser.
   *
   * @param netlistFile the netlist file
   */
  public NetlistParser(String netlistFile) {
    this.netlistFile = netlistFile;
    parse(netlistFile);
  }

  /*
   * Parsing steps
   * parse input signal names
   * parse output signal names
   * parse finish time
   * parse stimulus lists for each input signal
   * parse the netlist
   *
   */

  /**
   * Parses the netlist contained in fileName.
   *
   * @param fileName the file name
   */
  public void parse(String fileName) {
    System.out.println("input: reading circuit from file: " + fileName);

    char[] buf = new char[FILE_SIZE];
    FileReader file = null;
    int numbytes = 0;

    try {
      file = new FileReader(fileName);
      numbytes = file.read(buf);
    } catch (IOException e) {
      System.out.print(e.getMessage());
      System.exit(1);
    }

    String text = new String(buf, 0, numbytes);

    // remove the comments of style //
    Pattern comment = Pattern.compile("//.*\n");
    Matcher m = comment.matcher(text);
    text = m.replaceAll("");

    // System.out.println(text); // for debugging
    // System.exit(0);

    StringTokenizer tokenizer = new StringTokenizer(text, DELIM);

    String token;

    while (tokenizer.hasMoreTokens()) {

      token = tokenizer.nextToken().toLowerCase();

      if (token.equals("inputs")) {
        parsePortList(tokenizer, inputNames);
      } else if (token.equals("outputs")) {
        parsePortList(tokenizer, outputNames);
      } else if (token.equals("outvalues")) {
        parseOutValues(tokenizer, outValues);
      } else if (token.equals("finish")) {
        token = tokenizer.nextToken().toLowerCase();
        finishTime = Long.parseLong(token);
      } else if (token.equals("initlist")) {
        parseInitList(tokenizer, inputStimulusMap);
      } else if (token.equals("netlist")) {
        parseNetlist(tokenizer, gates);
      }
    } // end outer while

  } // end parse()

  /**
   * Gets the finish time.
   *
   * @return the finish time
   */
  public long getFinishTime() {
    return finishTime;
  }

  /**
   * Parses the port list.
   *
   * @param tokenizer the tokenizer
   * @param portNames the net names for input/output ports
   */
  protected static void parsePortList(StringTokenizer tokenizer, List<String> portNames) {
    String token = tokenizer.nextToken().toLowerCase();
    while (!token.equals("end")) {
      portNames.add(token);
      token = tokenizer.nextToken().toLowerCase();
    }
  }

  /**
   * Parses the out values.
   *
   * @param tokenizer the tokenizer
   * @param outValues the expected out values at the end of the simulation
   */
  protected static void parseOutValues(StringTokenizer tokenizer, Map<String, Character> outValues) {
    String token = tokenizer.nextToken().toLowerCase();
    while (!token.equals("end")) {
      String outName = token;
      token = tokenizer.nextToken().toLowerCase();
      Character value = token.charAt(0);
      token = tokenizer.nextToken().toLowerCase();

      outValues.put(outName, value);
    }
  }

  /**
   * Parses the initialization list for all the inputs.
   *
   * @param tokenizer the tokenizer
   * @param inputStimulusMap the input stimulus map 
   */
  protected static void parseInitList(StringTokenizer tokenizer,
      Map<String, List<Pair<Long, Character>>> inputStimulusMap) {
    // capture the name of the input signal
    String input = tokenizer.nextToken().toLowerCase();

    String token = tokenizer.nextToken().toLowerCase();

    List<Pair<Long, Character>> timeValList = new ArrayList<Pair<Long, Character>>();
    while (!token.equals("end")) {

      Long t = Long.parseLong(token);
      token = tokenizer.nextToken().toLowerCase();
      Character v = token.charAt(0);

      timeValList.add(new Pair<Long, Character>(t, v));

      token = tokenizer.nextToken().toLowerCase();
    }

    inputStimulusMap.put(input, timeValList);
  }

  /**
   * Parses the actual list of gates
   *
   * @param tokenizer the tokenizer
   * @param gates the gates
   */
  protected static void parseNetlist(StringTokenizer tokenizer, List<GateRec> gates) {

    String token = tokenizer.nextToken().toLowerCase();

    while (!token.equals("end")) {

      if (oneInputGates.contains(token)) {

        GateRec g = new GateRec();
        g.setName(token); // set the gate name
        gates.add(g);

        token = tokenizer.nextToken().toLowerCase(); // output name
        g.addOutput(token);

        token = tokenizer.nextToken().toLowerCase(); // input
        g.addInput(token);

        // possibly delay, if no delay then next gate or end
        token = tokenizer.nextToken().toLowerCase();
        if (token.charAt(0) == '#') {
          token = token.substring(1);
          long d = Long.parseLong(token);
          g.setDelay(d);
        } else {
          continue;
        }
      } else if (twoInputGates.contains(token)) {
        GateRec g = new GateRec();
        g.setName(token); // set the gate name
        gates.add(g);

        token = tokenizer.nextToken().toLowerCase(); // output name
        g.addOutput(token);

        token = tokenizer.nextToken().toLowerCase(); // input 1
        g.addInput(token);

        token = tokenizer.nextToken().toLowerCase(); // input 2
        g.addInput(token);

        // possibly delay, if no delay then next gate or end
        token = tokenizer.nextToken().toLowerCase();
        if (token.charAt(0) == '#') {
          token = token.substring(1);
          long d = Long.parseLong(token);
          g.setDelay(d);
        } else {
          continue;
        }
      } else {
        System.err.println("Unknown type of gate " + token);
      }

      //necessary to move forward in the while loop
      token = tokenizer.nextToken().toLowerCase();
    } // end of while
  }

  /**
   * Gets the netlist file.
   *
   * @return the netlist file
   */
  public String getNetlistFile() {
    return netlistFile;
  }

  /**
   * Gets the input names.
   *
   * @return the input names
   */
  public List<String> getInputNames() {
    return inputNames;
  }

  /**
   * Gets the output names.
   *
   * @return the output names
   */
  public List<String> getOutputNames() {
    return outputNames;
  }

  /**
   * Gets the out values.
   *
   * @return the out values
   */
  public Map<String, Character> getOutValues() {
    return outValues;
  }

  /**
   * Gets the input stimulus map.
   *
   * @return the input stimulus map
   */
  public Map<String, List<Pair<Long, Character>>> getInputStimulusMap() {
    return inputStimulusMap;
  }

  /**
   * Gets the gates.
   *
   * @return the gates
   */
  public List<GateRec> getGates() {
    return gates;
  }

  /**
   * The Class GateRec stores the data for a specific gate.
   */
  public static class GateRec {

    /** The name. */
    public String name = new String();

    /** The net names outputs. */
    public List<String> outputs = new ArrayList<String>();

    /** The net names inputs. */
    public List<String> inputs = new ArrayList<String>();

    /** The delay. */
    public long delay = 0;

    /**
     * Adds the output.
     *
     * @param net the net
     */
    public void addOutput(String net) {
      outputs.add(net);
    }

    /**
     * Adds the input.
     *
     * @param net the net
     */
    public void addInput(String net) {
      inputs.add(net);
    }

    /**
     * Sets the delay.
     *
     * @param delay the new delay
     */
    public void setDelay(long delay) {
      this.delay = delay;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
      return name;
    }
  }

}
