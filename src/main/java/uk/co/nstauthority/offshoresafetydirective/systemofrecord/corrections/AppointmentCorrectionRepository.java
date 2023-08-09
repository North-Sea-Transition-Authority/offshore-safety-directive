package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;

@Repository
interface AppointmentCorrectionRepository extends CrudRepository<AppointmentCorrection, UUID> {

  List<AppointmentCorrection> findAllByAppointment(Appointment appointment);

  List<AppointmentCorrection> findAllByAppointment_IdIn(Collection<UUID> appointments);

}
