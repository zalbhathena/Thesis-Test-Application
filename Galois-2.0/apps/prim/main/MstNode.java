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

package prim.main;

import galois.objects.AbstractBaseObject;

public class MstNode extends AbstractBaseObject {

  private final String data;
  private boolean inMst;

  public MstNode(String data) {
    this.data = data;
    inMst = false;
  }

  public boolean inMst() {
    return inMst;
  }

  public void setInMst(boolean value) {
    inMst = value;
  }

  @Override
  public Object gclone() {
    MstNode ret = new MstNode(data);
    ret.inMst = inMst;
    return ret;
  }

  @Override
  public void restoreFrom(Object copy) {
    MstNode mstNode = (MstNode) copy;
    inMst = mstNode.inMst;
  }

  @Override
  public String toString() {
    return String.format("(%s,%b)", data, inMst);
  }
}
