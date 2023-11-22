package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NomineeDetailSubmissionServiceTest {

  @Mock
  private NomineeDetailFormService nomineeDetailFormService;

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private FileService fileService;

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @Test
  void isSectionSubmittable_whenSubmittable_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .build();

    var nomineeDetailForm = new NomineeDetailForm();

    var emptyBindingResult = ReverseRouter.emptyBindingResult();

    when(nomineeDetailFormService.getForm(nominationDetail)).thenReturn(nomineeDetailForm);

    when(nomineeDetailFormService.validate(eq(nomineeDetailForm), any(BindingResult.class)))
        .thenReturn(emptyBindingResult);

    assertTrue(
        nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)
    );
  }

  @Test
  void isSectionSubmittable_whenSubmittable_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .build();

    var nomineeDetailForm = new NomineeDetailForm();

    var bindingResultWithError = ReverseRouter.emptyBindingResult();
    bindingResultWithError.addError(new FieldError("object", "field", "message"));

    when(nomineeDetailFormService.getForm(nominationDetail)).thenReturn(nomineeDetailForm);

    when(nomineeDetailFormService.validate(eq(nomineeDetailForm), any(BindingResult.class)))
        .thenReturn(bindingResultWithError);

    assertFalse(
        nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)
    );
  }

  @Test
  void submit() {
    var detail = NominationDetailTestUtil.builder().build();
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());
    var form = new NomineeDetailForm();
    form.setAppendixDocuments(List.of(uploadedFileForm));

    var description = "desc";
    uploadedFileForm.setFileDescription(description);

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail())
        .thenReturn(user);

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withId(uploadedFileForm.getUploadedFileId())
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(user.wuaId().toString())
        .build();

    when(fileService.findAll(List.of(uploadedFileForm.getUploadedFileId())))
        .thenReturn(List.of(uploadedFile));

    nomineeDetailSubmissionService.submit(detail, form);


    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptor =
        ArgumentCaptor.forClass(Function.class);

    verify(fileService).updateUsageAndDescription(eq(uploadedFile), fileUsageFunctionCaptor.capture(), eq(description));

    assertThat(fileUsageFunctionCaptor.getValue().apply(FileUsage.newBuilder()))
        .extracting(
            FileUsage::usageId,
            FileUsage::usageType,
            FileUsage::documentType
        )
        .containsExactly(
            detail.getId().toString(),
            FileUsageType.NOMINATION_DETAIL.getUsageType(),
            FileDocumentType.APPENDIX_C.getDocumentType()
        );
  }

  @Test
  void submit_whenNotAllFilesCanBeSubmitted_verifyError() {
    var detail = NominationDetailTestUtil.builder().build();
    var firstUploadedFileForm = new UploadedFileForm();
    firstUploadedFileForm.setUploadedFileId(UUID.randomUUID());

    var secondUploadedFileForm = new UploadedFileForm();
    secondUploadedFileForm.setUploadedFileId(UUID.randomUUID());

    var form = new NomineeDetailForm();
    form.setAppendixDocuments(List.of(firstUploadedFileForm, secondUploadedFileForm));

    var description = "desc";
    firstUploadedFileForm.setFileDescription(description);

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail())
        .thenReturn(user);

    var uploadedFile = UploadedFileTestUtil.newBuilder()
        .withId(firstUploadedFileForm.getUploadedFileId())
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(user.wuaId().toString())
        .build();

    when(fileService.findAll(List.of(firstUploadedFileForm.getFileId(), secondUploadedFileForm.getFileId())))
        .thenReturn(List.of(uploadedFile));

    assertThatThrownBy(() -> nomineeDetailSubmissionService.submit(detail, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Not all documents [%s, %s] can be linked to nominee details for NominationDetail [%s]".formatted(
            firstUploadedFileForm.getFileId(),
            secondUploadedFileForm.getFileId(),
            detail.getId()
        ));
  }

}