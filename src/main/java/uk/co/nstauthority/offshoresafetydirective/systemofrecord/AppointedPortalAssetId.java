package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public record AppointedPortalAssetId(String id) {

  public PortalAssetId toPortalAssetId() {
    return new PortalAssetId(id);
  }

}
