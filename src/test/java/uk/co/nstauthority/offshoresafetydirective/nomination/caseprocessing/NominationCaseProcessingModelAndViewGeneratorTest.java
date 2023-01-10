package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingModelAndViewGeneratorTest {

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private NominationCaseProcessingService nominationCaseProcessingService;

  @Mock
  private NominationSummaryService nominationSummaryService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private NominationCaseProcessingModelAndViewGenerator modelAndViewGenerator;

  private NominationDetail nominationDetail;
  private NominationId nominationId;
  private ServiceUserDetail userDetail;

  @BeforeEach
  void setUp() {

    nominationDetail = NominationDetailTestUtil.builder().build();
    nominationId = new NominationId(nominationDetail.getNomination().getId());
    userDetail = ServiceUserDetailTestUtil.Builder().build();

    when(nominationDetailService.getLatestNominationDetail(nominationId))
        .thenReturn(nominationDetail);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);
  }

  @Test
  void getCaseProcessingModelAndView_whenCannotManageNomination_thenAssertModelProperties() {

    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(false);

    var qaChecksForm = new NominationQaChecksForm();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationId, qaChecksForm);

    assertThat(result.getModel())
        .extracting(
            "headerInformation",
            "summaryView",
            NominationQaChecksController.FORM_NAME,
            "caseProcessingAction_QA",
            "canManageNomination"
        ).containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            false
        );

    assertThat(result.getModel())
        .doesNotContainKeys(
            "qaChecksSubmitUrl"
        );

    assertBreadcrumbs(result, nominationDetail);
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    var qaChecksForm = new NominationQaChecksForm();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationId, qaChecksForm);

    assertThat(result.getModel())
        .extracting(
            "headerInformation",
            "summaryView",
            NominationQaChecksController.FORM_NAME,
            "caseProcessingAction_QA",
            "canManageNomination",
            "qaChecksSubmitUrl"
        ).containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            true,
            ReverseRouter.route(on(NominationQaChecksController.class).submitQa(nominationId, null, null))
        );

    assertBreadcrumbs(result, nominationDetail);
  }

  private void assertBreadcrumbs(ModelAndView modelAndView, NominationDetail nominationDetail) {
    assertThat(modelAndView.getModel())
        .extractingByKeys(
            "breadcrumbsList",
            "currentPage"
        ).containsExactly(
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE
            ),
            nominationDetail.getNomination().getReference()
        );
  }

}