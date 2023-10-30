package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NomineeDetailFormServiceTest {

  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private FileUploadService fileUploadService;

  @InjectMocks
  private NomineeDetailFormService nomineeDetailFormService;

  @Test
  void getForm_whenPreviousNomineeDetail_thenReturnFormWithRightFields() {
    var nomineeDetail = getNomineeDetail();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.of(nomineeDetail));

    assertThat(nomineeDetailFormService.getForm(nominationDetail))
        .extracting(
            NomineeDetailForm::getReasonForNomination,
            NomineeDetailForm::getPlannedStartDay,
            NomineeDetailForm::getPlannedStartMonth,
            NomineeDetailForm::getPlannedStartYear,
            NomineeDetailForm::getOperatorHasAuthority,
            NomineeDetailForm::getOperatorHasCapacity,
            NomineeDetailForm::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            nomineeDetail.getReasonForNomination(),
            String.valueOf(nomineeDetail.getPlannedStartDate().getDayOfMonth()),
            String.valueOf(nomineeDetail.getPlannedStartDate().getMonthValue()),
            String.valueOf(nomineeDetail.getPlannedStartDate().getYear()),
            String.valueOf(nomineeDetail.getOperatorHasAuthority()),
            String.valueOf(nomineeDetail.getOperatorHasCapacity()),
            String.valueOf(nomineeDetail.getLicenseeAcknowledgeOperatorRequirements())
        );
  }

  @Test
  void getForm_whenNoPreviousNomineeDetail_thenEmptyForm() {
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.empty());

    assertThat(nomineeDetailFormService.getForm(nominationDetail))
        .hasAllNullFieldsOrPropertiesExcept(
            "appendixDocuments"
        )
        .hasFieldOrPropertyWithValue("appendixDocuments", List.of());
  }

  @Test
  void validate_verifyMethodCall() {
    var form = NomineeDetailFormTestingUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailFormService.validate(form, bindingResult);

    verify(nomineeDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void nomineeDetailEntityToForm() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var nominatedOrganisationId = 123;
    var reasonForNomination = "reason";
    var plannedStartDate = LocalDate.of(2023, Month.APRIL, 1);
    var operatorHasCapacity = true;
    var operatorHasAuthority = true;
    var licenseeAcknowledgeOperatorRequirements = true;

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominatedOrganisationId(nominatedOrganisationId)
        .withReasonForNomination(reasonForNomination)
        .withPlannedStartDate(plannedStartDate)
        .withOperatorHasCapacity(operatorHasCapacity)
        .withOperatorHasAuthority(operatorHasAuthority)
        .withLicenseeAcknowledgeOperatorRequirements(licenseeAcknowledgeOperatorRequirements)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    var uploadedFile = UploadedFileTestUtil.builder().build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);
    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(Map.of(
        NomineeDetailAppendixFileController.PURPOSE, List.of(uploadedFileView)
    ));

    var fileUploadForm = new FileUploadForm();
    when(fileUploadService.getFileUploadFormsFromUploadedFileViews(List.of(uploadedFileView)))
        .thenReturn(List.of(fileUploadForm));

    var form = nomineeDetailFormService.getForm(nominationDetail);
    assertThat(form)
        .extracting(
            NomineeDetailForm::getNominatedOrganisationId,
            NomineeDetailForm::getReasonForNomination,
            NomineeDetailForm::getPlannedStartDay,
            NomineeDetailForm::getPlannedStartMonth,
            NomineeDetailForm::getPlannedStartYear,
            NomineeDetailForm::getOperatorHasCapacity,
            NomineeDetailForm::getOperatorHasAuthority,
            NomineeDetailForm::getLicenseeAcknowledgeOperatorRequirements,
            NomineeDetailForm::getAppendixDocuments
        )
        .containsExactly(
            nominatedOrganisationId,
            reasonForNomination,
            String.valueOf(plannedStartDate.getDayOfMonth()),
            String.valueOf(plannedStartDate.getMonthValue()),
            String.valueOf(plannedStartDate.getYear()),
            String.valueOf(operatorHasCapacity),
            String.valueOf(operatorHasAuthority),
            String.valueOf(licenseeAcknowledgeOperatorRequirements),
            List.of(fileUploadForm)
        );
  }

  private NomineeDetail getNomineeDetail() {
    return new NomineeDetail(
        nominationDetail,
        1,
        "reason",
        LocalDate.of(2022, 1, 1),
        true,
        true,
        true
    );
  }
}