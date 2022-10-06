package uk.co.nstauthority.offshoresafetydirective.configuration;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "saml")
@Validated
public class SamlProperties {

  @NotNull
  private String registrationId;

  @NotNull
  private String entityId;

  @NotNull
  private String certificate;

  @NotNull
  private String loginUrl;

  @NotNull
  private String consumerServiceLocation;

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

  public String getCertificate() {
    return certificate;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
  }

  public String getConsumerServiceLocation() {
    return consumerServiceLocation;
  }

  public void setConsumerServiceLocation(String consumerServiceLocation) {
    this.consumerServiceLocation = consumerServiceLocation;
  }
}

