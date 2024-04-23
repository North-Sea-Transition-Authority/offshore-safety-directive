package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;

@ContextConfiguration(classes = InstallationJourneyManagerController.class)
class InstallationJourneyManagerControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private InstallationInclusionAccessService installationInclusionAccessService;

  @MockBean
  private NominatedInstallationAccessService nominatedInstallationAccessService;

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
  void installationJourneyManager_onlyDraftNominationStatusPermitted() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(InstallationJourneyManagerController.class).installationJourneyManager(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void installationJourneyManager_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void installationJourneyManager_whenNoRoleInApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void installationJourneyManager_whenNoNominationDetailFound_thenNotFoundResponse() throws Exception {

    given(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void installationJourneyManager_whenInclusionQuestionNotAnswered_thenRedirectToInclusionEndpoint() throws Exception {

    given(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
            )
        );
  }

  @Test
  void installationJourneyManager_whenInclusionQuestionIsNo_thenRedirectToInclusionEndpoint() throws Exception {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .includeInstallationsInNomination(false)
        .build();

    given(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .willReturn(Optional.of(installationInclusion));

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
            )
        );
  }

  @Test
  void installationJourneyManager_whenInclusionQuestionIsYesAndNoInstallationsAdded_thenRedirectToInclusionEndpoint() throws Exception {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .includeInstallationsInNomination(true)
        .build();

    given(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .willReturn(Optional.of(installationInclusion));

    given(nominatedInstallationAccessService.getNominatedInstallations(nominationDetail))
        .willReturn(Collections.emptyList());

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
            )
        );
  }

  @Test
  void installationJourneyManager_whenInclusionQuestionIsYesAndInstallationsAdded_thenRedirectToManageEndpoint() throws Exception {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .includeInstallationsInNomination(true)
        .build();

    given(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .willReturn(Optional.of(installationInclusion));

    var nominatedInstallation = NominatedInstallationTestUtil.builder().build();

    given(nominatedInstallationAccessService.getNominatedInstallations(nominationDetail))
        .willReturn(List.of(nominatedInstallation));

    mockMvc.perform(get(ReverseRouter.route(on(InstallationJourneyManagerController.class)
        .installationJourneyManager(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(ManageInstallationsController.class).getManageInstallations(NOMINATION_ID))
            )
        );
  }
}