package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;

class NominationActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT =
      "/actuator/nominations/publish-epmq-message/nomination/%d";

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private NominationSnsService nominationSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @Test
  void publishNominationSubmittedMessage() throws Exception {
    var nominationId = new NominationId(1);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(nominationId))
        .thenReturn(nominationDetail);

    mockMvc
        .perform(
            post(PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(nominationId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isOk());

    verify(nominationSnsService).publishNominationSubmittedMessage(nominationDetail);
  }

  @Test
  void publishNominationSubmittedMessage_nominationNotFound() throws Exception {
    var nominationId = new NominationId(1);

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenThrow(OsdEntityNotFoundException.class);

    mockMvc
        .perform(
            post(PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(nominationId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isNotFound());

    verify(nominationSnsService, never()).publishNominationSubmittedMessage(any());
  }

  @SecurityTest
  void publishNominationSubmittedMessage_notAuthorised() throws Exception {
    var nominationId = new NominationId(1);

    mockMvc
        .perform(
            post(PUBLISH_NOMINATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(nominationId.id()))
                .with(httpBasic("admin", "invalidpassword"))
        )
        .andExpect(status().isUnauthorized());
  }
}
