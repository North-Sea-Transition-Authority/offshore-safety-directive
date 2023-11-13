package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeAccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominatedWellDetailController.class)
@IncludeAccidentRegulatorConfigurationProperties
class NominatedWellDetailControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private final NominationId nominationId = new NominationId(UUID.randomUUID());

  private NominationDetail nominationDetail;

  @MockBean
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @MockBean
  private WellQueryService wellQueryService;

  @MockBean
  private NominatedWellDetailFormService nominatedWellDetailFormService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(nominationId)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));

  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = NominatedWellFormTestUtil.builder().build();
    when(nominatedWellDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedWellDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedWellDetailController.class)
                .saveNominatedWellDetail(nominationId, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var form = NominatedWellFormTestUtil.builder().build();
    when(nominatedWellDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedWellDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedWellDetailController.class)
                .saveNominatedWellDetail(nominationId, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void renderNominatedWellDetail_assertModelProperties() throws Exception {

    var form = new NominatedWellDetailForm();
    when(nominatedWellDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId)))
            .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/specificWells"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("pageTitle", NominatedWellDetailController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(nominationId, null, null))
        ))
        .andExpect(model().attribute(
            "wellsRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
        ))
        .andExpect(model().attribute(
            "alreadyAddedWells",
            Collections.emptyList()
        ))
        .andExpect(model().attribute(
            "wellPhases",
            DisplayableEnumOptionUtil.getDisplayableOptions(WellPhase.class)
        ))
        .andExpect(model().attributeExists("accidentRegulatorBranding"))
        .andExpect(model().attributeExists("org.springframework.validation.BindingResult.accidentRegulatorBranding"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(
        (AccidentRegulatorConfigurationProperties) modelAndView.getModel().get("accidentRegulatorBranding")
    )
        .hasNoNullFieldsOrProperties();
  }

  /**
   * Wellbores are sorted by a series of properties (block, platform letters, drilling sequence etc.) which
   * are values not exposed over the EPA api. Assert that the wells are ordered by the order they are returned
   * from the well query service
   */
  @Test
  void renderNominatedWellDetail_whenSavedWells_thenWellInOrderFromQueryService() throws Exception {

    var formWithWells = NominatedWellFormTestUtil.builder()
        .withWell(1)
        .withWell(2)
        .build();

    var wellboreIdsFromForm = formWithWells.getWells()
        .stream()
        .map(WellboreId::new)
        .toList();

    when(nominatedWellDetailFormService.getForm(nominationDetail)).thenReturn(formWithWells);

    var firstWellDto = WellDtoTestUtil.builder()
        .withWellboreId(1)
        .build();

    var secondWellDto = WellDtoTestUtil.builder()
        .withWellboreId(2)
        .build();

    // controller should return wellbores in the same order as they
    // are returned from the query service
    when(wellQueryService.getWellsByIds(wellboreIdsFromForm, NominatedWellDetailController.ALREADY_ADDED_WELLS_PURPOSE))
        .thenReturn(List.of(secondWellDto, firstWellDto));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("alreadyAddedWells"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    @SuppressWarnings("unchecked")
    var returnedAlreadyAddedWells = (List<WellAddToListView>) modelAndView.getModel().get("alreadyAddedWells");

    assertThat(returnedAlreadyAddedWells)
        .extracting(WellAddToListView::getId)
        .containsExactly(
            String.valueOf(secondWellDto.wellboreId().id()),
            String.valueOf(firstWellDto.wellboreId().id())
        );
  }

  @Test
  void saveNominatedWellDetail_whenNoValidationErrors_verifyMethodCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");

    when(nominatedWellDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(nominationId, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(nominationId))));

    verify(nominatedWellDetailPersistenceService, times(1)).createOrUpdateNominatedWellDetail(eq(nominationDetail), any());
  }

  @Test
  void saveNominatedWellDetail_whenValidationErrors_verifyRedirection() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedWellDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(nominationId, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk());

    verify(nominatedWellDetailPersistenceService, never()).createOrUpdateNominatedWellDetail(any(), any());
  }
}