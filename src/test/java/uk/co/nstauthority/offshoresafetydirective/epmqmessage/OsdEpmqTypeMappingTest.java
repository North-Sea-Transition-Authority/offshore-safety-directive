package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class OsdEpmqTypeMappingTest {

  @Test
  void getTypeToClassMapByTopic_nominations() {
    var typeToClassMap = OsdEpmqTypeMapping.getTypeToClassMapByTopic(OsdEpmqTopics.NOMINATIONS);
    var values = Set.copyOf(typeToClassMap.values());

    assertThat(typeToClassMap.keySet()).isEqualTo(Set.of(NominationSubmittedOsdEpmqMessage.TYPE));
    assertThat(values).isEqualTo(Set.of(NominationSubmittedOsdEpmqMessage.class));
  }

  @Test
  void getTypeToClassMapByTopic_appointments() {
    var typeToClassMap = OsdEpmqTypeMapping.getTypeToClassMapByTopic(OsdEpmqTopics.APPOINTMENTS);
    var values = Set.copyOf(typeToClassMap.values());

    assertThat(typeToClassMap.keySet())
        .isEqualTo(
          Set.of(
              AppointmentCreatedOsdEpmqMessage.TYPE,
              AppointmentDeletedOsdEpmqMessage.TYPE
          ));

    assertThat(values)
        .isEqualTo(
          Set.of(
              AppointmentCreatedOsdEpmqMessage.class,
              AppointmentDeletedOsdEpmqMessage.class
          ));
  }
}