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

File: InputParameters.java 

 */
package surveypropagation.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Class InputParameters.
 */
public class InputParameters {

  /** The nsu. The number of successive updates*/
  protected static long nsu;

  /** The tau. For each tau number ofupdates of a node, tmax is incremented once*/
  protected static long tau; // 

  /** The tmax. For each tmax iterations, timeout is checked */
  public static long tmax = 20; // 

  /** The epsilon. */
  protected static double epsilon;

  /** The delta. */
  protected static double delta;

  /**
   * Read. Reads in configuration values from spparam.txt file.
   */
  public static void Read() {
    // set default values
    nsu = 25;
    tau = 10000;
    tmax = 100;
    epsilon = 0.01;
    delta = 0.005;
    NodeData.time = new AtomicLong(0);

    // read in parameter from spparam.txt
    Scanner scanner = null;
    try {
      scanner = new Scanner(new BufferedReader(new FileReader("./input/surveypropagation/spparam.txt")));
      while (scanner.hasNextLine()) {
        final Scanner linescanner = new Scanner(scanner.nextLine());
        linescanner.useDelimiter("=");

        if (linescanner.hasNext()) {
          String token = linescanner.next();
          if (token.equals("nsu")) {
            if (linescanner.hasNextLong()) {
              nsu = linescanner.nextLong();
            }
          } else if (token.equals("tau")) {
            if (linescanner.hasNextLong()) {
              tau = linescanner.nextLong();
            }
          } else if (token.equals("tmax")) {
            if (linescanner.hasNextLong()) {
              tmax = linescanner.nextLong();
            }
          } else if (token.equals("epsilon")) {
            if (linescanner.hasNextDouble()) {
              epsilon = linescanner.nextDouble();
            }
          } else if (token.equals("delta")) {
            if (linescanner.hasNextDouble()) {
              delta = linescanner.nextDouble();
            }
          } else {
            System.err.println("Unknown token in file spparam.txt");
            System.exit(-1);
          }
        }
        linescanner.close();
      }
    } catch (final FileNotFoundException excep) {
      System.err.println(excep);
    } finally {
      if (scanner != null) {
        scanner.close();
      }
    }
  }
}
