package uk.co.nstauthority.offshoresafetydirective.configuration;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "saml")
@Validated
class SamlProperties {

  @NotNull
  private String registrationId;

  @NotNull
  private String entityId;

  @NotNull
  private String certificateLocation;

  @NotNull
  private String loginUrl;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getCertificateLocation() {
    return certificateLocation;
  }

  public void setCertificateLocation(String certificateLocation) {
    this.certificateLocation = certificateLocation;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
  }

}

