package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

public abstract class SubareaDto {

  private final LicenceBlockSubareaId licenceBlockSubareaId;

  SubareaDto(LicenceBlockSubareaId licenceBlockSubareaId) {
    this.licenceBlockSubareaId = licenceBlockSubareaId;
  }

  public LicenceBlockSubareaId subareaId() {
    return licenceBlockSubareaId;
  }
}
