package uk.co.fivium.offshoresafetydirective.sns.message;

import org.junit.jupiter.api.Test;

class OsdEpmqMessagesJarTest {

  @Test
  void classesExist() throws ClassNotFoundException {
    Class.forName("uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics");
    Class.forName("uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage");
    Class.forName("uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage");
  }
}
