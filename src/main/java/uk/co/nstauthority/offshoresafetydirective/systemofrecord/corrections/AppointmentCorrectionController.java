package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/correct")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class AppointmentCorrectionController {

  private final AppointmentAccessService appointmentAccessService;
  private final PortalAssetNameService portalAssetNameService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final AppointmentCorrectionValidator appointmentCorrectionValidator;
  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public AppointmentCorrectionController(AppointmentAccessService appointmentAccessService,
                                         PortalAssetNameService portalAssetNameService,
                                         PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                         AppointmentCorrectionService appointmentCorrectionService,
                                         AppointmentCorrectionValidator appointmentCorrectionValidator,
                                         ControllerHelperService controllerHelperService,
                                         NominationDetailService nominationDetailService) {
    this.appointmentAccessService = appointmentAccessService;
    this.portalAssetNameService = portalAssetNameService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentCorrectionValidator = appointmentCorrectionValidator;
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView renderCorrection(@PathVariable AppointmentId appointmentId) {

    var appointment = getAppointment(appointmentId);
    var form = appointmentCorrectionService.getForm(appointment);

    return getModelAndView(appointment, form);
  }

  @PostMapping
  public ModelAndView submitCorrection(@PathVariable AppointmentId appointmentId,
                                       @Nullable @ModelAttribute("form") AppointmentCorrectionForm form,
                                       @Nullable BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

    var appointment = getAppointment(appointmentId);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var validatorHint = new AppointmentCorrectionValidationHint(appointmentDto);

    appointmentCorrectionValidator.validate(form, bindingResult, validatorHint);

    return controllerHelperService.checkErrorsAndRedirect(
        Objects.requireNonNull(bindingResult),
        getModelAndView(appointment, form),
        form,
        () -> {
          appointmentCorrectionService.updateCorrection(appointment, Objects.requireNonNull(form));

          var assetName = getAssetName(appointmentDto.assetDto());
          var notificationBanner = NotificationBanner.builder()
              .withBannerType(NotificationBannerType.SUCCESS)
              .withHeading("Corrected appointment for %s".formatted(assetName.value()))
              .build();

          NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

          return getSubmitRedirectRoute(appointmentDto);
        });
  }

  private ModelAndView getSubmitRedirectRoute(AppointmentDto appointmentDto) {
    return switch (appointmentDto.assetDto().portalAssetType()) {
      case INSTALLATION -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderInstallationAppointmentTimeline(appointmentDto.assetDto().portalAssetId()));
      case WELLBORE -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderWellboreAppointmentTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.redirect(on(AppointmentTimelineController.class)
          .renderSubareaAppointmentTimeline(appointmentDto.assetDto().portalAssetId()));
    };
  }

  private ModelAndView getModelAndView(Appointment appointment,
                                       AppointmentCorrectionForm form) {

    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var assetDto = appointmentDto.assetDto();
    var assetName = getAssetName(assetDto);

    var appointmentTypes = DisplayableEnumOptionUtil.getDisplayableOptions(AppointmentType.class);

    var modelAndView = new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("assetName", assetName.value())
        .addObject("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName())
        .addObject("assetTypeSentenceCaseDisplayName", assetDto.portalAssetType().getSentenceCaseDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(AppointmentCorrectionController.class)
            .submitCorrection(appointmentDto.appointmentId(), null, null, null)))
        .addObject("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null)))
        .addObject("preselectedOperator", getPreselectedOperator(form))
        .addObject("phases", appointmentCorrectionService.getSelectablePhaseMap(assetDto))
        .addObject("appointmentTypes", appointmentTypes)
        .addObject("form", form)
        .addObject(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        );

    if (AppointmentType.ONLINE_NOMINATION.name().equals(form.getAppointmentType())
        && form.getOnlineNominationReference() != null) {
      nominationDetailService.getLatestNominationDetailOptional(
          new NominationId(form.getOnlineNominationReference())
      ).ifPresent(nominationDetail -> {
        var nomination = nominationDetail.getNomination();
        modelAndView.addObject("preselectedNominationReference", Map.of(
            nomination.getId(),
            nomination.getReference()
        ));
      });
    }

    if (PortalAssetType.SUBAREA.equals(assetDto.portalAssetType())) {
      modelAndView.addObject("phaseSelectionHint", "If decommissioning is required, another phase must be selected.");
    }

    return modelAndView;
  }

  private AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName());
  }

  private Map<String, String> getPreselectedOperator(AppointmentCorrectionForm form) {
    var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());
    return operator.stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> String.valueOf(portalOrganisationDto.id()),
            OrganisationUnitDisplayUtil::getOrganisationUnitDisplayName
        ));
  }

  private Appointment getAppointment(AppointmentId appointmentId) {
    return appointmentAccessService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));
  }


}
