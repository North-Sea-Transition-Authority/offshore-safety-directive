package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = ExcludedWellboreController.class)
class ExcludedWellboreControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(100);

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private NominationDetail nominationDetail;

  @MockBean
  private LicenceBlockSubareaWellboreService subareaWellboreService;

  @MockBean
  NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @MockBean
  private ExcludedWellValidator excludedWellValidator;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(ExcludedWellboreController.class).renderPossibleWellsToExclude(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(NOMINATION_ID, null, ReverseRouter.emptyBindingResult())),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(ExcludedWellboreController.class).renderPossibleWellsToExclude(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(NOMINATION_ID, null, ReverseRouter.emptyBindingResult())),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void renderPossibleWellsToExclude_verifyModelAndViewProperties() throws Exception {

    given(subareaWellboreService.getSubareaRelatedWellbores(anyList()))
        .willReturn(Collections.emptyList());

    mockMvc.perform(
            get(ReverseRouter.route(on(ExcludedWellboreController.class).renderPossibleWellsToExclude(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/exclusions/wellsToExclude"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(NOMINATION_ID, null, ReverseRouter.emptyBindingResult()))
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
                ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))
        ))
        .andExpect(model().attribute("wellbores", Collections.emptyList()));
  }

  @Test
  void saveWellsToExclude_whenValidForm_verifyRedirection() throws Exception {

    doAnswer(invocation -> ReverseRouter.emptyBindingResult())
        .when(excludedWellValidator).validate(any(), any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        );
  }

  @Test
  void saveWellsToExclude_whenInvalidForm_verifyNoRedirection() throws Exception {

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue("hasWellsToExclude", "code", "message");
      return bindingResult;
    }).when(excludedWellValidator).validate(any(), any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/exclusions/wellsToExclude"));
  }

}