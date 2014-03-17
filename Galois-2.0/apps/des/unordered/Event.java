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

File: Event.java 

*/





package des.unordered;

import galois.objects.graph.GNode;


/**
 * The Class Event.
 *
 * @param <M> the generic type representing the message type
 */
public class Event<M> extends des.main.BaseEvent<SimObject, M> {

  /**
   * The Enum Type representing the type of the Event NULL or REGULAR.
   */
  public static enum Type { 
    /** The REGULAR. */
    REGULAR, 
    /** The NULL. */
    NULL 
  };

  /** The type of the event. */
  protected Type type;

  /**
   * Instantiates a new event.
   *
   * @param sendObj the send obj
   * @param recvObj the recv obj
   * @param type the type
   * @param sendTime the send time
   * @param recvTime the recv time
   * @param message the message
   */
  public Event(GNode<SimObject> sendObj, GNode<SimObject> recvObj, Type type, long sendTime, long recvTime, M message) {
    super(sendObj, recvObj, sendTime, recvTime, message);
    this.setType(type);
  }

 
  // FIXME: sendObj and recvObj do not print the node data
  /* (non-Javadoc)
   * @see des.main.BaseEvent#detailedString()
   */
  @Override
  public String detailedString() {
   
    return super.detailedString() + " " + type;
  }

  /* (non-Javadoc)
   * @see des.main.BaseEvent#toString()
   */
  @Override 
    public String toString() {
      return Long.toString(recvTime);
    }
  
  

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(Type type) {
    assert type == Type.NULL || type == Type.REGULAR;
    this.type = type;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public Type getType() {
    return type;
  }
}

