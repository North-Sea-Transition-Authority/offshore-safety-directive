package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class CaseEventQueryServiceTest {

  @Mock
  private CaseEventRepository caseEventRepository;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private CaseEventFileService caseEventFileService;

  @InjectMocks
  private CaseEventQueryService caseEventQueryService;

  @Test
  void getDecisionDateForNominationDetail_whenCaseEventFound_thenAssertDate() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var createdInstant = Instant.now();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .withNominationVersion(nominationVersion)
        .withCreatedInstant(createdInstant)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getDecisionDateForNominationDetail(detail);

    assertThat(result).contains(LocalDate.ofInstant(createdInstant, ZoneId.systemDefault()));
  }

  @Test
  void getDecisionDateForNominationDetail_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getDecisionDateForNominationDetail(detail);

    assertThat(result).isEmpty();
  }

  @Test
  void getAppointmentConfirmationDateForNominationDetail_whenCaseEventFound_thenAssertDate() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.APPOINTED)
        .build();

    var confirmationInstant = Instant.now();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.CONFIRM_APPOINTMENT)
        .withNominationVersion(nominationVersion)
        .withEventInstant(confirmationInstant)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(detail);

    assertThat(result).contains(LocalDate.ofInstant(confirmationInstant, ZoneId.systemDefault()));
  }

  @Test
  void getAppointmentConfirmationDateForNominationDetail_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.APPOINTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(detail);

    assertThat(result).isEmpty();
  }

  @Test
  void getCaseEventForNomination_whenFound_thenAssert() {
    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();
    var caseEvent = CaseEventTestUtil.builder().build();

    when(caseEventRepository.findByUuidAndNomination(
        caseEvent.getUuid(),
        nominationDetail.getNomination()
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getCaseEventForNomination(
        new CaseEventId(caseEvent.getUuid()),
        nominationDetail.getNomination()
    );
    assertThat(result).contains(caseEvent);
  }

  @Test
  void getCaseEventForNomination_whenNotFound_thenAssertEmpty() {
    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();
    var caseEvent = CaseEventTestUtil.builder().build();

    when(caseEventRepository.findByUuidAndNomination(
        caseEvent.getUuid(),
        nominationDetail.getNomination()
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getCaseEventForNomination(
        new CaseEventId(caseEvent.getUuid()),
        nominationDetail.getNomination()
    );
    assertThat(result).isEmpty();
  }

  @Test
  void getLatestReasonForUpdate_whenCaseEventFound_thenAssertUpdateReason() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var reason = "reason";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .withNominationVersion(nominationVersion)
        .withComment(reason)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getLatestReasonForUpdate(detail);

    assertThat(result).contains(reason);
  }

  @Test
  void getLatestReasonForUpdate_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getLatestReasonForUpdate(detail);

    assertThat(result).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "WITHDRAWN")
  void hasUpdateRequest_whenUpdateRequestAndNotWithdrawnStatus_thenTrue(NominationStatus nominationStatus) {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(nominationStatus)
        .build();

    var expectedCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetail.getNomination().getId(),
        nominationVersion
    ))
        .thenReturn(Optional.of(expectedCaseEvent));

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetail);

    assertTrue(hasUpdateRequest);
  }

  @Test
  void hasUpdateRequest_whenUpdateRequestAndWithdrawnStatus_thenFalse() {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.WITHDRAWN)
        .withVersion(nominationVersion)
        .build();

    var expectedCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetail.getNomination().getId(),
        nominationVersion
    ))
        .thenReturn(Optional.of(expectedCaseEvent));

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetail);

    assertFalse(hasUpdateRequest);
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "WITHDRAWN")
  void hasUpdateRequest_whenNoUpdateRequestAndNotWithdrawnStatus_thenFalse(NominationStatus nominationStatus) {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(nominationStatus)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetail.getNomination().getId(),
        nominationVersion
    ))
        .thenReturn(Optional.empty());

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetail);

    assertFalse(hasUpdateRequest);
  }

  @Test
  void hasUpdateRequest_whenNoUpdateRequestAndWithdrawnStatus_thenFalse() {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.WITHDRAWN)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetail.getNomination().getId(),
        nominationVersion
    ))
        .thenReturn(Optional.empty());

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetail);

    assertFalse(hasUpdateRequest);
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "WITHDRAWN")
  void hasUpdateRequest_DtoVariant_whenUpdateRequestAndNotWithdrawnStatus_thenTrue(NominationStatus nominationStatus) {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(nominationStatus)
        .build();

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    var expectedCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetailDto.nominationId().id(),
        nominationVersion
    ))
        .thenReturn(Optional.of(expectedCaseEvent));

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetailDto);

    assertTrue(hasUpdateRequest);
  }

  @Test
  void hasUpdateRequest_DtoVariant_whenUpdateRequestAndWithdrawnStatus_thenFalse() {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.WITHDRAWN)
        .withVersion(nominationVersion)
        .build();

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    var expectedCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetailDto.nominationId().id(),
        nominationVersion
    ))
        .thenReturn(Optional.of(expectedCaseEvent));

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetailDto);

    assertFalse(hasUpdateRequest);
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "WITHDRAWN")
  void hasUpdateRequest_DtoVariant_whenNoUpdateRequestAndNotWithdrawnStatus_thenFalse(NominationStatus nominationStatus) {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(nominationStatus)
        .build();

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetailDto.nominationId().id(),
        nominationVersion
    ))
        .thenReturn(Optional.empty());

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetailDto);

    assertFalse(hasUpdateRequest);
  }

  @Test
  void hasUpdateRequest_DtoVariant_whenNoUpdateRequestAndWithdrawnStatus_thenFalse() {

    var nominationVersion = 5;

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.WITHDRAWN)
        .build();

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    when(caseEventRepository.findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        nominationDetailDto.nominationId().id(),
        nominationVersion
    ))
        .thenReturn(Optional.empty());

    var hasUpdateRequest = caseEventQueryService.hasUpdateRequest(nominationDetailDto);

    assertFalse(hasUpdateRequest);
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, mode = EnumSource.Mode.INCLUDE, names = {"NOMINATION_MANAGER", "VIEW_ANY_NOMINATION"})
  void getCaseViews_whenRegulator_thenShowCaseEventsVisibleToUser(Role role) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var regulatorTeamRole = new TeamRole();
    regulatorTeamRole.setTeam(regulatorTeam);
    regulatorTeamRole.setRole(role);

    when(teamQueryService.getTeamRolesForUser(user.wuaId()))
        .thenReturn(Set.of(regulatorTeamRole));

    var nomination = NominationTestUtil.builder().build();

    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user.wuaId())
        .build();

    when(energyPortalUserService.findByWuaIds(
        List.of(new WebUserAccountId(user.wuaId())),
        CaseEventQueryService.CASE_EVENT_CREATED_BY_USER_PURPOSE)
    ).thenReturn(Collections.singletonList(epaUser));

    List<CaseEvent> allCaseEvents = new ArrayList<>();

    // GIVEN a nomination which has all the case events associated
    Arrays.stream(CaseEventType.values()).forEach(caseEventType -> {

      var caseEvent = CaseEventTestUtil.builder()
          .withCaseEventType(caseEventType)
          .withCreatedBy(user.wuaId())
          .build();

      allCaseEvents.add(caseEvent);
    });

    when(caseEventRepository.findAllByNomination(nomination))
        .thenReturn(allCaseEvents);

    // WHEN I get the case events I can see
    var resultingCaseEventViews = caseEventQueryService.getCaseEventViews(nomination);

    // THEN as a regulator team member
    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getCaseEventType)
        .containsExactlyInAnyOrder(
            CaseEventType.NO_OBJECTION_DECISION,
            CaseEventType.OBJECTION_DECISION,
            CaseEventType.WITHDRAWN,
            CaseEventType.CONFIRM_APPOINTMENT,
            CaseEventType.NOMINATION_SUBMITTED,
            CaseEventType.UPDATE_REQUESTED,
            CaseEventType.CONSULTATION_RESPONSE,
            CaseEventType.SENT_FOR_CONSULTATION,
            CaseEventType.GENERAL_NOTE,
            CaseEventType.QA_CHECKS
        );
  }

  @Test
  void getCaseViews_whenIndustry_thenShowCaseEventsVisibleToUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var organisationGroupTeam = new Team();
    organisationGroupTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    var organisationTeamRole = new TeamRole();
    organisationTeamRole.setTeam(organisationGroupTeam);

    when(teamQueryService.getTeamRolesForUser(user.wuaId()))
        .thenReturn(Set.of(organisationTeamRole));

    var nomination = NominationTestUtil.builder().build();

    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user.wuaId())
        .build();

    when(energyPortalUserService.findByWuaIds(
        List.of(new WebUserAccountId(user.wuaId())),
        CaseEventQueryService.CASE_EVENT_CREATED_BY_USER_PURPOSE)
    ).thenReturn(Collections.singletonList(epaUser));

    List<CaseEvent> allCaseEvents = new ArrayList<>();

    // GIVEN a nomination which has all the case events associated
    Arrays.stream(CaseEventType.values()).forEach(caseEventType -> {

      var caseEvent = CaseEventTestUtil.builder()
          .withCaseEventType(caseEventType)
          .withCreatedBy(user.wuaId())
          .build();

      allCaseEvents.add(caseEvent);
    });

    when(caseEventRepository.findAllByNomination(nomination))
        .thenReturn(allCaseEvents);

    // WHEN I get the case events I can see
    var resultingCaseEventViews = caseEventQueryService.getCaseEventViews(nomination);

    // THEN as an industry team member
    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getCaseEventType)
        .containsExactlyInAnyOrder(
            CaseEventType.NO_OBJECTION_DECISION,
            CaseEventType.OBJECTION_DECISION,
            CaseEventType.WITHDRAWN,
            CaseEventType.CONFIRM_APPOINTMENT,
            CaseEventType.NOMINATION_SUBMITTED,
            CaseEventType.UPDATE_REQUESTED
        );
  }

  @Test
  void getCaseViews_whenConsultee_thenShowCaseEventsVisibleToUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var consulteeTeam = new Team();
    consulteeTeam.setTeamType(TeamType.CONSULTEE);

    var consulteeTeamRole = new TeamRole();
    consulteeTeamRole.setTeam(consulteeTeam);
    consulteeTeamRole.setRole(Role.CONSULTATION_MANAGER);

    when(teamQueryService.getTeamRolesForUser(user.wuaId()))
        .thenReturn(Set.of(consulteeTeamRole));

    var nomination = NominationTestUtil.builder().build();

    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user.wuaId())
        .build();

    when(energyPortalUserService.findByWuaIds(
        List.of(new WebUserAccountId(user.wuaId())),
        CaseEventQueryService.CASE_EVENT_CREATED_BY_USER_PURPOSE)
    ).thenReturn(Collections.singletonList(epaUser));

    List<CaseEvent> allCaseEvents = new ArrayList<>();

    // GIVEN a nomination which has all the case events associated
    Arrays.stream(CaseEventType.values()).forEach(caseEventType -> {

      var caseEvent = CaseEventTestUtil.builder()
          .withCaseEventType(caseEventType)
          .withCreatedBy(user.wuaId())
          .build();

      allCaseEvents.add(caseEvent);
    });

    when(caseEventRepository.findAllByNomination(nomination))
        .thenReturn(allCaseEvents);

    // WHEN I get the case events I can see
    var resultingCaseEventViews = caseEventQueryService.getCaseEventViews(nomination);

    // THEN as an industry team member
    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getCaseEventType)
        .containsExactlyInAnyOrder(
            CaseEventType.NO_OBJECTION_DECISION,
            CaseEventType.OBJECTION_DECISION,
            CaseEventType.WITHDRAWN,
            CaseEventType.CONFIRM_APPOINTMENT,
            CaseEventType.NOMINATION_SUBMITTED,
            CaseEventType.UPDATE_REQUESTED
        );
  }

  @Test
  void buildCaseEventView_qaChecks_assertView() {
    var caseEventType = CaseEventType.QA_CHECKS;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "qa body comment";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "QA comments")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Completed by")
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Completion date")
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @ParameterizedTest
  @EnumSource(value = CaseEventType.class, names = {"NO_OBJECTION_DECISION", "OBJECTION_DECISION"})
  void buildCaseEventView_objectionDecision_assertView(CaseEventType caseEventType) {
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "objection body comment";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Decision comment")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Decision date")
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("customFilePrompt", "Decision document")
        .hasFieldOrPropertyWithValue("fileViews", List.of(fileSummaryView))
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("formattedEventTime", DateUtil.formatLongDate(caseEvent.getEventInstant()))
        .hasAssertedAllProperties();
  }

  @Test
  void buildCaseEventView_withdrawn_assertView() {
    var caseEventType = CaseEventType.WITHDRAWN;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "withdrawn reason";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Withdrawal reason")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Withdrawn by")
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Withdrawal date")
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void buildCaseEventView_confirmAppointment_assertView() {
    var caseEventType = CaseEventType.CONFIRM_APPOINTMENT;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "confirmation";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Appointment comments")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Appointment date")
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", List.of(fileSummaryView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Appointment documents")
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("formattedEventTime", DateUtil.formatLongDate(caseEvent.getEventInstant()))
        .hasAssertedAllProperties();
  }

  @Test
  void buildCaseEventView_generalNote_assertView() {
    var caseEventType = CaseEventType.GENERAL_NOTE;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "case note text";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Case note text")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", List.of(fileSummaryView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Case note documents")
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("customDatePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void buildCaseEventView_nominationSubmitted_assertView() {
    var caseEventType = CaseEventType.NOMINATION_SUBMITTED;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "submitted body";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", null)
        .hasFieldOrPropertyWithValue("customBodyPrompt", null)
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Submitted by")
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customDatePrompt", "Submitted on")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void buildCaseEventView_sentForConsultation_assertView() {
    var caseEventType = CaseEventType.SENT_FOR_CONSULTATION;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "unassigned comment text";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", null)
        .hasFieldOrPropertyWithValue("customBodyPrompt", null)
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customDatePrompt", "Date requested")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void buildCaseEventView_consultationResponse_assertView() {
    var caseEventType = CaseEventType.CONSULTATION_RESPONSE;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "consultation response";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Consultation response")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", List.of(fileSummaryView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Consultation response documents")
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customDatePrompt", "Response date")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void buildCaseEventView_updateRequested_assertView() {
    var caseEventType = CaseEventType.UPDATE_REQUESTED;
    var creatorId = 123L;
    var nominationVersion = 2;
    var comment = "reason for update request";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withCreatedBy(creatorId)
        .withNominationVersion(nominationVersion)
        .withComment(comment)
        .build();

    var fileSummaryView = new FileSummaryView(
        UploadedFileView.from(UploadedFileTestUtil.builder().build()),
        "/"
    );

    var creatorDto = EnergyPortalUserDtoTestUtil.Builder().build();
    var creatorMap = Map.of(creatorId, creatorDto);

    var result = caseEventQueryService.buildCaseEventView(caseEvent, creatorMap, List.of(fileSummaryView));

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("nominationVersion", nominationVersion)
        .hasFieldOrPropertyWithValue("body", comment)
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Reason for update")
        .hasFieldOrPropertyWithValue("createdBy", creatorDto.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Requested by")
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("caseEventType", caseEventType)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("customDatePrompt", "Date requested")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

}