package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;

@Repository
interface AppointmentTerminationRepository extends CrudRepository<AppointmentTermination, UUID> {
  Optional<AppointmentTermination> findTerminationByAppointment(Appointment appointment);

  List<AppointmentTermination> findByAppointmentIn(List<Appointment> appointments);
}
