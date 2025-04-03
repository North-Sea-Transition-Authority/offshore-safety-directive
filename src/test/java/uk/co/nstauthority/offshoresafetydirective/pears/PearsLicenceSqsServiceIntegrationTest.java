package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessageTypeMapping;
import uk.co.fivium.energyportalmessagequeue.message.EpmqTopics;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;

@DatabaseIntegrationTest
@SpringBootTest(properties = { "epmq.message-poll-interval-seconds = 1" })
class PearsLicenceSqsServiceIntegrationTest {

  @MockitoBean
  private SqsService sqsService;

  @MockitoBean
  protected SnsService snsService;

  @Test
  void receiveMessages_verifySchedule() {
    // after 10 seconds, ensure that the method has been invoked multiple times
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() ->
            verify(sqsService, atLeast(2)).receiveQueueMessages(
                any(),
                eq(EpmqMessageTypeMapping.getTypeToClassMapByTopic(EpmqTopics.PEARS_LICENCES)),
                any()
            )
        );
  }
}