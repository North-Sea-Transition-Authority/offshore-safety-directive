package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

record Licence(LicenceType licenceType, LicenceNumber licenceNumber, LicenceReference licenceReference) {

  record LicenceType(String value) {}

  record LicenceNumber(Integer value) {}

  record LicenceReference(String value) {}
}
