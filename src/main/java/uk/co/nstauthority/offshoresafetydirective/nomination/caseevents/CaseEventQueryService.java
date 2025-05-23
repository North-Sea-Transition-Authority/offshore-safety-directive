package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
public class CaseEventQueryService {

  static final RequestPurpose CASE_EVENT_CREATED_BY_USER_PURPOSE =
      new RequestPurpose("Get details for user who created the case event");
  private final CaseEventRepository caseEventRepository;
  private final CaseEventFileService caseEventFileService;
  private final EnergyPortalUserService energyPortalUserService;
  private final UserDetailService userDetailService;
  private final TeamQueryService teamQueryService;

  @Autowired
  CaseEventQueryService(CaseEventRepository caseEventRepository,
                        CaseEventFileService caseEventFileService,
                        EnergyPortalUserService energyPortalUserService,
                        UserDetailService userDetailService, TeamQueryService teamQueryService) {
    this.caseEventRepository = caseEventRepository;
    this.caseEventFileService = caseEventFileService;
    this.energyPortalUserService = energyPortalUserService;
    this.userDetailService = userDetailService;
    this.teamQueryService = teamQueryService;
  }

  public Optional<LocalDate> getDecisionDateForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
            nominationDetail.getNomination(),
            dto.version())
        .map(caseEvent -> LocalDate.ofInstant(caseEvent.getEventInstant(), ZoneId.systemDefault()));
  }

  public Optional<LocalDate> getAppointmentConfirmationDateForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
            nominationDetail.getNomination(),
            dto.version())
        .map(caseEvent -> LocalDate.ofInstant(caseEvent.getEventInstant(), ZoneId.systemDefault()));
  }

  public Optional<String> getLatestReasonForUpdate(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.UPDATE_REQUESTED),
            nominationDetail.getNomination(),
            dto.version()
        )
        .map(CaseEvent::getComment);
  }

  public boolean hasUpdateRequest(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return hasUpdateRequest(nominationDetailDto);
  }

  public boolean hasUpdateRequest(NominationDetailDto nominationDetailDto) {

    Optional<CaseEvent> updateRequestEvent = caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetailDto.nominationId().id(),
        nominationDetailDto.version()
    );

    return updateRequestEvent.isPresent() && nominationDetailDto.nominationStatus() != NominationStatus.WITHDRAWN;
  }

  public List<CaseEventView> getCaseEventViews(Nomination nomination) {

    var events = caseEventRepository.findAllByNomination(nomination);

    var createdByUserIds = events.stream()
        .map(caseEvent -> new WebUserAccountId(caseEvent.getCreatedBy()))
        .distinct()
        .toList();

    var users = energyPortalUserService.findByWuaIds(createdByUserIds, CASE_EVENT_CREATED_BY_USER_PURPOSE);

    var userIdAndDtoMap = users.stream()
        .collect(Collectors.toMap(
            EnergyPortalUserDto::webUserAccountId,
            user -> user
        ));

    var files = caseEventFileService.getFileViewMapFromCaseEvents(events);

    var user = userDetailService.getUserDetail();

    var teamRolesForUser = teamQueryService.getTeamRolesForUser(user.wuaId());

    var canViewAllCaseEvents = teamRolesForUser
        .stream()
        .anyMatch(this::hasRegulatorCaseEventRole);

    if (canViewAllCaseEvents) {
      return events.stream()
          .map(caseEvent -> buildCaseEventView(caseEvent, userIdAndDtoMap, files.get(caseEvent)))
          .sorted(Comparator.comparing(CaseEventView::getCreatedInstant, Comparator.reverseOrder()))
          .toList();
    }

    Set<TeamType> teamTypesForUser = teamRolesForUser
        .stream()
        .map(teamRole -> teamRole.getTeam().getTeamType())
        .collect(Collectors.toSet());

    return events.stream()
        .filter(caseEvent -> CaseEventType.isValidForTeamType(teamTypesForUser, caseEvent.getCaseEventType()))
        .map(caseEvent -> buildCaseEventView(caseEvent, userIdAndDtoMap, files.get(caseEvent)))
        .sorted(Comparator.comparing(CaseEventView::getCreatedInstant, Comparator.reverseOrder()))
        .toList();
  }

  public Optional<CaseEvent> getCaseEventForNomination(CaseEventId caseEventId,
                                                       Nomination nomination) {
    return caseEventRepository.findByUuidAndNomination(
        caseEventId.uuid(),
        nomination
    );
  }

  CaseEventView buildCaseEventView(CaseEvent caseEvent, Map<Long, EnergyPortalUserDto> userIdAndNameMap,
                                   List<FileSummaryView> uploadedFileViews) {

    var caseEventBuilder = CaseEventView.builder(
        Optional.ofNullable(caseEvent.getTitle()).orElse(caseEvent.getCaseEventType().getScreenDisplayText()),
        caseEvent.getNominationVersion(),
        caseEvent.getCreatedInstant(),
        caseEvent.getEventInstant(),
        userIdAndNameMap.get(caseEvent.getCreatedBy()).displayName(),
        caseEvent.getCaseEventType()
    );

    return switch (caseEvent.getCaseEventType()) {
      case QA_CHECKS -> caseEventBuilder
          .withCustomDatePrompt("Completion date")
          .withCustomCreatorPrompt("Completed by")
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("QA comments")
          .build();
      case NO_OBJECTION_DECISION, OBJECTION_DECISION -> caseEventBuilder
          .withCustomDatePrompt("Decision date")
          .withEventInstant(caseEvent.getEventInstant(), DateUtil.formatLongDate(caseEvent.getEventInstant()))
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("Decision comment")
          .withFileViews(uploadedFileViews)
          .withCustomFilePrompt("Decision document")
          .build();
      case WITHDRAWN -> caseEventBuilder
          .withCustomDatePrompt("Withdrawal date")
          .withCustomCreatorPrompt("Withdrawn by")
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("Withdrawal reason")
          .build();
      case CONFIRM_APPOINTMENT -> caseEventBuilder
          .withCustomDatePrompt("Appointment date")
          .withEventInstant(caseEvent.getEventInstant(), DateUtil.formatLongDate(caseEvent.getEventInstant()))
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("Appointment comments")
          .withFileViews(uploadedFileViews)
          .withCustomFilePrompt("Appointment documents")
          .build();
      case GENERAL_NOTE -> caseEventBuilder
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("Case note text")
          .withFileViews(uploadedFileViews)
          .withCustomFilePrompt("Case note documents")
          .build();
      case NOMINATION_SUBMITTED -> caseEventBuilder
          .withCustomDatePrompt("Submitted on")
          .withCustomCreatorPrompt("Submitted by")
          .build();
      case SENT_FOR_CONSULTATION -> caseEventBuilder
          .withCustomDatePrompt("Date requested")
          .build();
      case CONSULTATION_RESPONSE -> caseEventBuilder
          .withCustomDatePrompt("Response date")
          .withBody(caseEvent.getComment())
          .withCustomBodyPrompt("Consultation response")
          .withFileViews(uploadedFileViews)
          .withCustomFilePrompt("Consultation response documents")
          .build();
      case UPDATE_REQUESTED -> caseEventBuilder
          .withCustomBodyPrompt("Reason for update")
          .withBody(caseEvent.getComment())
          .withCustomDatePrompt("Date requested")
          .withCustomCreatorPrompt("Requested by")
          .build();
    };
  }

  private boolean hasRegulatorCaseEventRole(TeamRole teamRole) {
    return TeamType.REGULATOR.equals(teamRole.getTeam().getTeamType())
        && Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION).contains(teamRole.getRole());
  }
}
