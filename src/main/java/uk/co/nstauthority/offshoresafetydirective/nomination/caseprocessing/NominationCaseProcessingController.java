package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationCaseProcessingController {

  private final NominationDetailService nominationDetailService;
  private final NominationCaseProcessingService nominationCaseProcessingService;
  private final NominationSummaryService nominationSummaryService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;

  @Autowired
  public NominationCaseProcessingController(
      NominationDetailService nominationDetailService,
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

  @GetMapping
  public ModelAndView renderCaseProcessing(@PathVariable("nominationId") NominationId nominationId,
                                           @ModelAttribute("qaChecksForm") NominationQaChecksForm nominationQaChecksForm) {
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
        ));

    var canManageNomination = false;

    if (permissionService.hasPermission(userDetailService.getUserDetail(), Set.of(RolePermission.MANAGE_NOMINATIONS))) {
      canManageNomination = true;
      modelAndView
          .addObject("qaChecksSubmitUrl",
              ReverseRouter.route(on(NominationQaChecksController.class).submitQa(nominationId, null, null)));
    }

    modelAndView.addObject("canManageNomination", canManageNomination);

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }

}
