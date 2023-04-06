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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
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
class NominationManagementInteractableServiceTest {

  private final FileUploadConfig fileUploadConfig = FileUploadConfigTestUtil.builder()
      .withAllowedFileExtensions(List.of(".txt", ".pdf", ".png"))
      .build();
  private NominationManagementInteractableService nominationManagementInteractableService;

  @BeforeEach
  void setUp() {
    nominationManagementInteractableService = new NominationManagementInteractableService(fileUploadConfig);
  }

  @Test
  void createQaChecksInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createQaChecksInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl,
            NominationManagementInteractable::getModelProperties
        ).containsExactly(
            NominationManagementItem.QA_CHECKS,
            NominationManagementGroup.COMPLETE_QA_CHECKS,
            new CaseProcessingAction(CaseProcessingAction.QA),
            ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(nominationId, true, CaseProcessingAction.QA, null, null,
                    null)),
            Map.of()
        );
  }

  @Test
  void createWithdrawInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createWithdrawInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl,
            NominationManagementInteractable::getModelProperties
        ).containsExactly(
            NominationManagementItem.WITHDRAW,
            NominationManagementGroup.DECISION,
            new CaseProcessingAction(CaseProcessingAction.WITHDRAW),
            ReverseRouter.route(
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true, CaseProcessingAction.WITHDRAW,
                    null, null, null)),
            Map.of()
        );
  }

  @Test
  void createNominationDecisionInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createNominationDecisionInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl
        ).containsExactly(
            NominationManagementItem.NOMINATION_DECISION,
            NominationManagementGroup.DECISION,
            new CaseProcessingAction(CaseProcessingAction.DECISION),
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(nominationId, true, CaseProcessingAction.DECISION,
                    null, null, null))
        );

    assertThat(result)
        .extracting(NominationManagementInteractable::getModelProperties)
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
  void createConfirmNominationAppointmentInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createConfirmNominationAppointmentInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl
        ).containsExactly(
            NominationManagementItem.CONFIRM_APPOINTMENT,
            NominationManagementGroup.CONFIRM_APPOINTMENT,
            new CaseProcessingAction(CaseProcessingAction.CONFIRM_APPOINTMENT),
            ReverseRouter.route(
                on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true,
                    CaseProcessingAction.CONFIRM_APPOINTMENT, null, null, null))
        );

    assertThat(result)
        .extracting(NominationManagementInteractable::getModelProperties)
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
  void createGeneralCaseNoteInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createGeneralCaseNoteInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl
        ).containsExactly(
            NominationManagementItem.GENERAL_CASE_NOTE,
            NominationManagementGroup.ADD_CASE_NOTE,
            new CaseProcessingAction(CaseProcessingAction.GENERAL_NOTE),
            ReverseRouter.route(
                on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true,
                    CaseProcessingAction.GENERAL_NOTE, null, null, null))
        );

    assertThat(result)
        .extracting(NominationManagementInteractable::getModelProperties)
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
  void createPearsReferencesInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createPearsReferencesInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl,
            NominationManagementInteractable::getModelProperties
        ).containsExactly(
            NominationManagementItem.PEARS_REFERENCE,
            NominationManagementGroup.RELATED_APPLICATIONS,
            new CaseProcessingAction(CaseProcessingAction.PEARS_REFERENCES),
            ReverseRouter.route(
                on(NominationPortalReferenceController.class).updatePearsReferences(nominationId, true,
                    CaseProcessingAction.PEARS_REFERENCES, null, null, null)),
            Map.of()
        );
  }

  @Test
  void createWonsReferencesInteractable() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = nominationManagementInteractableService.createWonsReferencesInteractable(nominationId);

    assertThat(result)
        .extracting(
            NominationManagementInteractable::getItem,
            NominationManagementInteractable::getGroup,
            NominationManagementInteractable::getCaseProcessingAction,
            NominationManagementInteractable::getSubmitUrl,
            NominationManagementInteractable::getModelProperties
        ).containsExactly(
            NominationManagementItem.WONS_REFERENCE,
            NominationManagementGroup.RELATED_APPLICATIONS,
            new CaseProcessingAction(CaseProcessingAction.WONS_REFERENCES),
            ReverseRouter.route(
                on(NominationPortalReferenceController.class).updateWonsReferences(nominationId, true,
                    CaseProcessingAction.WONS_REFERENCES, null, null, null)),
            Map.of()
        );
  }
}