package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;


@WebMvcTest
@ContextConfiguration(
    classes = {
        ApplicantDetailController.class,
    }
)
@WithMockUser
class ApplicantDetailControllerTest extends AbstractControllerTest {

  private final NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private ApplicantDetailService applicantDetailService;

  @MockBean
  private NominationService nominationService;

  @Test
  void getApplicantDetails_assertStatusOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(ApplicantDetailController.class).getApplicantDetails()))
    )
        .andExpect(status().isOk());

    verify(applicantDetailService, times(1)).getApplicantDetailsModelAndView(any());
  }

  @Test
  void saveApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("REF#1");
    var applicationDetail = new ApplicantDetail(1);
    applicationDetail.setPortalOrganisationId(form.getPortalOrganisationId());
    applicationDetail.setApplicantReference(form.getApplicantReference());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationService.startNomination()).thenReturn(nominationDetail);
    when(applicantDetailService.createApplicantDetail(any(), eq(nominationDetail))).thenReturn(applicationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).saveApplicantDetails(form, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(nominationService, times(1)).startNomination();
    verify(applicantDetailService, times(1)).createApplicantDetail(any(), eq(nominationDetail));
  }

  @Test
  void saveApplicantDetails_whenInvalidForm_assertOk() throws Exception {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(applicantDetailService.getApplicantDetailsModelAndView(any())).thenReturn(new ModelAndView());

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).saveApplicantDetails(form, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominationService, never()).startNomination();
    verify(applicantDetailService, never()).createApplicantDetail(any(), any());

  }
}