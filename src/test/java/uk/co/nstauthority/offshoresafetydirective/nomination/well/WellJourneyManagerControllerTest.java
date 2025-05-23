package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;

@ContextConfiguration(classes = WellJourneyManagerController.class)
class WellJourneyManagerControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockitoBean
  private WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @MockitoBean
  private NominatedWellAccessService nominatedWellAccessService;

  @MockitoBean
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    given(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .willReturn(nominationDetail);

    given(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .willReturn(Optional.of(nominationDetail));

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void wellJourneyManager_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void wellJourneyManager_whenNoRoleInApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void wellJourneyManager_onlyDraftNominationStatusPermitted() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void wellJourneyManager_whenNoNominationDetailFound_thenNotFoundResponse() throws Exception {

    given(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .willReturn(Optional.empty());

    mockMvc.perform(
        get(
            ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
        )
            .with(user(USER))
    )
        .andExpect(status().isNotFound());
  }

  @Test
  void wellJourneyManager_whenSetupQuestionNotAnswered_thenRedirectToSetupController() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.empty());

    mockMvc.perform(
        get(
            ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
        )
            .with(user(USER))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
            )
        );
  }

  @Test
  void wellJourneyManager_whenSetupQuestionIsNo_thenRedirectToSetupController() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.NO_WELLS));

    mockMvc.perform(
            get(
                ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
            )
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
            )
        );
  }

  @Test
  void wellJourneyManager_whenSpecificWellsAndNoWellsAdded_thenRedirectToSetupController() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.SPECIFIC_WELLS));

    given(nominatedWellAccessService.getNominatedWells(nominationDetail))
        .willReturn(Collections.emptyList());

    mockMvc.perform(
            get(
                ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
            )
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
            )
        );
  }

  @Test
  void wellJourneyManager_whenSubareaWellsAndNoSubareasAdded_thenRedirectToSetupController() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(Collections.emptyList());

    mockMvc.perform(
            get(
                ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
            )
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
            )
        );
  }

  @Test
  void wellJourneyManager_whenSpecificWellsAndWellsAdded_thenRedirectToSummary() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.SPECIFIC_WELLS));

    var nominatedWell = NominatedWellTestUtil.builder().build();

    given(nominatedWellAccessService.getNominatedWells(nominationDetail))
        .willReturn(List.of(nominatedWell));

    mockMvc.perform(
            get(
                ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
            )
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID))
            )
        );
  }

  @Test
  void wellJourneyManager_whenSubareaWellsAndSubareasAdded_thenRedirectToSummary() throws Exception {

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(new NominatedBlockSubareaDto(new LicenceBlockSubareaId("10"), "subarea name")));

    mockMvc.perform(
            get(
                ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
            )
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID))
            )
        );
  }
}
