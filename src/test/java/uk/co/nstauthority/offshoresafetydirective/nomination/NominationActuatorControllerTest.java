package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;

class NominationActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT =
      "/actuator/nominations/publish-epmq-message/nomination/%s";

  @MockBean
  private NominationSnsService nominationSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @Test
  void publishNominationSubmittedMessage() throws Exception {
    var nominationId = new NominationId(UUID.randomUUID());

    mockMvc
        .perform(
            post(PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(nominationId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isOk());

    verify(nominationSnsService).publishNominationSubmittedMessage(nominationId);
  }

  @SecurityTest
  void publishNominationSubmittedMessage_notAuthorised() throws Exception {
    var nominationId = new NominationId(UUID.randomUUID());

    mockMvc
        .perform(
            post(PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(nominationId.id()))
                .with(httpBasic("admin", "invalidpassword"))
        )
        .andExpect(status().isUnauthorized());
  }
}
