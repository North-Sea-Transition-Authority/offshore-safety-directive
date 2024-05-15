package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.saml1.core.AttributeValue;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.springframework.security.core.GrantedAuthority;

class SamlResponseParserTest {

  private SamlResponseParser samlResponseParser;

  @BeforeEach
  void setUp() {
    samlResponseParser = new SamlResponseParser();
  }

  @Test
  void parseSamlResponse() {

    var attributes = samlAttributeBuilder()
        .withWebUserAccountId("1")
        .withPersonId("2")
        .withForename("Forename")
        .withSurname("Surname")
        .withEmailAddress("email@address.com")
        .withPortalPrivileges("PRIV_ONE,PRIV_TWO,PRIV_THREE")
        .build();

    var samlResponse = createResponse(attributes);

    var authentication = samlResponseParser.parseSamlResponse(samlResponse);
    var userDetails = (ServiceUserDetail) authentication.getPrincipal();

    assertThat(userDetails.wuaId()).isEqualTo(1L);
    assertThat(userDetails.personId()).isEqualTo(2L);
    assertThat(userDetails.forename()).isEqualTo("Forename");
    assertThat(userDetails.surname()).isEqualTo("Surname");
    assertThat(userDetails.emailAddress()).isEqualTo("email@address.com");
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly(
            "PRIV_ONE",
            "PRIV_TWO",
            "PRIV_THREE"
        );
  }

  @Test
  void parseSamlResponse_missingAttributes() {
    var samlResponse = createResponse(List.of());
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseSamlResponse_whenWebUserAccountIdAttributeEmpty_thenException(String webUserAccountId) {

    var attributes = samlAttributeBuilder()
        .withWebUserAccountId(webUserAccountId)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseSamlResponse_whenPersonIdAttributeEmpty_thenException(String personId) {

    var attributes = samlAttributeBuilder()
        .withPersonId(personId)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseSamlResponse_whenForenameAttributeEmpty_thenException(String forename) {

    var attributes = samlAttributeBuilder()
        .withForename(forename)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseSamlResponse_whenSurnameAttributeEmpty_thenException(String surname) {

    var attributes = samlAttributeBuilder()
        .withSurname(surname)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseSamlResponse_whenEmailAttributeEmpty_thenException(String emailAddress) {

    var attributes = samlAttributeBuilder()
        .withEmailAddress(emailAddress)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @Test
  void parseSamlResponse_whenPrivilegesEmpty_thenAuthoritiesEmpty() {

    var attributes = samlAttributeBuilder()
        .withPortalPrivileges("")
        .build();

    var samlResponse = createResponse(attributes);
    assertThat(samlResponseParser.parseSamlResponse(samlResponse).getAuthorities()).isEmpty();
  }

  @Test
  void parseSamlResponse_whenPrivilegesNull_thenException() {

    var attributes = samlAttributeBuilder()
        .withEmailAddress(null)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @Test
  void parseSamlResponse_whenProxyUser_assertValues() {
    var attributes = samlAttributeBuilder()
        .withProxyUsername("user")
        .withProxyWuaId("2")
        .build();
    var samlResponse = createResponse(attributes);
    assertThat((ServiceUserDetail) samlResponseParser.parseSamlResponse(samlResponse).getPrincipal())
        .extracting(
            ServiceUserDetail::proxyUsername,
            ServiceUserDetail::proxyWuaId)
        .containsExactly(
            "user",
            2L
        );
  }

  @Test
  void parseSamlResponse_whenNullProxyValuesProvided_assertNulls() {
    var attributes = samlAttributeBuilder()
        .withProxyUsername(null)
        .withProxyWuaId(null)
        .build();
    var samlResponse = createResponse(attributes);
    assertThat((ServiceUserDetail) samlResponseParser.parseSamlResponse(samlResponse).getPrincipal())
        .extracting(
            ServiceUserDetail::proxyUsername,
            ServiceUserDetail::proxyWuaId)
        .containsExactly("", null);
  }

  private Response createResponse(List<Attribute> samlAttributes) {
    var samlResponse = new ResponseBuilder().buildObject();
    var samlAssertion = new AssertionBuilder().buildObject();
    var attributeStatement = new AttributeStatementBuilder().buildObject();
    attributeStatement.getAttributes().addAll(samlAttributes);
    samlAssertion.getAttributeStatements().add(attributeStatement);
    samlResponse.getAssertions().add(samlAssertion);
    return samlResponse;
  }

  static SamlAttributeTestBuilder samlAttributeBuilder() {
    return new SamlAttributeTestBuilder();
  }

  static class SamlAttributeTestBuilder {

    private String webUserAccountId = "1";
    private String personId = "2";
    private String forename = "Forename";
    private String surname = "Surname";
    private String emailAddress = "email@address.com";
    private String portalPrivilegeCsv = "PRIVILEGE_1";
    private String proxyUsername = "username";
    private String proxyWuaId = "1";

    private final List<Attribute> attributes = new ArrayList<>();

    private void addAttribute(EnergyPortalSamlAttribute samlAttribute, String value) {
      attributes.add(createSamlAttribute(samlAttribute, value));
    }

    SamlAttributeTestBuilder withWebUserAccountId(String webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    SamlAttributeTestBuilder withPersonId(String personId) {
      this.personId = personId;
      return this;
    }

    SamlAttributeTestBuilder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    SamlAttributeTestBuilder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    SamlAttributeTestBuilder withEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
      return this;
    }

    SamlAttributeTestBuilder withPortalPrivileges(String portalPrivilegeCsv) {
      this.portalPrivilegeCsv = portalPrivilegeCsv;
      return this;
    }

    SamlAttributeTestBuilder withProxyUsername(String proxyUsername) {
      this.proxyUsername = proxyUsername;
      return this;
    }

    SamlAttributeTestBuilder withProxyWuaId(String proxyWuaId) {
      this.proxyWuaId = proxyWuaId;
      return this;
    }

    List<Attribute> build() {
      addAttribute(EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID, webUserAccountId);
      addAttribute(EnergyPortalSamlAttribute.PERSON_ID, personId);
      addAttribute(EnergyPortalSamlAttribute.FORENAME, forename);
      addAttribute(EnergyPortalSamlAttribute.SURNAME, surname);
      addAttribute(EnergyPortalSamlAttribute.EMAIL_ADDRESS, emailAddress);
      addAttribute(EnergyPortalSamlAttribute.PORTAL_PRIVILEGES, portalPrivilegeCsv);
      addAttribute(EnergyPortalSamlAttribute.PROXY_USER_NAME, proxyUsername);
      addAttribute(EnergyPortalSamlAttribute.PROXY_USER_WUA_ID, proxyWuaId);
      return this.attributes;
    }

    private Attribute createSamlAttribute(EnergyPortalSamlAttribute samlAttribute, String value) {
      try {
        var attribute = new AttributeBuilder().buildObject();
        attribute.setName(samlAttribute.getAttributeName());

        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        var element = document.createElement(samlAttribute.getAttributeName());
        element.setTextContent(value);

        var attributeValue = new XSAnyBuilder().buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
        attributeValue.setDOM(element);

        attribute.getAttributeValues().add(attributeValue);

        return attribute;
      } catch (Exception e) {
        throw new RuntimeException("Failed to construct SAML attribute", e);
      }
    }
  }
}