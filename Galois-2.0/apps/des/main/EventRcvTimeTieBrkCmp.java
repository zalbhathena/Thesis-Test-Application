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

File: EventRcvTimeTieBrkCmp.java 

*/





package des.main;

import java.util.Comparator;

/**
 * The Class EventRcvTimeTieBrkCmp.
 */
public class EventRcvTimeTieBrkCmp implements Comparator<BaseEvent<?,?>> {
  /*
   * comparison is based on recvTime,
   * and attempts to break the tie when recvTime's are equal
   *    - if recvObj's are same then
   *       the event with lower id was generated first. and should be processed first
   *       A SimObject may receive events on inputs i1 and i2 with same timestamp, resulting
   *       in generating output msgs m1 and m2 on same output line with same timestamps. The order
   *       in which m1 and m2 are processed should be the same in which i1 and i2 are processed, which
   *       requires a consistent tie breaking policy, but
   *       heap based priority queues don't guarantee order when timestamps are same. In general it is sufficient
   *       to define a total order only on events received by a SimObject (or sent), but heap based priority queue
   *       requires a total order
   *
   *
   */

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(BaseEvent<?,?> left, BaseEvent<?,?> right) {
    if (left.recvTime < right.recvTime) {
      return -1;
    } else if (left.recvTime == right.recvTime) {
      return left.id - right.id;
    } else {
      return 1;
    }
  }
}
