package uk.co.nstauthority.offshoresafetydirective.authentication;

enum EnergyPortalSamlAttribute {

  WEB_USER_ACCOUNT_ID("WUA_ID"),
  PERSON_ID("PERSON_ID"),
  FORENAME("FORENAME"),
  SURNAME("SURNAME"),
  EMAIL_ADDRESS("PRIMARY_EMAIL_ADDRESS"),
  PORTAL_PRIVILEGES("PRIVILEGES");

  private final String attributeName;

  EnergyPortalSamlAttribute(String attributeName) {
    this.attributeName = attributeName;
  }

  String getAttributeName() {
    return attributeName;
  }
}
