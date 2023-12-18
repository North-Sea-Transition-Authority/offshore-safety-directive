package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class CaseEventQueryServiceTest {

  @Mock
  private CaseEventRepository caseEventRepository;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private CaseEventFileService caseEventFileService;

  @Mock
  private TeamMemberService teamMemberService;

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
  @MethodSource("getCaseEventTypes")
  void getCaseViews_whenNominationManager_thenShowAllCaseEvents(CaseEventType hiddenFromOperators, CaseEventType visibleToOperators) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);
    when(permissionService.hasPermission(user, RolePermission.VIEW_ALL_NOMINATIONS)).thenReturn(true);

    var nomination = NominationTestUtil.builder().build();

    var onlyManagePermissionCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(hiddenFromOperators)
        .withCreatedBy(user.wuaId())
        .withCreatedInstant(Instant.now())
        .build();

    var anyPermissionCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(visibleToOperators)
        .withCreatedBy(user.wuaId())
        .withCreatedInstant(Instant.now().minus(Period.ofDays(1)))
        .build();

    when(caseEventRepository.findAllByNomination(nomination))
        .thenReturn(List.of(onlyManagePermissionCaseEvent, anyPermissionCaseEvent));

    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user.wuaId())
        .build();

    when(energyPortalUserService.findByWuaIds(
        List.of(new WebUserAccountId(user.wuaId())),
        CaseEventQueryService.CASE_EVENT_CREATED_BY_USER_PURPOSE)
    ).thenReturn(Collections.singletonList(epaUser));

    var resultingCaseEventViews = caseEventQueryService.getCaseEventViews(nomination);

    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getTitle)
        .containsExactly(
            hiddenFromOperators.getScreenDisplayText(),
            visibleToOperators.getScreenDisplayText()
        );
  }

  @ParameterizedTest
  @MethodSource("getCaseEventTypes")
  void getCaseViews_whenNotNominationManager_thenShowCaseEventsVisibleToUser(CaseEventType hiddenFromOperators, CaseEventType visibleToOperators) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);
    when(permissionService.hasPermission(user, RolePermission.VIEW_ALL_NOMINATIONS)).thenReturn(false);
    var teamMember = TeamMemberTestUtil.Builder().withTeamType(TeamType.INDUSTRY).build();
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));

    var nomination = NominationTestUtil.builder().build();

    var onlyManagePermissionCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(hiddenFromOperators)
        .withCreatedBy(user.wuaId())
        .withCreatedInstant(Instant.now())
        .build();

    var anyPermissionCaseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(visibleToOperators)
        .withCreatedBy(user.wuaId())
        .withCreatedInstant(Instant.now().minus(Period.ofDays(1)))
        .build();

    when(caseEventRepository.findAllByNomination(nomination))
        .thenReturn(List.of(onlyManagePermissionCaseEvent, anyPermissionCaseEvent));

    var epaUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user.wuaId())
        .build();

    when(energyPortalUserService.findByWuaIds(
        List.of(new WebUserAccountId(user.wuaId())),
        CaseEventQueryService.CASE_EVENT_CREATED_BY_USER_PURPOSE)
    ).thenReturn(Collections.singletonList(epaUser));

    var resultingCaseEventViews = caseEventQueryService.getCaseEventViews(nomination);

    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getTitle)
        .containsExactly(visibleToOperators.getScreenDisplayText());

    assertThat(resultingCaseEventViews)
        .extracting(CaseEventView::getTitle)
        .doesNotContain(hiddenFromOperators.getScreenDisplayText());
  }

  private static Stream<Arguments> getCaseEventTypes() {
    return Stream.of(
        Arguments.of(CaseEventType.QA_CHECKS, CaseEventType.NO_OBJECTION_DECISION),
        Arguments.of(CaseEventType.GENERAL_NOTE, CaseEventType.WITHDRAWN),
        Arguments.of(CaseEventType.SENT_FOR_CONSULTATION, CaseEventType.CONFIRM_APPOINTMENT),
        Arguments.of(CaseEventType.CONSULTATION_RESPONSE, CaseEventType.UPDATE_REQUESTED)
    );
  }
}