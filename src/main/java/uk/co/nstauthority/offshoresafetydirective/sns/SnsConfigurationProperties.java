package uk.co.nstauthority.offshoresafetydirective.sns;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sns")
@Validated
record SnsConfigurationProperties(
    @NotNull String accessKeyId,
    @NotNull String secretAccessKey,
    @NotNull String regionId,
    @NotNull String topicSuffix
) {
}
