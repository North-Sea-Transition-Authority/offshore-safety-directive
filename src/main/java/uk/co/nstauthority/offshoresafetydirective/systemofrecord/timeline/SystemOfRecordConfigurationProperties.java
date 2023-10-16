package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "system-of-record")
@Validated
public record SystemOfRecordConfigurationProperties(@NotEmpty String offlineNominationDocumentUrl) {

}
