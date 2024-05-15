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
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@ExtendWith(MockitoExtension.class)
class NominationConsultationResponseSubmissionServiceTest {

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
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setUploadedFileId(UUID.randomUUID());

    form.setConsultationResponseFiles(List.of(uploadedFileForm));

    nominationConsultationResponseSubmissionService.submitConsultationResponse(nominationDetail, form);

    verify(caseEventService).createConsultationResponseEvent(nominationDetail, formResponse, List.of(uploadedFileForm));
    verifyNoMoreInteractions(caseEventService);
  }
}