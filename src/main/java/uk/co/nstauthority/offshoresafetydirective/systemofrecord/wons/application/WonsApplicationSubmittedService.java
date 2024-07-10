package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons.application;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons.WellboreLinkedAppointmentService;

@Service
public class WonsApplicationSubmittedService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WonsApplicationSubmittedService.class);

  private final AppointmentAccessService appointmentAccessService;
  private final WellQueryService wellQueryService;
  private final WellboreLinkedAppointmentService wellboreLinkedAppointmentService;

  @Autowired
  public WonsApplicationSubmittedService(AppointmentAccessService appointmentAccessService,
                                         WellQueryService wellQueryService,
                                         WellboreLinkedAppointmentService wellboreLinkedAppointmentService) {
    this.appointmentAccessService = appointmentAccessService;
    this.wellQueryService = wellQueryService;
    this.wellboreLinkedAppointmentService = wellboreLinkedAppointmentService;
  }

  @Transactional
  public void processApplicationSubmittedEvent(Integer applicationId, Integer wellboreId,
                                               String forwardAreaApprovalAppointmentString) {

    if (StringUtils.isBlank(forwardAreaApprovalAppointmentString)) {
      LOGGER.info("""
          WONS application {} for wellbore {} is not using a forward area approval appointment.
          No appointment will be created for this wellbore.
          """, applicationId, wellboreId
      );
      return;
    }

    UUID forwardAreaApprovalAppointmentId;

    try {
      forwardAreaApprovalAppointmentId = UUID.fromString(forwardAreaApprovalAppointmentString);
    } catch (IllegalArgumentException exception) {
      LOGGER.error("""
          Wons application {} for wellbore {} is using a forward area approval appointment but the ID provided is not
          a valid UUID, {}. No appointment will be created for this wellbore. Is there a bug in the WONS code
          suggesting appointments?
          """, applicationId, wellboreId, forwardAreaApprovalAppointmentString);
      return;
    }

    Optional<Appointment> forwardAreaApprovalAppointmentOptional = appointmentAccessService
        .getAppointment(new AppointmentId(forwardAreaApprovalAppointmentId));

    if (forwardAreaApprovalAppointmentOptional.isEmpty()) {
      LOGGER.error("""
          WONS application with ID {} submitted using forward area approval appointment {} but no
          appointment exists with that ID. No appointment for wellbore {} created.
          Is there a bug in the WONS application code for suggesting valid forward approval appointments?
          """, applicationId, forwardAreaApprovalAppointmentId, wellboreId
      );
      return;
    }

    var forwardAreaApprovalAppointment = forwardAreaApprovalAppointmentOptional.get();

    if (forwardAreaApprovalAppointment.getResponsibleToDate() != null) {
      LOGGER.error("""
          WONS application with ID {} submitted using forward area approval appointment {} but appointment is not
          active. No appointment for wellbore {} created. Is there a bug in the WONS application code for suggesting
          valid forward area approval appointments?
          """, applicationId, forwardAreaApprovalAppointmentId, wellboreId);
      return;
    }

    WellDto wellbore = wellQueryService
        .getWell(new WellboreId(wellboreId), new RequestPurpose("Get wellbore from application submitted event"))
        .orElseThrow(() -> new IllegalArgumentException(
            "Unable to find wellbore from WONS with ID %d".formatted(wellboreId)
        ));

    if (wellboreLinkedAppointmentService.appointmentDoesNotCoverWellboreIntent(
        wellbore,
        forwardAreaApprovalAppointment
    )) {
      LOGGER.error("""
          WONS application with ID {} submitted using forward area approval appointment {} but appointment does not
          cover the {} intent of the wellbore. No appointment for wellbore {} created. Is there a bug in the WONS
          application code for suggesting valid forward area approval appointments?
          """, applicationId, forwardAreaApprovalAppointmentId, wellbore.intent(), wellboreId);
      return;
    }

    wellboreLinkedAppointmentService
        .createLinkedWellboreAppointment(wellbore, forwardAreaApprovalAppointment, AppointmentType.FORWARD_APPROVED);
  }
}
