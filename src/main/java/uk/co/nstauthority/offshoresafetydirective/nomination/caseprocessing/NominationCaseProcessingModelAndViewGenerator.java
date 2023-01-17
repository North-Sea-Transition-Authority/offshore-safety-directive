package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class NominationCaseProcessingModelAndViewGenerator {

  private final NominationCaseProcessingService nominationCaseProcessingService;
  private final NominationSummaryService nominationSummaryService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;

  @Autowired
  public NominationCaseProcessingModelAndViewGenerator(NominationCaseProcessingService nominationCaseProcessingService,
                                                       NominationSummaryService nominationSummaryService,
                                                       PermissionService permissionService,
                                                       UserDetailService userDetailService) {
    this.nominationCaseProcessingService = nominationCaseProcessingService;
    this.nominationSummaryService = nominationSummaryService;
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
  }

  public ModelAndView getCaseProcessingModelAndView(NominationDetail nominationDetail,
                                                    NominationQaChecksForm nominationQaChecksForm,
                                                    NominationDecisionForm nominationDecisionForm,
                                                    WithdrawNominationForm withdrawNominationForm) {

    var headerInformation = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Unable to find %s for nomination with ID: [%d]".formatted(
                NominationCaseProcessingHeader.class.getSimpleName(),
                nominationDetail.getNomination().getId()
            )
        ));

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(nominationDetail.getNomination().getReference())
        .addWorkAreaBreadcrumb()
        .build();

    var modelAndView = new ModelAndView("osd/nomination/caseProcessing/caseProcessing")
        .addObject("headerInformation", headerInformation)
        .addObject("summaryView", nominationSummaryService.getNominationSummaryView(
            nominationDetail,
            SummaryValidationBehaviour.NOT_VALIDATED
        ))
        .addObject(NominationQaChecksController.FORM_NAME, nominationQaChecksForm)
        .addObject("caseProcessingAction_QA", CaseProcessingAction.QA)
        .addObject(NominationDecisionController.FORM_NAME, nominationDecisionForm)
        .addObject("caseProcessingAction_DECISION", CaseProcessingAction.DECISION)
        .addObject("nominationDecisions", Arrays.stream(NominationDecision.values())
            .sorted(Comparator.comparing(NominationDecision::getDisplayOrder))
            .collect(Collectors.toList())
        )
        .addObject("nominationDecisions", NominationDecision.values())
        .addObject(WithdrawNominationController.FORM_NAME, withdrawNominationForm)
        .addObject("caseProcessingAction_WITHDRAW", CaseProcessingAction.WITHDRAW);

    addRelevantDropdownActions(modelAndView, nominationDetail);

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }

  private void addRelevantDropdownActions(ModelAndView modelAndView, NominationDetail nominationDetail) {
    var canManageNomination = false;
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    if (permissionService.hasPermission(userDetailService.getUserDetail(), Set.of(RolePermission.MANAGE_NOMINATIONS))) {

      if (nominationDetailDto.nominationStatus() == NominationStatus.SUBMITTED) {
        modelAndView
            .addObject("qaChecksSubmitUrl",
                ReverseRouter.route(
                    on(NominationQaChecksController.class).submitQa(nominationId, CaseProcessingAction.QA, null, null)))
            .addObject("decisionSubmitUrl",
                ReverseRouter.route(
                    on(NominationDecisionController.class).submitDecision(nominationId, true,
                        CaseProcessingAction.DECISION, null, null, null)))
            .addObject("withdrawSubmitUrl",
                ReverseRouter.route(
                    on(WithdrawNominationController.class).withdrawNomination(nominationId, true, null, null, null, null)
                ));

        canManageNomination = true;
      }
    }

    modelAndView.addObject("canManageNomination", canManageNomination);
  }

}
