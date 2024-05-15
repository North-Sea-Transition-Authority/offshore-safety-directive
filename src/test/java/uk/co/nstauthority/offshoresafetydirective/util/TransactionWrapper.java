package uk.co.nstauthority.offshoresafetydirective.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionWrapper {

  /**
   * This is used to ensure a new transaction is used and therefore can be committed/rolled-back independently
   */
  public void runInNewTransaction(Runnable toRun) {
    toRun.run();
  }
}
