package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetPhaseRepository extends CrudRepository<AssetPhase, UUID> {

  List<AssetPhase> findByAsset_Id(UUID assetId);

  List<AssetPhase> findByAppointment(Appointment appointment);

  List<AssetPhase> findByAppointmentIn(Collection<Appointment> appointments);

  List<AssetPhase> findAllByAppointment(Appointment appointment);

  List<AssetPhase> findAllByAppointment_IdIn(Collection<UUID> appointments);

}
