// TODO OSDOP-811
//package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
//import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
//import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
//
//import java.util.Collections;
//import java.util.EnumSet;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ContextConfiguration;
//import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
//import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
//import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
//import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
//import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
//import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
//import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
//import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
//import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
//
//@ContextConfiguration(classes = NominationPortalReferenceController.class)
//class NominationPortalReferenceControllerTest extends AbstractNominationControllerTest {
//
//  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
//
//  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();
//
//  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
//      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
//      .build();
//
//  @MockBean
//  private NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService;
//
//  private NominationDetail nominationDetail;
//
//  @BeforeEach
//  void setup() {
//    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
//        .withNominationId(NOMINATION_ID)
//        .withStatus(NominationStatus.SUBMITTED)
//        .build();
//
//    // when retrieving the nomination detail in the post request
//    when(nominationDetailService.getLatestNominationDetailWithStatuses(
//        NOMINATION_ID,
//        EnumSet.of(NominationStatus.SUBMITTED)
//    ))
//        .thenReturn(Optional.of(nominationDetail));
//
//    // for checking the nomination detail in the @HasNominationStatus annotation
//    when(nominationDetailService.getLatestNominationDetailWithStatuses(
//        NOMINATION_ID,
//        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
//    ))
//        .thenReturn(Optional.of(nominationDetail));
//
//    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGER_USER))
//        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));
//  }
//
//  @SecurityTest
//  void smokeTestNominationStatuses_onlySubmittedPermitted() {
//    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
//        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
//        .withNominationDetail(nominationDetail)
//        .withUser(NOMINATION_MANAGER_USER)
//        .withPostEndpoint(ReverseRouter.route(on(NominationPortalReferenceController.class).updatePearsReferences(
//                NOMINATION_ID, true, CaseProcessingActionIdentifier.PEARS_REFERENCES, null, null, null)),
//            status().is3xxRedirection(),
//            status().isForbidden()
//        )
//        .withPostEndpoint(ReverseRouter.route(on(NominationPortalReferenceController.class).updateWonsReferences(
//                NOMINATION_ID, true, CaseProcessingActionIdentifier.WONS_REFERENCES, null, null, null)),
//            status().is3xxRedirection(),
//            status().isForbidden()
//        )
//        .test();
//  }
//
//  @SecurityTest
//  void smokeTestPermissions_onlyManagePermitted() {
//    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
//        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
//        .withUser(NOMINATION_MANAGER_USER)
//        .withPostEndpoint(ReverseRouter.route(on(NominationPortalReferenceController.class).updatePearsReferences(
//                NOMINATION_ID, true, CaseProcessingActionIdentifier.PEARS_REFERENCES, null, null, null)),
//            status().is3xxRedirection(),
//            status().isForbidden()
//        )
//        .withPostEndpoint(ReverseRouter.route(on(NominationPortalReferenceController.class).updateWonsReferences(
//                NOMINATION_ID, true, CaseProcessingActionIdentifier.WONS_REFERENCES, null, null, null)),
//            status().is3xxRedirection(),
//            status().isForbidden()
//        )
//        .test();
//  }
//
//  @Test
//  void updatePearsReferences_whenNewReferenceProvided_thenVerifyUpdated() throws Exception {
//
//    var expectedNotificationBanner = NotificationBanner.builder()
//        .withBannerType(NotificationBannerType.SUCCESS)
//        .withHeading(
//            "PEARS references for %s have been updated"
//                .formatted(nominationDetail.getNomination().getReference())
//        )
//        .build();
//
//    var newReferences = "new/ref";
//
//    mockMvc.perform(post(ReverseRouter.route(
//            on(NominationPortalReferenceController.class).updatePearsReferences(NOMINATION_ID, true,
//                CaseProcessingActionIdentifier.PEARS_REFERENCES, null, null, null)))
//            .with(csrf())
//            .with(user(NOMINATION_MANAGER_USER))
//            .param("references.inputValue", newReferences))
//        .andExpect(redirectedUrl(
//            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
//        .andExpect(notificationBanner(expectedNotificationBanner));
//
//    verify(nominationPortalReferencePersistenceService).updatePortalReferences(nominationDetail.getNomination(),
//        PortalReferenceType.PEARS, newReferences);
//  }
//
//  @Test
//  void updatePearsReferences_whenNoReferenceProvided_thenVerifyUpdated() throws Exception {
//
//    var expectedNotificationBanner = NotificationBanner.builder()
//        .withBannerType(NotificationBannerType.SUCCESS)
//        .withHeading(
//            "PEARS references for %s have been updated"
//                .formatted(nominationDetail.getNomination().getReference())
//        )
//        .build();
//
//    mockMvc.perform(post(ReverseRouter.route(
//            on(NominationPortalReferenceController.class).updatePearsReferences(NOMINATION_ID, true,
//                CaseProcessingActionIdentifier.PEARS_REFERENCES, null, null, null)))
//            .with(csrf())
//            .with(user(NOMINATION_MANAGER_USER)))
//        .andExpect(redirectedUrl(
//            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
//        .andExpect(notificationBanner(expectedNotificationBanner));
//
//    verify(nominationPortalReferencePersistenceService).updatePortalReferences(nominationDetail.getNomination(),
//        PortalReferenceType.PEARS, null);
//  }
//
//  @Test
//  void updateWonsReferences_whenNewReferenceProvided_thenVerifyUpdated() throws Exception {
//
//    var expectedNotificationBanner = NotificationBanner.builder()
//        .withBannerType(NotificationBannerType.SUCCESS)
//        .withHeading(
//            "WONS references for %s have been updated"
//                .formatted(nominationDetail.getNomination().getReference())
//        )
//        .build();
//
//    var newReferences = "new/ref";
//
//    mockMvc.perform(post(ReverseRouter.route(
//            on(NominationPortalReferenceController.class).updateWonsReferences(NOMINATION_ID, true,
//                CaseProcessingActionIdentifier.WONS_REFERENCES, null, null, null)))
//            .with(csrf())
//            .with(user(NOMINATION_MANAGER_USER))
//            .param("references.inputValue", newReferences))
//        .andExpect(redirectedUrl(
//            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
//        .andExpect(notificationBanner(expectedNotificationBanner));
//
//    verify(nominationPortalReferencePersistenceService).updatePortalReferences(nominationDetail.getNomination(),
//        PortalReferenceType.WONS, newReferences);
//  }
//
//  @Test
//  void updateWonsReferences_whenNoReferenceProvided_thenVerifyUpdated() throws Exception {
//
//    var expectedNotificationBanner = NotificationBanner.builder()
//        .withBannerType(NotificationBannerType.SUCCESS)
//        .withHeading(
//            "WONS references for %s have been updated"
//                .formatted(nominationDetail.getNomination().getReference())
//        )
//        .build();
//
//    mockMvc.perform(post(ReverseRouter.route(
//            on(NominationPortalReferenceController.class).updateWonsReferences(NOMINATION_ID, true,
//                CaseProcessingActionIdentifier.WONS_REFERENCES, null, null, null)))
//            .with(csrf())
//            .with(user(NOMINATION_MANAGER_USER)))
//        .andExpect(redirectedUrl(
//            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
//        .andExpect(notificationBanner(expectedNotificationBanner));
//
//    verify(nominationPortalReferencePersistenceService).updatePortalReferences(nominationDetail.getNomination(),
//        PortalReferenceType.WONS, null);
//  }
//}