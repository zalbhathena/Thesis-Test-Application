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

package des.unordered;

import galois.objects.AbstractNoConflictBaseObject;
import galois.objects.MethodFlag;
import galois.objects.graph.GNode;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import des.main.EventRcvTimeTieBrkCmp;

//TODO: modeling one output for now. Need to extend for multiple outputs
/**
 * The Class SimObject represents an abstract simulation object (processing station). A simulation application
 * would inherit from this class.
 */
public abstract class SimObject extends AbstractNoConflictBaseObject {

  /** The id counter. */
  protected static int idCntr = 0;

  /** The MI n_ delay. */
  protected static long MIN_DELAY = 1l;

  /** The Constant INFINITY. */
  protected static final long INFINITY = Long.MAX_VALUE;

  /** The id. */
  protected int id;

  /** The number of inputs. */
  protected int numInputs;

  /** store the incoming events on each input */
  protected PriorityQueue<Event<?>>[] inputEvents;

  /** store the timestamp of latest event received on an input line */
  protected long[] inputTimes;

  /** local clock value, is the minimum of events received on all input lines */
  protected long clock = 0;

  /** readyEvents set, is a pq of events that can safely be processed now
    if minRecv = min latest timestamp received on any event i.e. min of inputTimes
    then events with timestamp <= minRecv go into readyEvents 
   */
  PriorityQueue<Event<?>> readyEvents;

  /** time stamp of the  last message sent on output 
  protected long lastSent = 0;

  /** flag that tells the SimObject is active and is/should be put on the worklist */
  protected AtomicBoolean isOnwl;

  /** whether it can process some events received on the input. i.e. if readyEvents
    is computed, it'll be non-empty
   */
  protected boolean active;

  /**
   * Instantiates a new simulation object.
   *
   * @param numInputs the number of  inputs
   * @param numOutputs the number of outputs
   */
  public SimObject(int numInputs, int numOutputs) {
    super();
    init(numInputs, numOutputs);
  }

  /**
   * Inits the object.
   *
   * @param numInputs the number of  inputs
   * @param numOutputs the number of outputs
   */
  private void init(int numInputs, int numOutputs) {
    this.id = ++idCntr;

    this.numInputs = numInputs;
    inputEvents = new PriorityQueue[numInputs];
    inputTimes = new long[numInputs];

    for (int i = 0; i < numInputs; ++i) {
      inputTimes[i] = 0;
      inputEvents[i] = new PriorityQueue<Event<?>>(1, new EventRcvTimeTieBrkCmp());
    }

    readyEvents = new PriorityQueue<Event<?>>(1, new EventRcvTimeTieBrkCmp());

    isOnwl = new AtomicBoolean();
  }

  /**
   * a way to construct different subtypes 
   * @return a new instance of subtype
   */
  public abstract SimObject newInstance();

  /**
   * @see galois.objects.AbsstractBaseObject#gclone()
   */
  @Override
  public Object gclone() {
    // since we are implementing cautious optimization for unordered des
    // we can uncomment the following line to ensure that no copying is being done
    // throw new IllegalStateException();

    SimObject that = this.newInstance();
    that.deepCopy(this);
    return that;
  }

  @Override
  public void restoreFrom(Object gclone) {
    SimObject that = (SimObject) gclone;
    this.deepCopy(that);
  }

  /**
   * performs a deep copy.
   *
   * @param that the that
   */

  protected void deepCopy(SimObject that) {
    init(that.numInputs, 1);
    for (int i = 0; i < numInputs; ++i) {
      this.inputTimes[i] = that.inputTimes[i];

      for (Event<?> e : that.inputEvents[i]) { // copy over the events for each input
        this.inputEvents[i].add(e);
      }
    }

    // defensive
    this.clock = that.clock;
  }

  /**
   * Simulate.
   *
   * @param myNode the node in the graph that has this SimObject as its node data
   * @return number of events ready to be executed.
   */
  public int simulate(GNode<SimObject> myNode) {
    computeClock();// update the clock, can do at the end if null msgs are propagated initially
    populateReadyEvents(); // fill up readyEvents, 
    int retVal = this.readyEvents.size();

    while (!readyEvents.isEmpty()) {
      Event<?> e = readyEvents.poll();
      assert e.getRecvObj() == myNode && myNode.getData(MethodFlag.NONE) == this; // should already own a lock

      execEvent(myNode, e);
    }

    return retVal;
  }

