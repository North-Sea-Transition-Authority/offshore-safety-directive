package uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Map;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = ManageInstallationsController.class)
class ManageInstallationsControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;


  @MockBean
  ManageInstallationService manageInstallationService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void getManageInstallations_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ManageInstallationsController.class)
        .getManageInstallations(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getManageInstallations_whenUserNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(ManageInstallationsController.class)
        .getManageInstallations(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(ManageInstallationsController.class).getManageInstallations(NOMINATION_ID))
        )
        .test();
  }

  @Test
  void getManageInstallations_assertModelAndViewProperties() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ManageInstallationsController.class)
        .getManageInstallations(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/manageInstallations"))
        .andExpect(model().attribute("pageTitle", ManageInstallationsController.PAGE_TITLE))
        .andExpect(model().attribute(
            "installationInclusionChangeUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "nominatedInstallationDetailChangeUrl",
            ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "saveAndContinueUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)),
                NominationTaskListController.PAGE_NAME
            )
        ))
        .andExpect(model().attributeExists("installationInclusionView"))
        .andExpect(model().attributeExists("nominatedInstallationDetailView"));
  }
}