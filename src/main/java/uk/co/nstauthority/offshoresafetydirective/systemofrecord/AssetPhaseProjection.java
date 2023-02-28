package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

public interface AssetPhaseProjection {

  @Value("#{target.asset.id}")
  UUID getAssetId();

  @Value("#{target.appointment.id}")
  UUID getAppointmentId();

  String getPhase();
}