  // The user code should override this method inorder to
  // define the semantics of executing and event on
  /**
   * Exec event.
   *
   * @param myNode the node in the graph that has this SimObject as its node data
   * @param e the input event
   */
  protected abstract void execEvent(GNode<SimObject> myNode, Event<?> e);

  /**
   * Make event.
   *
   * @param <M> the generic type representing the message type
   * @param sendObj the send obj
   * @param recvObj the recv obj
   * @param type the type
   * @param msg the msg
   * @param sendTime the send time
   * @param delay the delay
   * @return the event
   */
  protected final <M> Event<M> makeEvent(GNode<SimObject> sendObj, GNode<SimObject> recvObj, Event.Type type, M msg,
      long sendTime, long delay) {
    if (delay <= 0) {
      delay = SimObject.MIN_DELAY;
    }
    long recvTime;
    if (sendTime == INFINITY) {
      recvTime = INFINITY;
    } else {
      recvTime = sendTime + delay;
    }
    return new Event<M>(sendObj, recvObj, type, sendTime, recvTime, msg); // TODO: call the constructor appropriately.
  }

  /**
   * Recv event.
   *
   * @param in the in
   * @param e the e
   */
  public void recvEvent(int in, Event<?> e) {
    assert in >= 0 && in < inputEvents.length;
    this.inputEvents[in].add(e);
    if (this.inputTimes[in] < e.getRecvTime()) {
      this.inputTimes[in] = e.getRecvTime();
    }
  }

  /**
   * compute the minimum time of a message received so far
   * for every input
   * if pq is not empty then take the time of min event in the pq
   * else take the time of the last event received on the input.
   */
  /*
  protected void computeClock() {
    long min = INFINITY;
    for(int i = 0; i < numInputs; ++i) {
      PriorityQueue<Event> pq = this.inputEvents[i];
      if(!pq.isEmpty()) {
        if(pq.peek().recvTime < min ) {
          min = pq.peek().recvTime;
        }
      }
      else {
        min = this.inputTimes[i];
      }
    }
    
    this.clock = min;
    
  }
   */

  protected void computeClock() {
    long min = INFINITY;
    for (int i = 0; i < numInputs; ++i) {
      if (min < this.inputTimes[i]) {
        min = this.inputTimes[i];
      }
    }

    this.clock = min;
  }

  /**
   * Checks if is active.
   *
   * @return true, if is active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * active is set to true if there exists an event on each input pq
   * or if an input pq is empty and an event with INFINITY has been received
   * telling that no more events on this input will be received.
   */
  public void updateActive() {
    boolean isWaiting = false; // is Waiting on an input event
    // i.e. pq of the input is empty and the time of last evetn is not INFINITY
    boolean allEmpty = true;
    for (int i = 0; i < numInputs; ++i) {
      if (inputEvents[i].isEmpty()) {
        if (inputTimes[i] < INFINITY) {
          isWaiting = true;
        }
      } else {
        allEmpty = false;
      }
    }
    active = !allEmpty && !isWaiting;

  }

  /**
   * Send event.
   *
   * @param outIndex the out index
   * @param target the target
   * @param e the e
   */
  protected void sendEvent(int outIndex, SimObject target, Event<?> e) {
    //TODO: not implemented yet
  }

  /**
   * Populate ready events.
   *
   * @return PriorityQueue of events that have recvTime <= this.clock
   */
  protected void populateReadyEvents() {

    for (PriorityQueue<Event<?>> pq : inputEvents) {
      // while(!pq.isEmpty() && pq.peek().recvTime <= this.clock) {
      // changing while to if to process one event per input at a time
      // this increases parallelism, while reduces parallelism but increases the 
      // work performed per iteration.
      while (!pq.isEmpty() && pq.peek().getRecvTime() <= this.clock) {
        this.readyEvents.add(pq.poll());
      }
    }

  }

  /**
   * flag indicating this is on the worklist
   *
   * @return the atomic boolean
   */
  public AtomicBoolean onwlFlag() {
    return this.isOnwl;
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
   * Gets the id counter.
   *
   * @return the id counter
   */
  public static int getIdCntr() {
    return idCntr;
  }

} // end class

