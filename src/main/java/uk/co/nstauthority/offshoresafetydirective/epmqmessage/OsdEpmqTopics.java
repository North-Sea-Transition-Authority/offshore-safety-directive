package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

public enum OsdEpmqTopics {

  NOMINATIONS("osd-nominations"),
  APPOINTMENTS("osd-appointments");

  private final String name;

  OsdEpmqTopics(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
