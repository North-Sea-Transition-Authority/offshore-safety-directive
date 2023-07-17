package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = {NominationReferenceRestController.class})
class NominationReferenceRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail APPOINTMENT_MANAGER = ServiceUserDetailTestUtil.Builder().build();
  private static final TeamMember APPOINTMENT_MANAGER_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(APPOINTMENT_MANAGER))
        .thenReturn(Collections.singletonList(APPOINTMENT_MANAGER_MEMBER));
  }

  @SecurityTest
  void searchPostSubmissionNominations_whenUnauthenticated_thenRedirectedToLogin() throws Exception {
    var searchTerm = "search";
    mockMvc.perform(get(
            ReverseRouter.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(searchTerm)))
        )
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void searchPostSubmissionNominations_whenAuthenticated_thenOk() {

    var searchTerm = "search";

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.MANAGE_APPOINTMENTS))
        .withUser(APPOINTMENT_MANAGER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(searchTerm))
        )
        .test();
  }

  @Test
  void searchPostSubmissionNominations() throws Exception {
    var searchTerm = "search";

    var firstNominationId = new NominationId(123);
    var firstNominationReference = "NOM/REF/1";
    var firstNominationDto = NominationDtoTestUtil.builder()
        .withNominationId(firstNominationId)
        .withNominationReference(firstNominationReference)
        .build();

    var secondNominationId = new NominationId(124);
    var secondNominationReference = "NOM/REF/2";
    var secondNominationDto = NominationDtoTestUtil.builder()
        .withNominationId(secondNominationId)
        .withNominationReference(secondNominationReference)
        .build();

    when(nominationDetailService.getNominationsByReferenceLikeWithStatuses(
        searchTerm,
        EnumSet.of(NominationStatus.APPOINTED)
    )).thenReturn(Set.of(secondNominationDto, firstNominationDto));

    var result = mockMvc.perform(get(
            ReverseRouter.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var mappedResult = OBJECT_MAPPER.readValue(result, RestSearchResult.class);

    assertThat(mappedResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(firstNominationId.toString(), firstNominationReference),
            tuple(secondNominationId.toString(), secondNominationReference)
        );
  }
}