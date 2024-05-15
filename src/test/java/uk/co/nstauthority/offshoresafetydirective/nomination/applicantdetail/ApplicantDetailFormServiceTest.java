package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailFormServiceTest {

  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private static ApplicantDetailFormValidator applicantDetailFormValidator;

  private static ApplicantDetailFormService applicantDetailFormService;

  @BeforeAll
  static void setup() {
    applicantDetailPersistenceService = mock(ApplicantDetailPersistenceService.class);
    applicantDetailFormValidator = mock(ApplicantDetailFormValidator.class);
    applicantDetailFormService = new ApplicantDetailFormService(applicantDetailPersistenceService, applicantDetailFormValidator);
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    applicantDetailFormService.validate(form, bindingResult);

    verify(applicantDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenPreviousApplicantDetail_thenAssertFormFields() {
    var applicantDetail = new ApplicantDetail(nominationDetail, 1, "ref #1");
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.of(applicantDetail));

    var form = applicantDetailFormService.getForm(nominationDetail);

    assertThat(form)
        .extracting(
            ApplicantDetailForm::getPortalOrganisationId,
            ApplicantDetailForm::getApplicantReference
        )
        .containsExactly(
            String.valueOf(applicantDetail.getPortalOrganisationId()),
            applicantDetail.getApplicantReference()
        );
  }

  @Test
  void getForm_whenNoPreviousApplicantDetail_thenAssertEmptyFormFields() {

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.empty());

    var form = applicantDetailFormService.getForm(nominationDetail);

    assertThat(form).hasAllNullFieldsOrProperties();
  }
}