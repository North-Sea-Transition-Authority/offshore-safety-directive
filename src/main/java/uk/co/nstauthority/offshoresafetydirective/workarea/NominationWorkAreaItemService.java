package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PearsReferences;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.stringutil.StringUtil;

@Service
class NominationWorkAreaItemService {

  private static final String DEFAULT_TEXT = "Not provided";
  public static final int PEARS_REFERENCE_MAX_LENGTH = 40;

  private final NominationWorkAreaItemTransformerService nominationWorkAreaItemTransformerService;

  @Autowired
  NominationWorkAreaItemService(
      NominationWorkAreaItemTransformerService nominationWorkAreaItemTransformerService) {
    this.nominationWorkAreaItemTransformerService = nominationWorkAreaItemTransformerService;
  }

  public List<WorkAreaItem> getNominationWorkAreaItems() {
    return nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos()
        .stream()
        .sorted(
            Comparator.comparing((NominationWorkAreaItemDto dto) -> dto.nominationStatus().getDisplayOrder())
                .thenComparing(sortByItemDate(), Comparator.reverseOrder())
        )
        .map(this::generateWorkAreaItem)
        .toList();
  }

  private Function<NominationWorkAreaItemDto, Instant> sortByItemDate() {
    return dto -> {
      if (dto.nominationStatus() == NominationStatus.DELETED) {
        throw getDeletedNominationInWorkAreaException(dto);
      }
      return switch (dto.nominationStatus().getSubmissionStage()) {
        case PRE_SUBMISSION -> dto.createdTime().instant();
        case POST_SUBMISSION -> dto.submittedTime().instant();
      };
    };
  }

  private WorkAreaItem generateWorkAreaItem(NominationWorkAreaItemDto dto) {

    var modelProperties = new WorkAreaItemModelProperties()
        .addProperty("status", dto.nominationStatus().getScreenDisplayText())
        .addProperty(
            "applicantReference",
            Optional.ofNullable(dto.applicantReference())
                .map(ApplicantReference::reference)
                .orElse(DEFAULT_TEXT))
        .addProperty("nominationType", dto.nominationDisplay().getDisplayText())
        .addProperty(
            "applicantOrganisation",
            Optional.ofNullable(dto.applicantOrganisationUnitDto())
                .map(PortalOrganisationDto::name)
                .orElse(DEFAULT_TEXT))
        .addProperty(
            "nominationOrganisation",
            Optional.ofNullable(dto.nominatedOrganisationUnitDto())
                .map(PortalOrganisationDto::name)
                .orElse(DEFAULT_TEXT)
        )
        .addProperty("hasUpdateRequest", dto.nominationHasUpdateRequest().value());

    if (canAddPearsReferencesToModelProperties(dto)) {
      addPearsReferencesToModelProperties(modelProperties, dto);
    }

    return new WorkAreaItem(
        WorkAreaItemType.NOMINATION,
        getWorkAreaItemHeading(dto),
        getWorkAreaItemCaption(dto),
        getActionUrl(dto),
        modelProperties
    );
  }

  private String getActionUrl(NominationWorkAreaItemDto dto) {
    return switch (dto.nominationStatus()) {
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
      default -> switch (dto.nominationStatus().getSubmissionStage()) {
        case PRE_SUBMISSION -> ReverseRouter.route(
            on(NominationTaskListController.class).getTaskList(dto.nominationId()));
        case POST_SUBMISSION -> ReverseRouter.route(
            on(NominationCaseProcessingController.class).renderCaseProcessing(dto.nominationId(), null));
      };
    };
  }

  private String getWorkAreaItemHeading(NominationWorkAreaItemDto dto) {
    return switch (dto.nominationStatus()) {
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
      default -> switch (dto.nominationStatus().getSubmissionStage()) {
        case PRE_SUBMISSION -> generateDraftWorkAreaItemHeading(dto);
        case POST_SUBMISSION -> dto.nominationReference().reference();
      };
    };
  }

  private String generateDraftWorkAreaItemHeading(NominationWorkAreaItemDto dto) {
    if (dto.nominationVersion().version().equals(1)) {
      return "Draft nomination";
    }
    return dto.nominationReference().reference();
  }

  private String getWorkAreaItemCaption(NominationWorkAreaItemDto dto) {
    return switch (dto.nominationStatus()) {
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
      default -> switch (dto.nominationStatus().getSubmissionStage()) {
        case POST_SUBMISSION -> null;
        case PRE_SUBMISSION -> "Created on %s".formatted(DateUtil.formatLongDateTime(dto.createdTime().instant()));
      };
    };
  }

  private boolean canAddPearsReferencesToModelProperties(NominationWorkAreaItemDto dto) {
    return switch (dto.nominationStatus()) {
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
      default -> dto.nominationStatus().getSubmissionStage().equals(NominationStatusSubmissionStage.POST_SUBMISSION);
    };
  }

  private void addPearsReferencesToModelProperties(WorkAreaItemModelProperties modelProperties,
                                                   NominationWorkAreaItemDto dto) {

    var reference = Optional.ofNullable(dto.pearsReferences()).map(PearsReferences::references).orElse("");
    var abbreviatedReference = StringUtil.abbreviate(reference, PEARS_REFERENCE_MAX_LENGTH);

    modelProperties
        .addProperty("pearsReferences", abbreviatedReference);

    if (!reference.equals(abbreviatedReference)) {
      modelProperties
          .addProperty("pearsReferencesAbbreviated", true);
    }
  }

  private IllegalStateException getDeletedNominationInWorkAreaException(NominationWorkAreaItemDto dto) {
    return new IllegalStateException("Nomination with ID [%d] should not appear in work area as status is [%s]"
        .formatted(
            dto.nominationId().id(),
            dto.nominationStatus()
        ));
  }

}
