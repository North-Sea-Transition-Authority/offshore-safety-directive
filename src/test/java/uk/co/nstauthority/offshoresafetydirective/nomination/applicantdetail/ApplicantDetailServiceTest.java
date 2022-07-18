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
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailServiceTest {

  private final NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();

  private static ApplicationDetailRepository applicationDetailRepository;
  private static ApplicantDetailFormValidator applicantDetailFormValidator;

  private static ApplicantDetailService applicantDetailService;

  @BeforeAll
  static void setup() {
    applicationDetailRepository = mock(ApplicationDetailRepository.class);
    applicantDetailFormValidator = mock(ApplicantDetailFormValidator.class);
    applicantDetailService = new ApplicantDetailService(applicationDetailRepository, applicantDetailFormValidator);
  }

  @Test
  void createOrUpdateApplicantDetail_whenGivenAForm_verifyCreatedEntity() {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("ref#1");
    var applicantDetailArgumentCaptor = ArgumentCaptor.forClass(ApplicantDetail.class);
    when(applicationDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.empty());

    applicantDetailService.createOrUpdateApplicantDetail(form, nominationDetail);

    verify(applicationDetailRepository, times(1)).save(applicantDetailArgumentCaptor.capture());
    var savedApplicantDetail = (ApplicantDetail) applicantDetailArgumentCaptor.getValue();
    
    assertThat(savedApplicantDetail)
        .extracting(
            ApplicantDetail::getPortalOrganisationId,
            ApplicantDetail::getApplicantReference,
            ApplicantDetail::getNominationDetail
        )
        .containsExactly(
            form.getPortalOrganisationId(),
            form.getApplicantReference(),
            nominationDetail
        );
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    applicantDetailService.validate(form, bindingResult);

    verify(applicantDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenPreviousApplicantDetail_thenAssertFormFields() {
    var applicantDetail = new ApplicantDetail(nominationDetail, 1, "ref #1");
    when(applicationDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.of(applicantDetail));

    var form = applicantDetailService.getForm(nominationDetail);

    assertThat(form)
        .extracting(
            ApplicantDetailForm::getPortalOrganisationId,
            ApplicantDetailForm::getApplicantReference
        )
        .containsExactly(
            applicantDetail.getPortalOrganisationId(),
            applicantDetail.getApplicantReference()
        );
  }
}