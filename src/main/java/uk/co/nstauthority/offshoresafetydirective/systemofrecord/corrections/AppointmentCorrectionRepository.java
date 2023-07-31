package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentCorrectionRepository extends CrudRepository<AppointmentCorrection, UUID> {

}
