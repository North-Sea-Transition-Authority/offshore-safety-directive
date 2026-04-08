package uk.co.nstauthority.offshoresafetydirective.energyportal.usercontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class OffshoreSafetyDirectiveEnergyPortalUserServicesContextProviderTest {

  private static final long USER_WUA_ID = 1L;
  private static final String SCOPE_ID_ONE = "100";
  private static final String SCOPE_ID_TWO = "101";
  private static final PortalOrganisationDto ORG_UNIT_ONE = PortalOrganisationDtoTestUtil.builder()
      .withId(Integer.parseInt(SCOPE_ID_ONE))
      .build();
  private static final PortalOrganisationDto ORG_UNIT_TWO = PortalOrganisationDtoTestUtil.builder()
      .withId(Integer.parseInt(SCOPE_ID_TWO))
      .build();

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Mock
  private CaseEventQueryService caseEventQueryService;

  @InjectMocks
  private OffshoreSafetyDirectiveEnergyPortalUserServicesContextProvider contextProvider;

  @BeforeEach
  void setUp() {
    when(teamQueryService.getScopeIdsWhereUserHasAtLeastOneScopedRole(
        USER_WUA_ID,
        TeamType.ORGANISATION_GROUP,
        Set.of(Role.NOMINATION_SUBMITTER)
    )).thenReturn(
        Set.of(SCOPE_ID_ONE, SCOPE_ID_TWO)
    );

    when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        anyList(),
        any(RequestPurpose.class))
    ).thenReturn(List.of(ORG_UNIT_ONE, ORG_UNIT_TWO));
  }

  @Test
  void getUserContext_addUpdatesRequested_noneRequested() {
    when(caseEventQueryService.findAllCaseEventsByApplicantIn(List.of(ORG_UNIT_ONE.id(), ORG_UNIT_TWO.id())))
        .thenReturn(List.of());

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addUpdatesRequested() {
    var nomination1 = NominationTestUtil.builder().build();
    var nomination2 = NominationTestUtil.builder().build();

    var submittedEvent1 = CaseEventTestUtil.builder()
        .withNomination(nomination1)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.NOMINATION_SUBMITTED)
        .build();
    var updateRequestedEvent1 = CaseEventTestUtil.builder()
        .withNomination(nomination1)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    var generalNoteEvent1 = CaseEventTestUtil.builder()
        .withNomination(nomination1)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.GENERAL_NOTE)
        .build();

    var submittedEvent2 = CaseEventTestUtil.builder()
        .withNomination(nomination2)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.NOMINATION_SUBMITTED)
        .build();
    var updateRequestedEvent2 = CaseEventTestUtil.builder()
        .withNomination(nomination2)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();

    when(caseEventQueryService.findAllCaseEventsByApplicantIn(List.of(ORG_UNIT_ONE.id(), ORG_UNIT_TWO.id())))
        .thenReturn(List.of(submittedEvent1, updateRequestedEvent1, generalNoteEvent1, submittedEvent2, updateRequestedEvent2));

    var expectedUserContext = VersionedUserContext.newBuilder().v1()
        .low(2, "updates requested")
        .build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addUpdatesRequested_whenNominationWithdrawn_thenNoUpdatesRequested() {
    var nomination = NominationTestUtil.builder().build();

    var submittedEvent = CaseEventTestUtil.builder()
        .withNomination(nomination)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.NOMINATION_SUBMITTED)
        .build();
    var updateRequestedEvent = CaseEventTestUtil.builder()
        .withNomination(nomination)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .build();
    var withdrawnEvent = CaseEventTestUtil.builder()
        .withNomination(nomination)
        .withNominationVersion(1)
        .withCaseEventType(CaseEventType.WITHDRAWN)
        .build();

    when(caseEventQueryService.findAllCaseEventsByApplicantIn(List.of(ORG_UNIT_ONE.id(), ORG_UNIT_TWO.id())))
        .thenReturn(List.of(submittedEvent, updateRequestedEvent, withdrawnEvent));

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }
}