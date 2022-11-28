package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Service
class NominationWorkAreaItemService {

  public static final String DATE_FORMAT = "dd MMM yyyy HH:mm";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

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
      case DRAFT -> "Created on %s".formatted(
          LocalDateTime.ofInstant(dto.createdTime().instant(), ZoneId.systemDefault())
              .format(DATE_TIME_FORMATTER)
      );
      case SUBMITTED -> null;
    };
  }

}
