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
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Service
class NominationWorkAreaItemService {

  private static final String DEFAULT_TEXT = "Not provided";

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
    return dto -> switch (dto.nominationStatus()) {
      case DRAFT -> dto.createdTime().instant();
      case SUBMITTED -> dto.submittedTime().instant();
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
    };
  }

  private WorkAreaItem generateWorkAreaItem(NominationWorkAreaItemDto dto) {
    return new WorkAreaItem(
        WorkAreaItemType.NOMINATION,
        getWorkAreaItemHeading(dto),
        getWorkAreaItemCaption(dto),
        ReverseRouter.route(on(NominationTaskListController.class).getTaskList(dto.nominationId())),
        new WorkAreaItemModelProperties()
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
    );
  }

  private String getWorkAreaItemHeading(NominationWorkAreaItemDto dto) {
    return switch (dto.nominationStatus()) {
      case DRAFT -> generateDraftWorkAreaItemHeading(dto);
      case SUBMITTED -> dto.nominationReference().reference();
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
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
      case DRAFT -> "Created on %s".formatted(DateUtil.formatDateTime(dto.createdTime().instant()));
      case SUBMITTED -> null;
      case DELETED -> throw getDeletedNominationInWorkAreaException(dto);
    };
  }

  private IllegalStateException getDeletedNominationInWorkAreaException(NominationWorkAreaItemDto dto) {
    return new IllegalStateException("Nomination with ID [%d] should not appear in work area as status is [%s]"
        .formatted(
            dto.nominationId().id(),
            dto.nominationStatus()
        ));
  }

}
