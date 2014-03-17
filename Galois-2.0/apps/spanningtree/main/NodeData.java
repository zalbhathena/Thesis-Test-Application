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

File: MstNode.java 

 */

package spanningtree.main;

import galois.objects.AbstractBaseObject;

public class NodeData extends AbstractBaseObject {
  private String data;
  private boolean inMst;

  public NodeData(String data) {
    this.data = data;
    inMst = false;
  }

  public boolean inSpanningTree() {
    return inMst;
  }

  public void setInSpanningTree(boolean value) {
    inMst = value;
  }

  @Override
  public Object gclone() {
    NodeData ret = new NodeData(data);
    ret.inMst = inMst;
    return ret;
  }

  @Override
  public void restoreFrom(Object copy) {
    NodeData mstNode = (NodeData) copy;
    inMst = mstNode.inMst;
  }

  @Override
  public String toString() {
    return String.format("(%s,%b)", data, inMst);
  }
}
