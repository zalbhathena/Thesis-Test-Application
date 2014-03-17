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

File: SimObject.java 

 */

package des.main;

import galois.objects.AbstractBaseObject;
import galois.objects.graph.GNode;

import java.util.List;

/**
 * The Class SimObject represents an abstract simulation object (a processing station). An application would inherit from this
 * class to define the simulation behavior of the object
 */
public abstract class SimObject extends AbstractBaseObject {

  /** The id. */
  protected int id;

  /** The id counter. */
  protected static int idCntr = 0;

  /** The min delay. */
  protected static long MIN_DELAY = 1l;

  /**
   * Instantiates a new simulation object.
   */
  public SimObject() {
    super();
    this.id = ++idCntr;
  }

  /**
   * a way to construct different subtypes 
   * @return a new instance of subtype
   */
  public abstract SimObject newInstance();

  // The user code should override this method inorder to
  // define the semantics of executing an event on some SimObject
  /**
   * Exec event.
   *
   * @param myNode the node in the graph that has this simobject as its node data
   * @param event the event
   * @return the list of newly created events
   */
  public abstract List<Event<?>> execEvent(GNode<SimObject> myNode, Event<?> event);

  // deep copy
  /**
   * deep copy.
   *
   * @param that the that
   */
  protected void deepCopy(SimObject that) {
    id = that.id;
  }

  // TODO: change name to makeEvent
  /**
   * Make event.
   *
   * @param <M> the generic type representing the message type
   * @param sendObj the send obj
   * @param recvObj the recv obj
   * @param msg the msg
   * @param sendTime the send time
   * @param delay the delay
   * @return the event
   */
  protected final <M> Event<M> makeEvent(GNode<SimObject> sendObj, GNode<SimObject> recvObj, M msg, long sendTime,
      long delay) {
    if (delay <= 0) {
      delay = SimObject.MIN_DELAY;
    }
    return new Event<M>(sendObj, recvObj, sendTime, sendTime + delay, msg); // TODO: call the constructor appropriately.
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = "SimObject-" + id + " ";
    return s;
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
   * Assign id.
   */
  protected void assignId() {
    this.id = ++idCntr;
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
   * @see galois.objects.AbsstractBaseObject#gclone()
   */
  @Override
  public Object gclone() {
    SimObject that = this.newInstance();
    that.deepCopy(this);
    return that;
  }

  /**
   * @see galois.objects.AbstractBaseObject#restoreFrom(Object)
   */
  @Override
  public void restoreFrom(Object gclone) {
    SimObject that = (SimObject) gclone;
    this.deepCopy(that);
  }

} // end class

