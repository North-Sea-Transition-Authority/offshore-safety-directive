package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfigTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private final FileUploadConfig fileUploadConfig = FileUploadConfigTestUtil.builder()
      .withAllowedFileExtensions(List.of(".txt", ".pdf", ".png"))
      .build();
  private CaseProcessingActionService caseProcessingActionService;

  @BeforeEach
  void setUp() {
    caseProcessingActionService = new CaseProcessingActionService(fileUploadConfig);
  }

  @Test
  void createQaChecksAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createQaChecksAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.QA_CHECKS,
            CaseProcessingActionGroup.COMPLETE_QA_CHECKS,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.QA),
            ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(nominationId, true, CaseProcessingActionIdentifier.QA, null, null,
                    null)),
            Map.of()
        );
  }

  @Test
  void createWithdrawAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createWithdrawAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.WITHDRAW,
            CaseProcessingActionGroup.DECISION,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.WITHDRAW),
            ReverseRouter.route(
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true, CaseProcessingActionIdentifier.WITHDRAW,
                    null, null, null)),
            Map.of()
        );
  }

  @Test
  void createNominationDecisionAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createNominationDecisionAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl
        ).containsExactly(
            CaseProcessingActionItem.NOMINATION_DECISION,
            CaseProcessingActionGroup.DECISION,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.DECISION),
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(nominationId, true, CaseProcessingActionIdentifier.DECISION,
                    null, null, null))
        );

    assertThat(result)
        .extracting(CaseProcessingAction::getModelProperties)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
        .containsExactly(
            Map.entry("decisionOptions", List.of(NominationDecision.NO_OBJECTION, NominationDecision.OBJECTION)),
            Map.entry("fileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(on(NominationDecisionFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(NominationDecisionFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(NominationDecisionFileController.class).delete(nominationId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                ".pdf"
            ))
        );
  }

  @Test
  void createConfirmNominationAppointmentAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createConfirmNominationAppointmentAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl
        ).containsExactly(
            CaseProcessingActionItem.CONFIRM_APPOINTMENT,
            CaseProcessingActionGroup.CONFIRM_APPOINTMENT,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT),
            ReverseRouter.route(
                on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true,
                    CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null))
        );

    assertThat(result)
        .extracting(CaseProcessingAction::getModelProperties)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
        .containsExactly(
            Map.entry("fileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).delete(nominationId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            ))
        );
  }

  @Test
  void createGeneralCaseNoteAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createGeneralCaseNoteAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl
        ).containsExactly(
            CaseProcessingActionItem.GENERAL_CASE_NOTE,
            CaseProcessingActionGroup.ADD_CASE_NOTE,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.GENERAL_NOTE),
            ReverseRouter.route(
                on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true,
                    CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null))
        );

    assertThat(result)
        .extracting(CaseProcessingAction::getModelProperties)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
        .containsExactly(
            Map.entry("fileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).delete(nominationId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            ))
        );
  }

  @Test
  void createPearsReferencesAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createPearsReferencesAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.PEARS_REFERENCE,
            CaseProcessingActionGroup.RELATED_APPLICATIONS,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.PEARS_REFERENCES),
            ReverseRouter.route(
                on(NominationPortalReferenceController.class).updatePearsReferences(nominationId, true,
                    CaseProcessingActionIdentifier.PEARS_REFERENCES, null, null, null)),
            Map.of()
        );
  }

  @Test
  void createWonsReferencesAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createWonsReferencesAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.WONS_REFERENCE,
            CaseProcessingActionGroup.RELATED_APPLICATIONS,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.WONS_REFERENCES),
            ReverseRouter.route(
                on(NominationPortalReferenceController.class).updateWonsReferences(nominationId, true,
                    CaseProcessingActionIdentifier.WONS_REFERENCES, null, null, null)),
            Map.of()
        );
  }
}