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


*/





package galois.runtime;

import fn.LambdaVoid;

class NoReplayFeature extends ReplayFeature {
  public NoReplayFeature(int maxIterations) {
    super(maxIterations);
  }

  @Override
  public void onFinish() {
    checkValidity();
  }

  @Override
  public boolean isCallbackControlled() {
    checkValidity();
    return false;
  }

  @Override
  public <T> void registerCallback(long rid, LambdaVoid<ForeachContext<T>> callback) {
    checkValidity();
  }

  @Override
  public void registerCallback(long rid, Callback callback) {
    checkValidity();
  }

  @Override
  public void onCallbackExecute(long rid) {
    checkValidity();
  }

  @Override
  public void onCommit(Iteration it, int tid, Object item) {
    checkValidity();
  }
}
