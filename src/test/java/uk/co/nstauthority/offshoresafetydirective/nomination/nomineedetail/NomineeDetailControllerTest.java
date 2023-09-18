package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NomineeDetailController.class)
@IncludeAccidentRegulatorConfigurationProperties
class NomineeDetailControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private final NominationId nominationId = new NominationId(UUID.randomUUID());
  private NominationDetail nominationDetail;
  private NomineeDetailForm form;

  @MockBean
  private NomineeDetailFormService nomineeDetailFormService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private FileUploadConfig fileUploadConfig;
  @MockBean
  private FileUploadService fileUploadService;
  @MockBean
  private FileAssociationService fileAssociationService;
  @MockBean
  private NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @Autowired
  private AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;

  @BeforeEach
  void setup() {
    form = new NomineeDetailForm();

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(nominationId)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(nomineeDetailFormService.getForm(nominationDetail)).thenReturn(form);
    when(portalOrganisationUnitQueryService.getOrganisationById(any())).thenReturn(Optional.empty());

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NomineeDetailController.class)
                .saveNomineeDetail(nominationId, form, bindingResult)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NomineeDetailController.class)
                .saveNomineeDetail(nominationId, form, bindingResult)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getNomineeDetail_assertModelProperties() throws Exception {

    form.setNominatedOrganisationId(100);

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var nominationDetailId = nominationDetailDto.nominationDetailId();
    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(20)
        .withName("name")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(portalOrganisationUnit));

    var fileReferenceCaptor = ArgumentCaptor.forClass(NominationDetailFileReference.class);
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);

    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    ))
        .thenReturn(
            Map.of(
                NomineeDetailAppendixFileController.PURPOSE,
                List.of(uploadedFileView)
            ));

    mockMvc.perform(
            get(ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/nomineeDetails/nomineeDetail"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("pageTitle", NomineeDetailController.PAGE_NAME))
        .andExpect(model().attribute("accidentRegulatorBranding", accidentRegulatorConfigurationProperties))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
        ))
        .andExpect(model().attribute(
            "preselectedItems",
            Map.of(
                String.valueOf(portalOrganisationUnit.id()),
                "%s (%s)".formatted(portalOrganisationUnit.name(), portalOrganisationUnit.registeredNumber().value())
            )
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, null, null))
        ))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)),
                NominationTaskListController.PAGE_NAME
            )
        ))
        .andExpect(model().attribute("uploadedFiles", List.of(uploadedFileView)))
        .andExpect(model().attribute("appendixDocumentFileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).download(nominationId, nominationDetailId, null)),
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).upload(nominationId, nominationDetailId, null)),
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).delete(nominationId, nominationDetailId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            )
        ));
  }

  @Test
  void saveNomineeDetail_whenValidForm_verifyMethodCalls() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection());

    verify(nomineeDetailFormService, times(1)).validate(any(), any());
    verify(nominationDetailService, times(2)).getLatestNominationDetail(nominationId);
    verify(nomineeDetailSubmissionService, times(1)).submit(eq(nominationDetail), any());
  }

  @Test
  void saveNomineeDetail_whenInvalidForm_verifyStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var nominationDetailId = nominationDetailDto.nominationDetailId();
    var uploadedFileId = new UploadedFileId(UUID.randomUUID());
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<FileUploadForm>> fileUploadFormListCaptor = ArgumentCaptor.forClass(List.class);
    when(fileUploadService.getUploadedFileViewListFromForms(fileUploadFormListCaptor.capture()))
        .thenReturn(List.of(uploadedFileView));

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
                .param("appendixDocuments[0].uploadedFileId", uploadedFileId.uuid().toString())
        )
        .andExpect(status().isOk())
        .andExpect(model().attribute("appendixDocumentFileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).download(nominationId, nominationDetailId, null)),
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).upload(nominationId, nominationDetailId, null)),
                ReverseRouter.route(
                    on(NomineeDetailAppendixFileController.class).delete(nominationId, nominationDetailId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            )
        ))
        .andExpect(model().attribute("uploadedFiles", List.of(uploadedFileView)));

    assertThat(fileUploadFormListCaptor.getValue())
        .extracting(FileUploadForm::getUploadedFileId)
        .containsExactly(uploadedFileId.uuid());

    verify(nomineeDetailFormService, times(1)).validate(any(), any());
    verify(nomineeDetailSubmissionService, never()).submit(any(), any());
  }
}