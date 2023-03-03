package uk.co.nstauthority.offshoresafetydirective.sns;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

@Service
public class SnsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SnsService.class);

  private final SnsClient snsClient;
  private final String topicSuffix;

  @Autowired
  SnsService(SnsClient snsClient, SnsConfigurationProperties snsConfigurationProperties) {
    this.snsClient = snsClient;
    this.topicSuffix = snsConfigurationProperties.topicSuffix();
  }

  public SnsTopicArn getOrCreateTopic(String baseName) {
    var name = baseName + topicSuffix + ".fifo";
    var createTopicResponse = snsClient.createTopic(CreateTopicRequest.builder()
        .name(name)
        .attributes(Map.of("FifoTopic", "true"))
        .build());
    var topicArn = createTopicResponse.topicArn();
    LOGGER.info("Created SNS topic: {} (ARN: {})", name, topicArn);
    return new SnsTopicArn(topicArn);
  }
}
