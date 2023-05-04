package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldAddToListItem;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RelatedInformationController.class})
class RelatedInformationControllerTest extends AbstractControllerTest {

  private NominationDetail nominationDetail;
  private ServiceUserDetail user;

  @MockBean
  private FieldRestService fieldRestService;

  @MockBean
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @MockBean
  private RelatedInformationFormService relatedInformationFormService;

  @MockBean
  private RelatedInformationValidator relatedInformationValidator;

  @Autowired
  private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    user = ServiceUserDetailTestUtil.Builder()
        .build();

    when(nominationDetailService.getLatestNominationDetail(new NominationId(nominationDetail)))
        .thenReturn(nominationDetail);

    var nominationCreatorTeamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(Collections.singletonList(nominationCreatorTeamMember));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var nominationId = new NominationId(nominationDetail);

    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(user)
        .withGetEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class)
                .submitRelatedInformation(nominationId, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var nominationId = new NominationId(nominationDetail);

    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(user)
        .withGetEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class)
                .submitRelatedInformation(nominationId, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void renderRelatedInformation_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(new NominationId(nominationDetail)))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderRelatedInformation_whenLoggedIn_thenOk() throws Exception {

    var fieldList = List.of(new FieldAddToListItem("543", "Field", true));
    when(fieldRestService.getAddToListItemsFromFieldIds(any())).thenReturn(fieldList);

    when(nominationDetailService.getLatestNominationDetail(new NominationId(nominationDetail)))
        .thenReturn(nominationDetail);
    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(new NominationId(nominationDetail))))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModelMap())
        .extractingByKeys("pageTitle", "actionUrl", "fieldRestUrl", "preselectedFields", "approvalsEmailAddress")
        .containsExactly(
            RelatedInformationController.PAGE_NAME,
            ReverseRouter.route(on(RelatedInformationController.class)
                .submitRelatedInformation(new NominationId(nominationDetail), null, null)),
            ReverseRouter.route(on(FieldRestController.class).getActiveFields(null)),
            fieldList,
            applicationContext.getBean(CustomerConfigurationProperties.class).businessEmailAddress()
        );
  }

  @SecurityTest
  void editMember_whenNotAuthorized_thenRedirectToLoginPage() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(new NominationId(nominationDetail), null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitRelatedInformation_whenLoggedIn_andFormValid_thenRedirect() throws Exception {

    var expectedRedirect = ReverseRouter.redirect(on(NominationTaskListController.class)
        .getTaskList(new NominationId(nominationDetail)));

    mockMvc.perform(post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(new NominationId(nominationDetail), null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name(Objects.requireNonNull(expectedRedirect.getViewName())));

    verify(relatedInformationPersistenceService).createOrUpdateRelatedInformation(eq(nominationDetail), any());
  }

  @Test
  void submitRelatedInformation_whenLoggedIn_andFormInvalid_thenOk() throws Exception {

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue(
          RelatedInformationValidator.FIELDS_FIELD_NAME,
          RelatedInformationValidator.FIELDS_REQUIRED_CODE,
          RelatedInformationValidator.FIELDS_REQUIRED_MESSAGE
      );
      return invocation;
    }).when(relatedInformationValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(new NominationId(nominationDetail), null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/relatedInformation/relatedInformation"));

    verifyNoInteractions(relatedInformationPersistenceService);
  }
}