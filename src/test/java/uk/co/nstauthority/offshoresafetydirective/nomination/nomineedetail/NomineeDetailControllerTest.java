package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@WebMvcTest
@ContextConfiguration(classes = NomineeDetailController.class)
@WithMockUser
class NomineeDetailControllerTest extends AbstractControllerTest {

  private final Integer nominationId = 1;
  private final NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();
  private final NomineeDetailForm form = new NomineeDetailForm();

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private NomineeDetailService nomineeDetailService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @BeforeEach
  void setup() {
    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(nomineeDetailService.getForm(nominationDetail)).thenReturn(form);
    when(portalOrganisationUnitQueryService.getOrganisationById(any())).thenReturn(Optional.empty());
  }

  @Test
  void getNomineeDetail_assertModelProperties() throws Exception {
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertEquals("osd/nomination/nomineeDetails/nomineeDetail", modelAndView.getViewName());
    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "form",
        "pageTitle",
        "portalOrganisationsRestUrl",
        "preselectedItems",
        "actionUrl",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "breadcrumbsList",
        "currentPage",
        "navigationItems",
        "currentEndPoint"
    );

    var model = modelAndView.getModel();
    var expectedPortalOrganisationsRestUrl =
        RestApiUtil.route(
            on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null));
    var expectedActionUrl =
        ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null));

    assertEquals(NomineeDetailController.PAGE_NAME, model.get("pageTitle"));
    assertEquals(expectedPortalOrganisationsRestUrl, model.get("portalOrganisationsRestUrl"));
    assertEquals(expectedActionUrl, model.get("actionUrl"));
  }

  @Test
  void saveNomineeDetail_whenValidForm_verifyMethodCalls() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection());

    verify(nomineeDetailService, times(1)).validate(any(), any());
    verify(nominationDetailService, times(1)).getLatestNominationDetail(nominationId);
    verify(nomineeDetailService, times(1)).createOrUpdateNomineeDetail(eq(nominationDetail), any());
  }

  @Test
  void saveNomineeDetail_whenInvalidForm_verifyStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailService.validate(any(), any())).thenReturn(bindingResult);
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nomineeDetailService, times(1)).validate(any(), any());
    verify(nomineeDetailService, never()).createOrUpdateNomineeDetail(any(), any());
  }
}