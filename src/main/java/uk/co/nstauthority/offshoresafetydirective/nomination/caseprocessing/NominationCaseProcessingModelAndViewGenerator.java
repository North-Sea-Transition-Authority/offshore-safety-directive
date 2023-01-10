package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class NominationCaseProcessingModelAndViewGenerator {

  private final NominationDetailService nominationDetailService;
  private final NominationCaseProcessingService nominationCaseProcessingService;
  private final NominationSummaryService nominationSummaryService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;

  @Autowired
  public NominationCaseProcessingModelAndViewGenerator(NominationDetailService nominationDetailService,
                                                       NominationCaseProcessingService nominationCaseProcessingService,
                                                       NominationSummaryService nominationSummaryService,
                                                       PermissionService permissionService,
                                                       UserDetailService userDetailService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationCaseProcessingService = nominationCaseProcessingService;
    this.nominationSummaryService = nominationSummaryService;
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
  }

  public ModelAndView getCaseProcessingModelAndView(NominationId nominationId,
                                                    NominationQaChecksForm nominationQaChecksForm) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var headerInformation = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Unable to find %s for nomination with ID: [%d]".formatted(
                NominationCaseProcessingHeader.class.getSimpleName(),
                nominationId.id()
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
        .addObject("caseProcessingAction_QA", CaseProcessingAction.QA);

    addRelevantDropdownActions(modelAndView, nominationId);

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }

  private void addRelevantDropdownActions(ModelAndView modelAndView, NominationId nominationId) {
    var canManageNomination = false;

    if (permissionService.hasPermission(userDetailService.getUserDetail(), Set.of(RolePermission.MANAGE_NOMINATIONS))) {
      canManageNomination = true;
      modelAndView
          .addObject("qaChecksSubmitUrl",
              ReverseRouter.route(on(NominationQaChecksController.class).submitQa(nominationId, null, null)));
    }

    modelAndView.addObject("canManageNomination", canManageNomination);
  }

}
