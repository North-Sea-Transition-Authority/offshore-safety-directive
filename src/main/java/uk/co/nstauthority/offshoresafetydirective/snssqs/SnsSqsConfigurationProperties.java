package uk.co.nstauthority.offshoresafetydirective.snssqs;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sns-sqs")
@Validated
public record SnsSqsConfigurationProperties(
    @NotNull String accessKeyId,
    @NotNull String secretAccessKey,
    @NotNull String regionId,
    @NotNull String environmentSuffix
) {
}
