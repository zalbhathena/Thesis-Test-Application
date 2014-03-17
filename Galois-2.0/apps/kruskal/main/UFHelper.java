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

File: UFHelper.java 

*/





package kruskal.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;

// TODO: Auto-generated Javadoc
/**
 * The Class UFHelper.
 */
public class UFHelper {
  
   // precond: node1 and node2 are representatives in their components
   /**
    * Union.
    *
    * @param node1 the node1
    * @param node2 the node2
    * @return the k node
    */
   public static KNode union(GNode<KNode> node1, GNode<KNode> node2) {
     
     KNode kn1 = node1.getData(MethodFlag.NONE);
     KNode kn2 = node2.getData(MethodFlag.NONE);
     
     assert kn1.getRep() == node1;
     assert kn2.getRep() == node2;
     
     KNode parent = null; 
     KNode child = null;
     
     if(kn1.rank > kn2.rank) {
       parent = kn1; 
       child = kn2; 
     } else if( kn1.rank < kn2.rank) {
       parent = kn2; 
       child = kn1;
     } else {
       parent = kn1;
       child = kn2;
       parent.incRank();
     }
     
     // link up
     child.setRep(parent.getRep());
     
     
     return parent;
     
   }
   
   
  
//  // for debugging, a version that doesn't use BaselineLocker
//  public KNode union(GNode<KNode> node1, GNode<KNode> node2) {
//    
//    KNode kn1 = node1.getData(MethodFlag.NONE);
//    KNode kn2 = node2.getData(MethodFlag.NONE);
//    
//    assert kn1.getRep() == node1;
//    assert kn2.getRep() == node2;
//    
//    KNode parent = null; 
//    GNode<KNode> pNode = null;
//    KNode child = null;
//    GNode<KNode> cNode = null;
//    
//    if(kn1.rank > kn2.rank) {
//      pNode = node1;
//      cNode = node2;
//    } else if( kn1.rank < kn2.rank) {
//      pNode = node2;
//      cNode = node1;
//    } else {
//      pNode = node1;
//      cNode = node2;
//
//      // parent.incRank();
//      KNode upd = new KNode( parent.myNode, parent.getRep(), parent.getRank()+1 );
//      pNode.setData(upd);
//    }
//
//    parent = pNode.getData(MethodFlag.NONE);
//    child = cNode.getData(MethodFlag.NONE);
//    
//    // link up
//    // child.setRep(parent.getRep());
//
//
//    KNode upd = new KNode( child.myNode, parent.getRep(), child.getRank());
//    cNode.setData(upd);
//    
//    return parent;
//    
//  }

   /**
 * Find.
 *
 * @param node the node
 * @return the g node
 */
public static GNode<KNode> find(GNode<KNode> node) {
     KNode kn = node.getData();
     GNode<KNode> rep = kn.getRep();
     if(rep.getData() == kn) { // equiv to kn.rep == kn
       return rep;
     }
     else {
       rep = find(rep);
       kn.setRep(rep);
       return rep;
     }
   }



//  // for debugging, a version that doesn't use BaselineLocker
//  public GNode<KNode> find(GNode<KNode> node) {
//    KNode kn = node.getData();
//    GNode<KNode> rep = kn.getRep();
//    if(rep.getData() == kn) { // equiv to kn.rep == kn
//      return rep;
//    }
//    else {
//      rep = find(rep);
//
//      // kn.setRep(rep);
//      KNode upd = new KNode(kn.myNode, rep, kn.getRank());
//      node.setData(upd);
//
//      assert node.getData().getRep() == rep;
//      return rep;
//    }
//  }
   
   
}
