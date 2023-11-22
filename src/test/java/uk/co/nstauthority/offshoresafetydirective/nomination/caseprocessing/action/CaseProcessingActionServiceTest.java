package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfigTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.file.UnlinkedFileController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.NominationConsultationRequestController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationRequestUpdateController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationStartUpdateController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private static final FileUploadConfig FILE_UPLOAD_CONFIG = FileUploadConfigTestUtil.builder()
      .withAllowedFileExtensions(Set.of(".txt", ".pdf", ".png"))
      .build();

  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of(".txt", ".pdf", ".png"))
      .build();

  @Mock
  private FileService fileService;

  private CaseProcessingActionService caseProcessingActionService;

  @BeforeEach
  void setUp() {
    caseProcessingActionService = new CaseProcessingActionService(FILE_UPLOAD_CONFIG, FILE_UPLOAD_PROPERTIES, fileService);
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
                on(NominationQaChecksController.class).submitQa(nominationId, true, CaseProcessingActionIdentifier.QA, null,
                    null, null)),
            Collections.emptyMap()
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
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true,
                    CaseProcessingActionIdentifier.WITHDRAW, null, null, null)),
            Collections.emptyMap()
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
                on(NominationDecisionController.class).submitDecision(nominationId, true,
                    CaseProcessingActionIdentifier.DECISION, null, null, null))
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
                FILE_UPLOAD_CONFIG.getMaxFileUploadBytes().toString(),
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
                FILE_UPLOAD_CONFIG.getMaxFileUploadBytes().toString(),
                String.join(",", FILE_UPLOAD_CONFIG.getDefaultPermittedFileExtensions())
            ))
        );
  }

  @Test
  void createGeneralCaseNoteAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());

    var maxBytes = 100;

    when(fileService.getFileUploadAttributes())
        .thenReturn(
            FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(DataSize.ofBytes(maxBytes))
                .withAllowedExtensions(FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions())
        );

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
            Map.entry("fileUploadTemplate", FileUploadComponentAttributes.newBuilder()
                .withMaximumSize(DataSize.ofBytes(maxBytes))
                .withAllowedExtensions(FILE_UPLOAD_PROPERTIES.defaultPermittedFileExtensions())
                .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null)))
                .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null)))
                .withUploadUrl(
                    ReverseRouter.route(on(UnlinkedFileController.class).upload(
                        null,
                        FileDocumentType.CASE_NOTE.name()
                    )))
                .build()
            ));
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
            Collections.emptyMap()
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
            Collections.emptyMap()
        );
  }

  @Test
  void createSendForConsultationAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createSendForConsultationAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.SEND_FOR_CONSULTATION,
            CaseProcessingActionGroup.CONSULTATIONS,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION),
            ReverseRouter.route(
                on(NominationConsultationRequestController.class).requestConsultation(nominationId, true,
                    CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION, null)),
            Collections.emptyMap()
        );

  }

  @Test
  void createConsultationResponseAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createConsultationResponseAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.CONSULTATION_RESPONSE,
            CaseProcessingActionGroup.CONSULTATIONS,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.CONSULTATION_RESPONSE),
            ReverseRouter.route(
                on(NominationConsultationResponseController.class).addConsultationResponse(nominationId, true,
                    CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)),
            Map.of("fileUploadTemplate", new FileUploadTemplate(
                ReverseRouter.route(on(NominationConsultationResponseFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(NominationConsultationResponseFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(NominationConsultationResponseFileController.class).delete(nominationId, null)),
                FILE_UPLOAD_CONFIG.getMaxFileUploadBytes().toString(),
                String.join(",", FILE_UPLOAD_CONFIG.getDefaultPermittedFileExtensions())
            ))
        );
  }

  @Test
  void createRequestNominationUpdateAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createRequestNominationUpdateAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.REQUEST_UPDATE,
            CaseProcessingActionGroup.REQUEST_UPDATE,
            new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.REQUEST_UPDATE),
            ReverseRouter.route(
                on(NominationRequestUpdateController.class).requestUpdate(nominationId, true,
                    CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)),
            Collections.emptyMap()
        );
  }

  @Test
  void createUpdateNominationAction() {
    var nominationId = new NominationId(NominationDetailTestUtil.builder().build());
    var result = caseProcessingActionService.createUpdateNominationAction(nominationId);

    assertThat(result)
        .extracting(
            CaseProcessingAction::getItem,
            CaseProcessingAction::getGroup,
            CaseProcessingAction::getCaseProcessingActionIdentifier,
            CaseProcessingAction::getSubmitUrl,
            CaseProcessingAction::getModelProperties
        ).containsExactly(
            CaseProcessingActionItem.UPDATE_NOMINATION,
            CaseProcessingActionGroup.UPDATE_NOMINATION,
            null,
            ReverseRouter.route(
                on(NominationStartUpdateController.class).startUpdateEntryPoint(nominationId)),
            Collections.emptyMap()
        );
  }
}