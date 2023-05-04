package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailService;

@ExtendWith(MockitoExtension.class)
class NomineeDetailSubmissionServiceTest {

  @Mock
  private NomineeDetailFormService nomineeDetailFormService;

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private UploadedFileDetailService uploadedFileDetailService;

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
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());
    var form = new NomineeDetailForm();
    form.setAppendixDocuments(List.of(fileUploadForm));

    nomineeDetailSubmissionService.submit(detail, form);

    verify(nomineeDetailPersistenceService).createOrUpdateNomineeDetail(detail, form);
    verify(uploadedFileDetailService).submitFiles(List.of(fileUploadForm));
  }

}