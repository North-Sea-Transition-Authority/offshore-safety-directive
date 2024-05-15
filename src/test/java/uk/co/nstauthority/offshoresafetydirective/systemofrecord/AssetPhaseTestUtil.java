package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class AssetPhaseTestUtil {

  private AssetPhaseTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();

    private Asset asset = AssetTestUtil.builder().build();

    private Appointment appointment = AppointmentTestUtil.builder().build();

    private String phase = "PHASE";

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withAsset(Asset asset) {
      this.asset = asset;
      return this;
    }

    public Builder withAppointment(Appointment appointment) {
      this.appointment = appointment;
      return this;
    }

    public Builder withPhase(String phase) {
      this.phase = phase;
      return this;
    }

    public AssetPhase build() {
      var assetPhase = new AssetPhase(id);
      assetPhase.setAsset(asset);
      assetPhase.setAppointment(appointment);
      assetPhase.setPhase(phase);
      return assetPhase;
    }
  }
}
