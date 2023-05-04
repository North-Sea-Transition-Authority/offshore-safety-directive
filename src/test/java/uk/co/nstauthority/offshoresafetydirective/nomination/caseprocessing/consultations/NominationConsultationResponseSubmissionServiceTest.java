package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;

@ExtendWith(MockitoExtension.class)
class NominationConsultationResponseSubmissionServiceTest {

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private CaseEventService caseEventService;

  @InjectMocks
  private NominationConsultationResponseSubmissionService nominationConsultationResponseSubmissionService;

  @Test
  void submitConsultationResponse() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var form = new NominationConsultationResponseForm();
    var formResponse = "response";
    form.getResponse().setInputValue(formResponse);
    var uploadForm = new FileUploadForm();
    uploadForm.setUploadedFileId(UUID.randomUUID());

    form.setConsultationResponseFiles(List.of(uploadForm));

    nominationConsultationResponseSubmissionService.submitConsultationResponse(nominationDetail, form);

    verify(fileUploadService).updateFileUploadDescriptions(List.of(uploadForm));
    verify(fileAssociationService).submitFiles(List.of(uploadForm));
    verify(caseEventService).createConsultationResponseEvent(nominationDetail, formResponse, List.of(uploadForm));
    verifyNoMoreInteractions(fileUploadService, fileAssociationService, caseEventService);
  }
}