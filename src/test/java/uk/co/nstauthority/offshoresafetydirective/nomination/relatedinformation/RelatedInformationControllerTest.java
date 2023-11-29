package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldAddToListItem;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = RelatedInformationController.class)
class RelatedInformationControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private NominationDetail nominationDetail;

  private ServiceUserDetail user;

  @MockBean
  private EnergyPortalFieldQueryService fieldQueryService;

  @MockBean
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @MockBean
  private RelatedInformationFormService relatedInformationFormService;

  @MockBean
  private RelatedInformationValidator relatedInformationValidator;

  @Autowired
  private CustomerConfigurationProperties customerConfigurationProperties;

  @BeforeEach
  void setUp() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    user = ServiceUserDetailTestUtil.Builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    givenUserHasNominationPermission(nominationDetail, user);
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(user)
        .withGetEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class)
                .submitRelatedInformation(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyEditNominationPermissionAllowed() {

    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.EDIT_NOMINATION))
        .withUser(user)
        .withTeam(getTeam())
        .withGetEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(RelatedInformationController.class)
                .submitRelatedInformation(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderRelatedInformation_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(new NominationId(nominationDetail)))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderRelatedInformation_whenLoggedIn_thenOk() throws Exception {

    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(new RelatedInformationForm());

    mockMvc.perform(
        get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(NOMINATION_ID)
        ))
            .with(user(user))
    )
        .andExpect(status().isOk());
  }

  @Test
  void renderRelatedInformation_whenNoPreSelectedFields_thenVerifyModelAndViewProperties() throws Exception {

    var form = new RelatedInformationForm();
    when(relatedInformationFormService.getForm(nominationDetail))
        .thenReturn(form);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(NOMINATION_ID)
        ))
            .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/relatedInformation/relatedInformation"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var modelMap = modelAndView.getModel();

    assertThat(modelMap)
        .isNotNull()
        .containsEntry("pageTitle", RelatedInformationController.PAGE_NAME)
        .containsEntry("actionUrl", ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(NOMINATION_ID, null, ReverseRouter.emptyBindingResult())))
        .containsEntry("form", form)
        .containsEntry("fieldRestUrl", ReverseRouter.route(on(FieldRestController.class).getActiveFields(null)))
        .containsEntry("preselectedFields", Collections.emptyList())
        .containsEntry("approvalsEmailAddress", customerConfigurationProperties.businessEmailAddress());
  }

  @Test
  void renderRelatedInformation_whenPreSelectedFields_thenVerifyModelAndViewProperties() throws Exception {

    var firstFieldByName = FieldDtoTestUtil.builder()
        .withId(10)
        .withName("a name")
        .withStatus(FieldStatus.STATUS100) // active status
        .build();

    var secondFieldByName = FieldDtoTestUtil.builder()
        .withId(20)
        .withName("B name")
        .withStatus(FieldStatus.STATUS9999) // non-active status
        .build();

    var form = RelatedInformationFormTestUtil.builder()
        .withField(firstFieldByName.fieldId().id())
        .withField(secondFieldByName.fieldId().id())
        .build();

    when(relatedInformationFormService.getForm(nominationDetail))
        .thenReturn(form);

    when(fieldQueryService.getFieldsByIds(
        Set.of(new FieldId(10), new FieldId(20)),
        RelatedInformationController.PRE_SELECTED_FIELDS_PURPOSE
    ))
        // return out of order to verify sort
        .thenReturn(List.of(secondFieldByName, firstFieldByName));

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(RelatedInformationController.class)
            .renderRelatedInformation(NOMINATION_ID)
        ))
            .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/relatedInformation/relatedInformation"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var modelMap = modelAndView.getModel();

    assertThat(modelMap)
        .isNotNull()
        .containsKey("preselectedFields");

    @SuppressWarnings("unchecked")
    List<FieldAddToListItem> preselectedFields = (List<FieldAddToListItem>) modelMap.get("preselectedFields");

    assertThat(preselectedFields)
        .extracting(
            FieldAddToListItem::getId,
            FieldAddToListItem::getName,
            FieldAddToListItem::isValid
        )
        .containsExactly(
            tuple(String.valueOf(firstFieldByName.fieldId().id()), firstFieldByName.name(), true),
            tuple(String.valueOf(secondFieldByName.fieldId().id()), secondFieldByName.name(), false)
        );
  }

  @SecurityTest
  void submitRelatedInformation_whenNotLoggedIn_thenRedirectedToLoginPage() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(new NominationId(nominationDetail), null, null)))
            .with(csrf())
        )
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitRelatedInformation_whenValidForm_thenRedirectToTaskList() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(NOMINATION_ID, null, null))
        )
            .with(csrf())
            .with(user(user))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ));

    verify(relatedInformationPersistenceService).createOrUpdateRelatedInformation(eq(nominationDetail), any());
  }

  @Test
  void submitRelatedInformation_whenFormHasErrors_thenOk() throws Exception {

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue(
          RelatedInformationValidator.FIELDS_FIELD_NAME,
          RelatedInformationValidator.FIELDS_REQUIRED_CODE,
          RelatedInformationValidator.FIELDS_REQUIRED_MESSAGE
      );
      return invocation;
    })
        .when(relatedInformationValidator).validate(any(), any());

    var modelAndView = mockMvc.perform(
        post(ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(NOMINATION_ID, null, null))
        )
            .with(csrf())
            .with(user(user))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/relatedInformation/relatedInformation"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var modelMap = modelAndView.getModel();

    assertThat(modelMap)
        .isNotNull()
        .containsEntry("pageTitle", RelatedInformationController.PAGE_NAME)
        .containsEntry("actionUrl", ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(NOMINATION_ID, null, ReverseRouter.emptyBindingResult())))
        .containsEntry("fieldRestUrl", ReverseRouter.route(on(FieldRestController.class).getActiveFields(null)))
        .containsEntry("preselectedFields", Collections.emptyList())
        .containsEntry("approvalsEmailAddress", customerConfigurationProperties.businessEmailAddress())
        .containsKey("form");

    verifyNoInteractions(relatedInformationPersistenceService);
  }
}