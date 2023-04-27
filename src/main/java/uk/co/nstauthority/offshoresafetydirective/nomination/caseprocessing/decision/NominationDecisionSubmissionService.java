package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationDetailFileService;

@Service
class NominationDecisionSubmissionService {

  private final CaseEventService caseEventService;
  private final FileUploadService fileUploadService;
  private final NominationDetailFileService nominationDetailFileService;
  private final NominationDetailService nominationDetailService;

  public static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.NOMINATION_DECISION;

  @Autowired
  NominationDecisionSubmissionService(CaseEventService caseEventService,
                                      FileUploadService fileUploadService,
                                      NominationDetailFileService nominationDetailFileService,
                                      NominationDetailService nominationDetailService) {
    this.caseEventService = caseEventService;
    this.fileUploadService = fileUploadService;
    this.nominationDetailFileService = nominationDetailFileService;
    this.nominationDetailService = nominationDetailService;
  }

  @Transactional
  public void submitNominationDecision(NominationDetail nominationDetail,
                                       NominationDecisionForm nominationDecisionForm) {

    caseEventService.createDecisionEvent(
        nominationDetail,
        nominationDecisionForm.getDecisionDate().getAsLocalDate()
            .orElseThrow(() -> new IllegalStateException("Decision date is null and passed validation")),
        nominationDecisionForm.getComments().getInputValue(),
        EnumUtils.getEnum(NominationDecision.class, nominationDecisionForm.getNominationDecision()),
        nominationDecisionForm.getDecisionFiles()
    );

    fileUploadService.updateFileUploadDescriptions(nominationDecisionForm.getDecisionFiles());

    nominationDetailFileService.submitAndCleanFiles(nominationDetail, nominationDecisionForm.getDecisionFiles(),
        VIRTUAL_FOLDER);

    nominationDetailService.updateNominationDetailStatusByDecision(
        nominationDetail,
        EnumUtils.getEnum(NominationDecision.class, nominationDecisionForm.getNominationDecision())
    );
  }

}
