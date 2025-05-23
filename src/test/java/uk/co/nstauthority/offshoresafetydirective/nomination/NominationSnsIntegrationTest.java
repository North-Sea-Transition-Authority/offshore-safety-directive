package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqTopics;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSubmissionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.util.TransactionWrapper;

@DatabaseIntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NominationSnsIntegrationTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private NominationSubmissionService nominationSubmissionService;

  @Autowired
  private TransactionWrapper transactionWrapper;

  @Autowired
  private SnsService snsService;

  @MockitoBean
  private NominationSnsQueryService nominationSnsQueryService;

  @MockitoSpyBean
  private NominationDetailService nominationDetailService;

  @Test
  void submittingNominationPublishesSnsMessage() {
    SamlAuthenticationUtil.Builder().setSecurityContext();

    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .build();

    transactionWrapper.runInNewTransaction(() -> {
      persistAndFlush(nomination);
      persistAndFlush(nominationDetail);
    });

    var nominationSnsDto = NominationSnsDtoTestUtil.builder().build();
    when(nominationSnsQueryService.getNominationSnsDto(nominationDetail))
        .thenReturn(nominationSnsDto);

    when(nominationDetailService.getLatestNominationDetail(new NominationId(nominationDetail)))
        .thenReturn(nominationDetail);

    var submissionForm = new NominationSubmissionForm();
    submissionForm.setConfirmedAuthority(Boolean.TRUE.toString());
    nominationSubmissionService.submitNomination(nominationDetail, submissionForm);

    var nominationsTopicArn = snsService.getOrCreateTopic(OsdEpmqTopics.NOMINATIONS.getName());

    var nominationSubmittedMessageArgumentCaptor = ArgumentCaptor.forClass(NominationSubmittedOsdEpmqMessage.class);

    Awaitility.waitAtMost(Duration.ofSeconds(10))
        .untilAsserted(() -> verify(snsService).publishMessage(eq(nominationsTopicArn), nominationSubmittedMessageArgumentCaptor.capture()));

    var publishedMessage = nominationSubmittedMessageArgumentCaptor.getValue();

    assertThat(publishedMessage)
        .extracting(
            NominationSubmittedOsdEpmqMessage::getNominationId,
            NominationSubmittedOsdEpmqMessage::getNominationReference,
            NominationSubmittedOsdEpmqMessage::getApplicantOrganisationUnitId,
            NominationSubmittedOsdEpmqMessage::getNominatedOrganisationUnitId,
            NominationSubmittedOsdEpmqMessage::getNominationAssetType,
            NominationSubmittedOsdEpmqMessage::getCorrelationId
        )
            .containsExactly(
                nomination.getId(),
                nomination.getReference(),
                nominationSnsDto.applicantOrganisationUnitId(),
                nominationSnsDto.nominatedOrganisationUnitId(),
                NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
                    nominationSnsDto.wellSelectionType(),
                    NominationHasInstallations.fromBoolean(nominationSnsDto.hasInstallations())
                ),
                correlationId
            );
  }

  @Test
  void submittingNominationWhenTransactionRolledBackDoesNotPublishSnsMessage() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .build();

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .build();

    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .build();

    transactionWrapper.runInNewTransaction(() -> {
      persistAndFlush(nomination);
      persistAndFlush(nominationDetail);
      persistAndFlush(relatedInformation);
      persistAndFlush(applicantDetail);
    });

    try {
      transactionWrapper.runInNewTransaction(() -> {
        nominationSubmissionService.submitNomination(nominationDetail, new NominationSubmissionForm());

        throw new RuntimeException("Test exception");
      });
    } catch (RuntimeException exception) {
      // Ignore
    }

    verify(snsService, never()).publishMessage(any(), any());
  }

  private void persistAndFlush(Object entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }
}
