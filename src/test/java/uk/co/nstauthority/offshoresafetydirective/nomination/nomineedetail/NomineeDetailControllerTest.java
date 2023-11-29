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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeAccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.UnlinkedFileController;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDraftFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NomineeDetailController.class)
@IncludeAccidentRegulatorConfigurationProperties
@EnableConfigurationProperties(value = FileUploadProperties.class)
class NomineeDetailControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
      .build();

  private final NominationId nominationId = new NominationId(UUID.randomUUID());
  private NominationDetail nominationDetail;
  private NomineeDetailForm form;

  @MockBean
  private NomineeDetailFormService nomineeDetailFormService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @MockBean
  private FileService fileService;

  @Autowired
  private AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;

  @Autowired
  private FileUploadProperties fileUploadProperties;

  @BeforeEach
  void setup() {
    form = new NomineeDetailForm();

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(nominationId)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(nomineeDetailFormService.getForm(nominationDetail)).thenReturn(form);
    when(portalOrganisationUnitQueryService.getOrganisationById(any(), any())).thenReturn(Optional.empty());

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

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

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

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

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

    form.setNominatedOrganisationId("100");

    var uploadedFileForm = UploadedFileFormTestUtil.builder().build();
    form.setAppendixDocuments(List.of(uploadedFileForm));

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(20)
        .withName("name")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailController.PRE_SELECTED_OPERATOR_PURPOSE
    ))
        .thenReturn(Optional.of(portalOrganisationUnit));

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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name()))
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
        .andExpect(model().attribute("uploadedFiles", List.of(uploadedFileForm)))
        .andExpect(model().attribute("appendixDocumentFileUploadTemplate", FileUploadComponentAttributes.newBuilder()
            .withDownloadUrl(ReverseRouter.route(
                on(NominationDraftFileController.class).download(nominationId, null))
            )
            .withUploadUrl(ReverseRouter.route(
                on(UnlinkedFileController.class).upload(null, FileDocumentType.APPENDIX_C.name()))
            )
            .withDeleteUrl(ReverseRouter.route(
                on(NominationDraftFileController.class).delete(nominationId, null))
            )
            .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
            .withAllowedExtensions(
                FileDocumentType.APPENDIX_C.getAllowedExtensions()
                    .orElse(fileUploadProperties.defaultPermittedFileExtensions())
            )
            .build()
        ));
  }

  @Test
  void getNomineeDetail_whenInvalidPreviouslySelectedItem_thenEmptyMap() throws Exception {

    form.setNominatedOrganisationId("FISH");

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

    var uploadedFileForm = UploadedFileFormTestUtil.builder().build();
    form.setAppendixDocuments(List.of(uploadedFileForm));

    mockMvc.perform(
            get(ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/nomineeDetails/nomineeDetail"))
        .andExpect(model().attribute("preselectedItems", Map.of()));

    verify(portalOrganisationUnitQueryService, never()).getOrganisationById(any(), any());
  }

  @Test
  void saveNomineeDetail_whenValidForm_verifyMethodCalls() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

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
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    var uploadedFileId = new UploadedFileId(UUID.randomUUID());
    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(uploadedFileId.uuid())
        .build();

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
                .withAllowedExtensions(fileUploadProperties.defaultPermittedFileExtensions())
        );

    var uploadedFileForm = UploadedFileFormTestUtil.builder()
        .withFileId(uploadedFileId.uuid())
        .withFileName(uploadedFile.getName())
        .withFileSize(FileUploadLibraryUtils.formatSize(100))
        .withFileUploadedAt(Instant.now())
        .withFileDescription("description")
        .build();

    var modelAndView = mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
                .param("appendixDocuments[0].uploadedFileId", uploadedFileForm.getFileId().toString())
                .param("appendixDocuments[0].fileName", uploadedFileForm.getFileName())
                .param("appendixDocuments[0].fileSize", uploadedFileForm.getFileSize())
                .param("appendixDocuments[0].fileUploadedAt", uploadedFileForm.getFileUploadedAt().toString())
                .param("appendixDocuments[0].fileDescription", uploadedFileForm.getFileDescription())
        )
        .andExpect(status().isOk())
        .andExpect(model().attribute("appendixDocumentFileUploadTemplate", FileUploadComponentAttributes.newBuilder()
            .withDownloadUrl(ReverseRouter.route(
                on(NominationDraftFileController.class).download(nominationId, null))
            )
            .withUploadUrl(ReverseRouter.route(
                on(UnlinkedFileController.class).upload(null, FileDocumentType.APPENDIX_C.name()))
            )
            .withDeleteUrl(ReverseRouter.route(
                on(NominationDraftFileController.class).delete(nominationId, null))
            )
            .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
            .withAllowedExtensions(
                FileDocumentType.APPENDIX_C.getAllowedExtensions()
                    .orElse(fileUploadProperties.defaultPermittedFileExtensions())
            )
            .build()
        ))
        .andReturn()
        .getModelAndView();

    verify(nomineeDetailFormService, times(1)).validate(any(), any());
    verify(nomineeDetailSubmissionService, never()).submit(any(), any());

    @SuppressWarnings("unchecked")
    var uploadedFiles = (List<UploadedFileForm>) Objects.requireNonNull(modelAndView).getModel().get("uploadedFiles");
    assertThat(uploadedFiles)
        .extracting(
            UploadedFileForm::getFileId,
            UploadedFileForm::getFileName,
            UploadedFileForm::getFileSize,
            UploadedFileForm::getFileDescription,
            UploadedFileForm::getFileUploadedAt
        )
        .containsExactly(
            Tuple.tuple(
              uploadedFileForm.getFileId(),
              uploadedFileForm.getFileName(),
              uploadedFileForm.getFileSize(),
              uploadedFileForm.getFileDescription(),
              uploadedFileForm.getFileUploadedAt()
            ));
  }
}