package uk.co.fivium.offshoresafetydirective.sns.message;

import org.junit.jupiter.api.Test;

class OsdEpmqMessagesTest {

  @Test
  void messageClassesExist() throws ClassNotFoundException {
    Class.forName("uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage");
    Class.forName("uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage");
  }
}
