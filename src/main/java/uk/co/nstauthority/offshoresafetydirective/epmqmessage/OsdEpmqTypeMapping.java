package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;

public enum OsdEpmqTypeMapping {

  APPOINTMENT_CREATED(
      AppointmentCreatedOsdEpmqMessage.TYPE,
      AppointmentCreatedOsdEpmqMessage.class,
      OsdEpmqTopics.APPOINTMENTS
  ),
  APPOINTMENT_DELETED(
      AppointmentDeletedOsdEpmqMessage.TYPE,
      AppointmentDeletedOsdEpmqMessage.class,
      OsdEpmqTopics.APPOINTMENTS
  ),
  APPOINTMENT_UPDATED(
      AppointmentUpdatedOsdEpmqMessage.TYPE,
      AppointmentUpdatedOsdEpmqMessage.class,
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
