package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;

@Service
class PearsLicenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PearsLicenceService.class);

  private final LicenceQueryService licenceQueryService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final AppointmentService appointmentService;


  @Autowired
  PearsLicenceService(LicenceQueryService licenceQueryService,
                      LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                      AppointmentService appointmentService) {
    this.licenceQueryService = licenceQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.appointmentService = appointmentService;
  }

  public void handlePearsCorrectionApplied(PearsCorrectionAppliedEpmqMessage message) {
    var licenceId = Integer.parseInt(message.getLicenceId());
    var optionalLicence = licenceQueryService.getLicenceById(new LicenceId(licenceId));
    if (optionalLicence.isEmpty()) {
      LOGGER.error(
          "No licence [{}] found for PEARS correction with id [{}]",
          message.getLicenceId(),
          message.getCorrectionId()
      );
      return;
    }
    var licence = optionalLicence.get();

    List<LicenceBlockSubareaDto> nonExtantSubareas = Optional.ofNullable(licence.licenceReference())
        .map(LicenceDto.LicenceReference::value)
        .map(reference -> licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
            reference,
            List.of(SubareaStatus.NOT_EXTANT)
        ))
        .orElse(List.of());

    if (!nonExtantSubareas.isEmpty()) {
      appointmentService.endAppointmentsForSubareas(nonExtantSubareas);
    }
  }

}
