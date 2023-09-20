package uk.co.nstauthority.offshoresafetydirective.authentication;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class SamlResponseParser {

  public ServiceSaml2Authentication parseSamlResponse(Response response) {
    var attributes = getSamlAttributes(response);
    var parsedAttributes = parseAttributes(attributes);

    var wuaId = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID);
    var personId = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.PERSON_ID);
    var forename = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.FORENAME);
    var surname = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.SURNAME);
    var email = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.EMAIL_ADDRESS);
    var proxyWuaId = parsedAttributes.getOrDefault(
        EnergyPortalSamlAttribute.PROXY_USER_WUA_ID.getAttributeName(),
        null
    );
    var proxyUserName = parsedAttributes.getOrDefault(
        EnergyPortalSamlAttribute.PROXY_USER_NAME.getAttributeName(),
        null
    );

    var userDetail = new ServiceUserDetail(
        Long.parseLong(wuaId),
        Long.parseLong(personId),
        forename,
        surname,
        email,
        Optional.ofNullable(proxyWuaId)
            .filter(StringUtils::isNotBlank)
            .map(Long::parseLong)
            .orElse(null),
        proxyUserName
    );

    var portalPrivileges = getNonNullAttribute(parsedAttributes, EnergyPortalSamlAttribute.PORTAL_PRIVILEGES);

    var grantedAuthorities =  Stream.of(StringUtils.split(portalPrivileges, ","))
        .map(SimpleGrantedAuthority::new)
        .toList();

    return new ServiceSaml2Authentication(userDetail, grantedAuthorities);
  }

  private String getNonEmptyAttribute(Map<String, String> parsedAttributes, EnergyPortalSamlAttribute samlAttribute) {
    return ObjectUtils.requireNonEmpty(parsedAttributes.get(samlAttribute.getAttributeName()));
  }

  private String getNonNullAttribute(Map<String, String> parsedAttributes, EnergyPortalSamlAttribute samlAttribute) {
    return Objects.requireNonNull(parsedAttributes.get(samlAttribute.getAttributeName()));
  }

  private List<Attribute> getSamlAttributes(Response response) {
    var assertions = response.getAssertions();
    if (assertions.size() != 1) {
      throw new SamlResponseException(String.format("SAML response contained %s assertions, expected 1", assertions.size()));
    }
    var attributeStatements = assertions.get(0).getAttributeStatements();
    if (attributeStatements.size() != 1) {
      throw new SamlResponseException(
          String.format("SAML response contained %s attribute statements, expected 1", attributeStatements.size())
      );
    }
    return attributeStatements.get(0).getAttributes();
  }

  private Map<String, String> parseAttributes(List<Attribute> attributes) {
    return attributes.stream()
        .collect(Collectors.toMap(Attribute::getName, this::getAttributeValue));
  }

  private String getAttributeValue(Attribute attribute) {
    return Objects.requireNonNull(
        attribute.getAttributeValues().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No values present for attribute '%s'", attribute.getName())))
            .getDOM()
    ).getTextContent();
  }

}
