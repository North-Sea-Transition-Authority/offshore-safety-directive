package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationSubmissionServiceTest {

  @Mock
  private RelatedInformationFormService relatedInformationFormService;

  @Mock
  private RelatedInformationValidator relatedInformationValidator;

  @InjectMocks
  private RelatedInformationSubmissionService relatedInformationSubmissionService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
  }

  @Test
  void isSectionSubmittable_whenValid_thenTrue() {
    var form = new RelatedInformationForm();
    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(form);

    var result = relatedInformationSubmissionService.isSectionSubmittable(nominationDetail);

    assertTrue(result);
  }

  @Test
  void isSectionSubmittable_whenInvalid_thenFalse() {
    var form = new RelatedInformationForm();
    when(relatedInformationFormService.getForm(nominationDetail)).thenReturn(form);

    // Add an error to the passed BindingResult
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));
      return invocation;
    }).when(relatedInformationValidator).validate(eq(form), any());

    var result = relatedInformationSubmissionService.isSectionSubmittable(nominationDetail);

    assertFalse(result);
  }
}