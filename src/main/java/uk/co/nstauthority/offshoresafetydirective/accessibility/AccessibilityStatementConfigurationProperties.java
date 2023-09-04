package uk.co.nstauthority.offshoresafetydirective.accessibility;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "accessibility")
@Validated
public record AccessibilityStatementConfigurationProperties(
    @NotEmpty String statementPreparedDate,
    @NotEmpty String statementLastReviewDate,
    @NotEmpty String serviceLastTestDate,
    @NotEmpty String serviceLastTestedBy,
    @NotEmpty String designSystemLastTestedOnDate) {
}