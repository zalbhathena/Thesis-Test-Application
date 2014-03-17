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

File: BaseEvent.java 

*/



package des.main;

import galois.objects.graph.GNode;
import galois.runtime.Features;
import galois.runtime.Replayable;

/**
 * The Class BaseEvent.
 *
 * @param <S> the generic type representing the simulation object 
 * @param <M> the generic type representing the message type
 */
public class BaseEvent<S, M> implements Replayable {

  /** The id. */
  protected int id;
  
  /** The send obj. */
  protected GNode<S> sendObj;
  
  /** The recv obj. */
  protected GNode<S> recvObj;
  
  /** The send time. */
  protected long sendTime;
  
  /** The recv time. */
  protected long recvTime;
  
  /** The message. */
  protected M message;

  /** The id counter for assigning ids to events. */
  private static int idCntr = 0;

  /**
   * Instantiates a new base event.
   *
   * @param sendObj the send obj
   * @param recvObj the recv obj
   * @param sendTime the send time
   * @param recvTime the recv time
   * @param message the message
   */
  public BaseEvent(GNode<S> sendObj, GNode<S> recvObj, long sendTime, long recvTime, M message) {
    super();
    this.sendObj = sendObj;
    this.recvObj = recvObj;
    this.sendTime = sendTime;
    this.recvTime = recvTime;
    this.message = message;
    this.id = idCntr++;
    Features.getReplayFeature().onCreateReplayable(this);
  }

  /**
   * Instantiates a new base event.
   */
  public BaseEvent() {
    this(null, null, -1, -1, null);
  }

  /*
   * compares equality two messages ignoring their types
   * for now the equals criteria include:
   * - id, sendObj, recvObj, sendTime, recvTime and message references equal
   *   but not the message types
   *
   */

  // note that if X extends Y
  // Object o1 = new X()
  // Object o2 = new Y()
  // o1 instanceof X == true
  // o2 instanceof X == true
  // o2 instanceof Y == true
  //
  /**
   * Equals ignore type.
   *
   * @param that the other event
   * @return true, if successful
   */
  public boolean equalsIgnoreType(BaseEvent<?, ?> that) {
    if (that != null) {
      return (this.id == that.id && this.sendObj == that.sendObj && this.recvObj == that.recvObj
          && this.sendTime == that.sendTime && this.recvTime == that.recvTime && this.message.equals(that.message));
    } else {
      return false;
    }
  }

  /**
   * Detailed string.
   *
   * @return the string
   */
  public String detailedString() {
    String s = "Event-" + id + ":" + "\n sendTime = " + sendTime + "\n sendObj = " + sendObj + "\n recvTime = "
        + recvTime + "\n recvObj = " + recvObj + "\n";
    s = s + message.toString();
    return s;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return Long.toString(recvTime);
  }

  /**
   * Gets the send obj.
   *
   * @return the send obj
   */
  public GNode<S> getSendObj() {
    return sendObj;
  }

  /**
   * Sets the send obj.
   *
   * @param sendObj the new send obj
   */
  public void setSendObj(GNode<S> sendObj) {
    this.sendObj = sendObj;
  }

  /**
   * Gets the recv obj.
   *
   * @return the recv obj
   */
  public GNode<S> getRecvObj() {
    return recvObj;
  }

  /**
   * Sets the recv obj.
   *
   * @param recvObj the new recv obj
   */
  public void setRecvObj(GNode<S> recvObj) {
    this.recvObj = recvObj;
  }

  /**
   * Gets the send time.
   *
   * @return the send time
   */
  public long getSendTime() {
    return sendTime;
  }

  /**
   * Sets the send time.
   *
   * @param sendTime the new send time
   */
  public void setSendTime(long sendTime) {
    this.sendTime = sendTime;
  }

  /**
   * Gets the recv time.
   *
   * @return the recv time
   */
  public long getRecvTime() {
    return recvTime;
  }

  /**
   * Sets the recv time.
   *
   * @param recvTime the new recv time
   */
  public void setRecvTime(long recvTime) {
    this.recvTime = recvTime;
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public M getMessage() {
    return message;
  }

  /**
   * Sets the message.
   *
   * @param message the new message
   */
  public void setMessage(M message) {
    this.message = message;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Gets the id counter.
   *
   * @return the id counter
   */
  public static int getIdCntr() {
    return idCntr;
  }

  /**
   * Reset id counter
   */
  protected static void resetIdCntr() {
    idCntr = 0;
  }

  /** The rid. */
  private long rid;

  /* (non-Javadoc)
   * @see galois.runtime.Replayable#getRid()
   */
  @Override
  public long getRid() {
    return rid;
  }

  /* (non-Javadoc)
   * @see galois.runtime.Replayable#setRid(long)
   */
  @Override
  public void setRid(long rid) {
    this.rid = rid;
  }
}
