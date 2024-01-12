package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.ForwardApprovedAppointmentRestController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.NominationReferenceRestController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;

@Service
public class AppointmentModelAndViewService {

  public static final RequestPurpose PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE =
      new RequestPurpose("Subarea name for the preselected forward approved appointment display string");

  public static final RequestPurpose PRE_SELECTED_OPERATOR_NAME_PURPOSE =
      new RequestPurpose("Get pre-selected operator name for appointment");

  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final AppointmentCorrectionService appointmentCorrectionService;
  private final NominationDetailService nominationDetailService;
  private final AppointmentAccessService appointmentAccessService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Autowired
  public AppointmentModelAndViewService(PortalAssetRetrievalService portalAssetRetrievalService,
                                        AppointmentCorrectionService appointmentCorrectionService,
                                        NominationDetailService nominationDetailService,
                                        AppointmentAccessService appointmentAccessService,
                                        LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                        PortalOrganisationUnitQueryService organisationUnitQueryService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.nominationDetailService = nominationDetailService;
    this.appointmentAccessService = appointmentAccessService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.organisationUnitQueryService = organisationUnitQueryService;
  }

  public ModelAndView getAppointmentModelAndView(String pageTitle, AssetDto assetDto, AppointmentCorrectionForm form,
                                                 String submitUrl) {
    var assetName = portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName().value());

    var modelAndView = new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("pageTitle", pageTitle)
        .addObject("assetName", assetName)
        .addObject("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName())
        .addObject("assetTypeSentenceCaseDisplayName", assetDto.portalAssetType().getSentenceCaseDisplayName())
        .addObject("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name())))
        .addObject("appointmentTypes", AppointmentType.getDisplayableOptions(assetDto.portalAssetType()))
        .addObject("phases", appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType()))
        .addObject("form", form)
        .addObject(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        )
        .addObject(
            "forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null))
        )
        .addObject("cancelUrl", getTimelineRoute(assetDto))
        .addObject("submitUrl", submitUrl)
        .addObject("preSelectedForwardApprovedAppointment", getPreselectedForwardApprovedAppointment(form))
        .addObject("preselectedNominationReference", getPreselectedNominationReference(form))
        .addObject("preselectedOperator", getPreselectedOperator(form));

    if (PortalAssetType.SUBAREA.equals(assetDto.portalAssetType())) {
      modelAndView.addObject("phaseSelectionHint", "If decommissioning is required, another phase must be selected.");
    }

    return modelAndView;
  }

  private Map<UUID, String> getPreselectedNominationReference(AppointmentCorrectionForm form) {
    if (AppointmentType.ONLINE_NOMINATION.name().equals(form.getAppointmentType())
        && form.getOnlineNominationReference() != null) {

      var optionalNominationDetail = nominationDetailService.getLatestNominationDetailOptional(
          new NominationId(UUID.fromString(form.getOnlineNominationReference())));

      if (optionalNominationDetail.isPresent()) {
        var nomination = optionalNominationDetail.get().getNomination();
        return Map.of(
            nomination.getId(),
            nomination.getReference()
        );
      }
    }
    return Map.of();
  }

  private Map<UUID, String> getPreselectedForwardApprovedAppointment(AppointmentCorrectionForm form) {
    if (AppointmentType.FORWARD_APPROVED.name().equals(form.getAppointmentType())
        && form.getForwardApprovedAppointmentId() != null) {
      var optionalAppointment = appointmentAccessService.getAppointment(
          new AppointmentId(UUID.fromString(form.getForwardApprovedAppointmentId())));

      if (optionalAppointment.isPresent()) {
        var preSelectedAppointment = optionalAppointment.get();
        var startDate = DateUtil.formatLongDate(preSelectedAppointment.getResponsibleFromDate());

        var subareaName = licenceBlockSubareaQueryService.getLicenceBlockSubarea(
                new LicenceBlockSubareaId(preSelectedAppointment.getAsset().getPortalAssetId()),
                PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
            ).map(LicenceBlockSubareaDto::displayName)
            .orElse(preSelectedAppointment.getAsset().getAssetName());

        return Map.of(
            preSelectedAppointment.getId(),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING.formatted(subareaName, startDate)
        );
      }
    }

    return Map.of();
  }

  private Map<String, String> getPreselectedOperator(AppointmentCorrectionForm form) {

    if (form.getAppointedOperatorId() != null && NumberUtils.isDigits(form.getAppointedOperatorId())) {
      var operator = organisationUnitQueryService.getOrganisationById(
          Integer.parseInt(form.getAppointedOperatorId()),
          PRE_SELECTED_OPERATOR_NAME_PURPOSE
      );

      return operator.stream()
          .collect(Collectors.toMap(
              portalOrganisationDto -> Objects.toString(portalOrganisationDto.id(), null),
              OrganisationUnitDisplayUtil::getOrganisationUnitDisplayName
          ));
    }

    return Map.of();
  }

  private String getTimelineRoute(AssetDto assetDto) {
    return switch (assetDto.portalAssetType()) {
      case WELLBORE -> ReverseRouter.route(on(AssetTimelineController.class).renderWellboreTimeline(assetDto.portalAssetId()));
      case INSTALLATION -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderInstallationTimeline(assetDto.portalAssetId()));
      case SUBAREA -> ReverseRouter.route(on(AssetTimelineController.class).renderSubareaTimeline(assetDto.portalAssetId()));
    };
  }
}
