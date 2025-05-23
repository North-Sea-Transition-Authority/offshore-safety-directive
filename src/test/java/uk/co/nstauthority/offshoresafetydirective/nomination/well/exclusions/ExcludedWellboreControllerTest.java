package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;

@ContextConfiguration(classes = ExcludedWellboreController.class)
class ExcludedWellboreControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private NominationDetail nominationDetail;

  @MockitoBean
  private LicenceBlockSubareaWellboreService subareaWellboreService;

  @MockitoBean
  NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @MockitoBean
  private ExcludedWellValidator excludedWellValidator;

  @MockitoBean
  private ExcludedWellPersistenceService excludedWellPersistenceService;

  @MockitoBean
  private ExcludedWellFormService excludedWellFormService;

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

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void renderPossibleWellsToExclude_whenUserIsNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ExcludedWellboreController.class)
        .renderPossibleWellsToExclude(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderPossibleWellsToExclude_whenNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(ExcludedWellboreController.class)
        .renderPossibleWellsToExclude(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void saveWellsToExclude_whenUserIsNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ExcludedWellboreController.class)
        .saveWellsToExclude(NOMINATION_ID, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveWellsToExclude_whenNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(post(ReverseRouter.route(on(ExcludedWellboreController.class)
        .saveWellsToExclude(NOMINATION_ID, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    given(excludedWellFormService.getExcludedWellForm(any()))
        .willReturn(WellExclusionFormTestUtil.builder().build());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withBodyParam("hasWellsToExclude", "false")
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

    var expectedForm = new WellExclusionForm();

    given(excludedWellFormService.getExcludedWellForm(nominationDetail))
        .willReturn(expectedForm);

    mockMvc.perform(get(ReverseRouter.route(on(ExcludedWellboreController.class)
        .renderPossibleWellsToExclude(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/exclusions/wellsToExclude"))
        .andExpect(model().attribute("form", expectedForm))
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

    given(excludedWellFormService.getExcludedWellForm(nominationDetail))
        .willReturn(WellExclusionFormTestUtil.builder().build());

    doAnswer(invocation -> ReverseRouter.emptyBindingResult()).when(excludedWellValidator).validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ExcludedWellboreController.class)
        .saveWellsToExclude(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("excludedWells", "1,2")
        .param("hasWellsToExclude", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        );

    then(excludedWellPersistenceService)
        .should()
        .saveWellsToExclude(nominationDetail, List.of(new WellboreId(1), new WellboreId(2)), true);
  }

  @Test
  void saveWellsToExclude_whenInvalidForm_verifyNoRedirection() throws Exception {

    given(excludedWellFormService.getExcludedWellForm(nominationDetail))
        .willReturn(new WellExclusionForm());

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue("hasWellsToExclude", "code", "message");
      return bindingResult;
    }).when(excludedWellValidator).validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ExcludedWellboreController.class)
        .saveWellsToExclude(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/exclusions/wellsToExclude"));

    then(excludedWellPersistenceService)
        .should(never())
        .saveWellsToExclude(eq(nominationDetail), anyList(), anyBoolean());
  }
}