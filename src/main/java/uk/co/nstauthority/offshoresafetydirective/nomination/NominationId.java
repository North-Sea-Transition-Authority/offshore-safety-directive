package uk.co.nstauthority.offshoresafetydirective.nomination;

public record NominationId(int id) {

  public NominationId(NominationDetail nominationDetail) {
    this(nominationDetail.getNomination().getId());
  }

  /**
   * Method provided so NominationId objects can be annotated as @PathVariable in controllers. Spring will
   * resolve by passing a string representation of the NominationId object to this method. This removes the need
   * to have a converter or argument resolver.
   * @param value The string representation of a NominationId object
   * @return a NominationId object converted from the string representation
   */
  public static NominationId valueOf(String value) {
    return new NominationId(Integer.parseInt(value));
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
