package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.geological.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.mechanical.WonsMechicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.respud.WonsRespudNotificationCompletedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class WonsNotificationCompletedService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WonsNotificationCompletedService.class);
  private final AssetAccessService assetAccessService;
  private final AppointmentAccessService appointmentAccessService;
  private final WellQueryService wellQueryService;
  private final WellboreLinkedAppointmentService wellboreLinkedAppointmentService;

  WonsNotificationCompletedService(AssetAccessService assetAccessService,
                                   AppointmentAccessService appointmentAccessService,
                                   WellQueryService wellQueryService,
                                   WellboreLinkedAppointmentService wellboreLinkedAppointmentService) {
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.wellQueryService = wellQueryService;
    this.wellboreLinkedAppointmentService = wellboreLinkedAppointmentService;
  }

  @Transactional
  public void processParentWellboreNotification(WonsGeologicalSidetrackNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  @Transactional
  public void processParentWellboreNotification(WonsMechicalSidetrackNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  @Transactional
  public void processParentWellboreNotification(WonsRespudNotificationCompletedEpmqMessage message) {
    processParentWellboreNotification(
        message.getNotificationId(),
        message.getCreatedWellboreId(),
        message.getSubmittedOnWellboreId(),
        message.isUsingParentWellboreAppointment()
    );
  }

  private void processParentWellboreNotification(String notificationId, Integer targetWellboreId,
                                                 Integer parentWellboreId, boolean usingParentWellboreAppointment) {
    if (!usingParentWellboreAppointment) {
      LOGGER.info(
          "Wellbore {} resulting from notification {} is not using the parent wellbore appointment. " +
              "No appointment will be created.",
          targetWellboreId,
          notificationId
      );
      return;
    }

    Optional<Appointment> parentAppointmentOptional = assetAccessService.getExtantAsset(
            new PortalAssetId(parentWellboreId.toString()),
            PortalAssetType.WELLBORE
        )
        .flatMap(assetDto -> appointmentAccessService.getCurrentAppointmentForAsset(assetDto.assetId()));

    if (parentAppointmentOptional.isEmpty()) {
      LOGGER.info(
          "The parent wellbore {} of wellbore {} resulting from notification {} " +
              "does not have an active appointment. No appointment for wellbore {} will be created.",
          parentWellboreId,
          targetWellboreId,
          notificationId,
          targetWellboreId
      );
      return;
    }

    Appointment parentAppointment = parentAppointmentOptional.get();

    // need to check the intent of the child well from EPA and see parent appointment covers those phases
    WellDto childWellbore = wellQueryService
        .getWell(new WellboreId(targetWellboreId), new RequestPurpose("Get target well from notification event"))
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find target wellbore from WONS with ID %d".formatted(targetWellboreId)
        ));

    if (wellboreLinkedAppointmentService.appointmentDoesNotCoverWellboreIntent(childWellbore, parentAppointment)) {
      LOGGER.info("""
          The parent wellbore appointment with ID {} does not cover the intent of the child wellbore with ID {}.
          No appointment will be created for the child wellbore
          """, parentAppointment.getId(), childWellbore.wellboreId().id());

      return;
    }

    wellboreLinkedAppointmentService
        .createLinkedWellboreAppointment(childWellbore, parentAppointment, AppointmentType.PARENT_WELLBORE);
  }
}
