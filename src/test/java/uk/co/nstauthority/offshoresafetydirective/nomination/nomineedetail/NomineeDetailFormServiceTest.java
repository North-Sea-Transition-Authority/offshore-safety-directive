package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
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
  private FileService fileService;

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
        .withNominationDetail(nominationDetail)
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

    when(fileService.findAll(
        nominationDetail.getId().toString(),
        FileUsageType.NOMINATION_DETAIL.getUsageType(),
        FileDocumentType.APPENDIX_C.name()
    ))
        .thenReturn(List.of(uploadedFile));

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
            NomineeDetailForm::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            nominatedOrganisationId,
            reasonForNomination,
            String.valueOf(plannedStartDate.getDayOfMonth()),
            String.valueOf(plannedStartDate.getMonthValue()),
            String.valueOf(plannedStartDate.getYear()),
            String.valueOf(operatorHasCapacity),
            String.valueOf(operatorHasAuthority),
            String.valueOf(licenseeAcknowledgeOperatorRequirements)
        );

    assertThat(form)
        .extracting(NomineeDetailForm::getAppendixDocuments)
        .asInstanceOf(InstanceOfAssertFactories.list(UploadedFileForm.class))
        .extracting(
            UploadedFileForm::getFileId,
            UploadedFileForm::getFileName,
            UploadedFileForm::getFileDescription,
            UploadedFileForm::getFileSize,
            UploadedFileForm::getFileUploadedAt
        )
        .containsExactly(
            Tuple.tuple(
                uploadedFile.getId(),
                uploadedFile.getName(),
                uploadedFile.getDescription(),
                FileUploadLibraryUtils.formatSize(uploadedFile.getContentLength()),
                uploadedFile.getUploadedAt()
            )
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