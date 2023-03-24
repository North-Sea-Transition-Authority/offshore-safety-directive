package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

public abstract class OsdEpmqMessage {

  private String service = "OSD";
  private String type;

  OsdEpmqMessage(String type) {
    this.type = type;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
