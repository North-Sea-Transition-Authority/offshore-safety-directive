package uk.co.nstauthority.offshoresafetydirective.util;

import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.verification.VerificationMode;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class MockitoUtil {

  private MockitoUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static VerificationMode onlyOnce() {
    return VerificationModeFactory.times(1);
  }
}
