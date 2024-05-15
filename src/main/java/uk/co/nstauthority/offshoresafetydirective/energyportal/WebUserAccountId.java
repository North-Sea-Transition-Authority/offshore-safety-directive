package uk.co.nstauthority.offshoresafetydirective.energyportal;

import java.io.Serial;
import java.io.Serializable;

public record WebUserAccountId(long id) implements Serializable {

  @Serial
  private static final long serialVersionUID = -1836128817324202878L;

  public static WebUserAccountId valueOf(String value) {
    return new WebUserAccountId(Long.parseLong(value));
  }

  public int toInt() {
    return ((Long) id).intValue();
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
