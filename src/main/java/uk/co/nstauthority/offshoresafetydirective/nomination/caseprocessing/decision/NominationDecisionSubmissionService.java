package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class NominationDecisionSubmissionService {

  private final CaseEventService caseEventService;
  private final FileUploadService fileUploadService;
  private final FileAssociationService fileAssociationService;
  private final NominationDetailService nominationDetailService;
  private final NominationDecisionDeterminedEventPublisher nominationDecisionDeterminedEventPublisher;

  @Autowired
  NominationDecisionSubmissionService(CaseEventService caseEventService,
                                      FileUploadService fileUploadService,
                                      FileAssociationService fileAssociationService,
                                      NominationDetailService nominationDetailService,
                                      NominationDecisionDeterminedEventPublisher nominationDecisionDeterminedEventPublisher) {
    this.caseEventService = caseEventService;
    this.fileUploadService = fileUploadService;
    this.fileAssociationService = fileAssociationService;
    this.nominationDetailService = nominationDetailService;
    this.nominationDecisionDeterminedEventPublisher = nominationDecisionDeterminedEventPublisher;
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

    fileAssociationService.submitFiles(nominationDecisionForm.getDecisionFiles());

    nominationDetailService.updateNominationDetailStatusByDecision(
        nominationDetail,
        EnumUtils.getEnum(NominationDecision.class, nominationDecisionForm.getNominationDecision())
    );

    nominationDecisionDeterminedEventPublisher.publish(new NominationId(nominationDetail));
  }

}
