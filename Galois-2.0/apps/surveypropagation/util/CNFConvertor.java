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

File: CNFConvertor.java 

*/

package surveypropagation.util;

/*
 *@author rashid
 *A utility to convert 3-CNF from SAT-Competition to Galois format. */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;


public class CNFConvertor {
	
	public static void main(String [] args)
	{
		
		if (args.length < 2)
		{
			throw new Error("Arguments: <input file> <output file>");
		}
		CNFConvertor.readIn(args[0], args[1]);
	
	}
	public static void readIn(final String cnfFileName, final String outFileName)
	{
		int numClauses = 0;
		Scanner scanner = null;
		int numVars = -1;
		numClauses = -1;

		try
		{
			scanner = new Scanner(new BufferedReader(new FileReader(cnfFileName)));
			scanner.useDelimiter(" ");
			String line = "";
			String output = "";
			while(scanner.hasNext())
			{
				line= scanner.nextLine();
				if (line.startsWith("c "))
					continue;
				if (line.startsWith("p cnf"))
				{
					StringTokenizer t = new StringTokenizer(line.substring(5)," ");
					numVars = Integer.parseInt(t.nextToken());
					numClauses = Integer.parseInt(t.nextToken());
					output += "numvar="+numVars+"\nnumclause="+numClauses+"\nmaxnumlits=3";
				}
				else
				{
					StringTokenizer t = new StringTokenizer(line.substring(0)," ");
					int tmp = Integer.parseInt(t.nextToken());
					output+="\n"+tmp;
					tmp = Integer.parseInt(t.nextToken());
					output+=","+tmp;
					tmp = Integer.parseInt(t.nextToken());
					output+=","+tmp;						
				}		
		}
		scanner.close();
		System.out.println(output);
		FileWriter f = new FileWriter(outFileName);
		f.write(output);
		f.close();
		}
		catch (final FileNotFoundException excep)
		{
			System.err.println(excep);
		}
		catch (IOException ioe)
		{
			System.err.println(ioe);
		}
		
	}
	//Do not really need this for now, but might need it if we
	//decide to convert galois formats to some other format.
	private Integer getVarValue(String str, final String numStr)
	{
		boolean okay = false;
		int value = -1;
		final Scanner scanner = new Scanner(str);
		scanner.useDelimiter("=");
		if (scanner.hasNext())
		{
			str = scanner.next();
			if (str.equals(numStr))
			{
				if (scanner.hasNextInt())
				{
					value = scanner.nextInt();
					okay = true;
				}
			}
		}
		if (!okay)
		{
			System.err.println("Error in reading the input CNF file");
			System.exit(-1);
		}

		return value;
	}

}