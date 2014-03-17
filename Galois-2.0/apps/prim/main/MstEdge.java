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

File: MstEdge.java 

 */

package prim.main;

import galois.objects.graph.GNode;
import galois.objects.graph.ObjectUndirectedEdge;

public class MstEdge extends ObjectUndirectedEdge<MstNode, Long> {

  MstEdge(GNode<MstNode> src, GNode<MstNode> dst, long data) {
    super(src, dst, data);
  }

  @Override
  public String toString() {
    return String.format("(%d,%x,%x)", super.getData(), super.getFirst().hashCode(), super.getSecond().hashCode());
  }
}
