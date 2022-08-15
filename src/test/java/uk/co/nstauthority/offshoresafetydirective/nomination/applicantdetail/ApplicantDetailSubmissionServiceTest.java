package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailSubmissionServiceTest {

  @Mock
  private ApplicantDetailFormService applicantDetailFormService;

  @InjectMocks
  private ApplicantDetailSubmissionService applicantDetailSubmissionService;

  @Test
  void isSectionSubmittable_whenSubmittable_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .build();

    var applicantDetailForm = new ApplicantDetailForm();

    var emptyBindingResult = ReverseRouter.emptyBindingResult();

    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(applicantDetailForm);

    when(applicantDetailFormService.validate(eq(applicantDetailForm), any(BindingResult.class)))
        .thenReturn(emptyBindingResult);

    assertTrue(
        applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)
    );
  }

  @Test
  void isSectionSubmittable_whenSubmittable_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .build();

    var applicantDetailForm = new ApplicantDetailForm();

    var bindingResultWithError = ReverseRouter.emptyBindingResult();
    bindingResultWithError.addError(new FieldError("object", "field", "message"));

    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(applicantDetailForm);

    when(applicantDetailFormService.validate(eq(applicantDetailForm), any(BindingResult.class)))
        .thenReturn(bindingResultWithError);

    assertFalse(
        applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)
    );
  }

}