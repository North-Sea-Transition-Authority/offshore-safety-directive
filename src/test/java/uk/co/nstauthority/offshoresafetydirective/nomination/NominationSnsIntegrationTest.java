package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.NominationSubmittedOsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.util.TransactionWrapper;

@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NominationSnsIntegrationTest {

  @Autowired
  private TestEntityManager testEntityManager;

  @Autowired
  private NominationSubmissionService nominationSubmissionService;

  @Autowired
  private SnsService snsService;

  @Autowired
  private SnsClient snsClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TransactionWrapper transactionWrapper;

  @Test
  void submittingNominationPublishesSnsMessage() throws JsonProcessingException {
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

    transactionWrapper.runInNewTransaction(() -> {
      testEntityManager.persistAndFlush(nomination);
      testEntityManager.persistAndFlush(nominationDetail);
      testEntityManager.persistAndFlush(relatedInformation);
    });

    nominationSubmissionService.submitNomination(nominationDetail);

    var nominationsTopicArn = snsService.getOrCreateTopic(NominationSnsService.NOMINATIONS_TOPIC_NAME);

    var expectedMessage =
        objectMapper.writeValueAsString(new NominationSubmittedOsdEpmqMessage(nomination.getId(), correlationId));

    verify(snsClient).publish(
        PublishRequest.builder()
            .topicArn(nominationsTopicArn.arn())
            .message(expectedMessage)
            .messageDeduplicationId(any())
            .messageGroupId(nominationsTopicArn.arn())
            .build()
    );
  }

  @Test
  void submittingNominationWhenTransactionRolledBackDoesNotPublishSnsMessage() throws JsonProcessingException {
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

    transactionWrapper.runInNewTransaction(() -> {
      testEntityManager.persistAndFlush(nomination);
      testEntityManager.persistAndFlush(nominationDetail);
      testEntityManager.persistAndFlush(relatedInformation);
    });

    try {
      transactionWrapper.runInNewTransaction(() -> {
        nominationSubmissionService.submitNomination(nominationDetail);

        throw new RuntimeException("Test exception");
      });
    } catch (RuntimeException exception) {
      // Ignore
    }

    verify(snsClient, never()).publish(any(PublishRequest.class));
  }
}
