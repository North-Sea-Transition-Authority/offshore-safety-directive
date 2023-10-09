package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;
import uk.co.nstauthority.offshoresafetydirective.ApplicationIntegrationTest;

@ApplicationIntegrationTest
class PearsLicenceSqsServiceIntegrationTest {

  @MockBean
  private SqsService sqsService;

  @MockBean
  private SnsService snsService;

  @Autowired
  private PearsLicenceSqsService pearsLicenceSqsService;

  @Test
  void receiveMessages_verifySchedule() {
    var expectedSchedule = 5L;
    var expectedInvocations = 2;
    var maxTimeInSeconds = expectedSchedule * (expectedInvocations + 1);
    var counter = new AtomicInteger();

    doAnswer(invocation -> {
      counter.incrementAndGet();
      return invocation;
    }).when(sqsService).receiveQueueMessages(
        any(),
        eq(PearsCorrectionAppliedEpmqMessage.class),
        any()
    );

    await()
        .atMost(maxTimeInSeconds, TimeUnit.SECONDS)
        .untilAtomic(counter, Matchers.is(expectedInvocations));
  }

}