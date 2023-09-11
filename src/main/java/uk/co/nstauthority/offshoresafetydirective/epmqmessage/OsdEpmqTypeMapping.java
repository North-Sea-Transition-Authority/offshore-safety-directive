package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;

public enum OsdEpmqTypeMapping {

  APPOINTMENT_CONFIRMED(
      AppointmentCreatedOsdEpmqMessage.TYPE,
      AppointmentCreatedOsdEpmqMessage.class,
      OsdEpmqTopics.APPOINTMENTS
  ),
  APPOINTMENT_TERMINATION(
      AppointmentTerminationOsdEpmqMessage.TYPE,
      AppointmentTerminationOsdEpmqMessage.class,
      OsdEpmqTopics.APPOINTMENTS
  ),
  NOMINATION_SUBMITTED(
      NominationSubmittedOsdEpmqMessage.TYPE,
      NominationSubmittedOsdEpmqMessage.class,
      OsdEpmqTopics.NOMINATIONS
  );

  private final String type;
  private final Class<? extends EpmqMessage> epmqMessageClass;
  private final OsdEpmqTopics osdEpmqTopic;

  OsdEpmqTypeMapping(String type, Class<? extends EpmqMessage> epmqMessageClass,
                     OsdEpmqTopics osdEpmqTopic) {
    this.type = type;
    this.epmqMessageClass = epmqMessageClass;
    this.osdEpmqTopic = osdEpmqTopic;
  }

  public static Map<String, Class<? extends EpmqMessage>> getTypeToClassMapByTopic(OsdEpmqTopics chosenOsdEpmqTopic) {
    return Arrays.stream(OsdEpmqTypeMapping.values())
        .filter(osdEpmqTypeMapping -> osdEpmqTypeMapping.osdEpmqTopic.equals(chosenOsdEpmqTopic))
        .collect(
            Collectors.toMap(
                osdEpmqTypeMapping -> osdEpmqTypeMapping.type,
                osdEpmqTypeMapping -> osdEpmqTypeMapping.epmqMessageClass
            ));
  }
}
