package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public record PortalAssetId(String id) {

  /**
   * Method provided so PortalAssetId objects can be annotated as @PathVariable in controllers. Spring will
   * resolve by passing a string representation of the PortalAssetId object to this method. This removes the need
   * to have a converter or argument resolver.
   * @param value The string representation of a PortalAssetId object
   * @return a PortalAssetId object converted from the string representation
   */
  public static PortalAssetId valueOf(String value) {
    return new PortalAssetId(value);
  }

  @Override
  public String toString() {
    return id;
  }
}
