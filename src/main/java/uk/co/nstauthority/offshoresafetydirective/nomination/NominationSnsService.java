package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsService;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;

@Service
class NominationSnsService {

  private static final String NOMINATION_TOPIC_NAME = "osd-nominations";

  private final SnsService snsService;
  private final SnsTopicArn nominationTopicArn;

  @Autowired
  NominationSnsService(SnsService snsService) {
    this.snsService = snsService;

    nominationTopicArn = snsService.getOrCreateTopic(NOMINATION_TOPIC_NAME);
  }
}
