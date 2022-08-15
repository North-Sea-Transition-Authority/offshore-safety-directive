package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@WebMvcTest
@ContextConfiguration(classes = NominatedInstallationController.class)
@WithMockUser
class NominatedInstallationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(1);
  private static final NominationDetail NOMINATION_DETAIL =  new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @MockBean
  private NominatedInstallationDetailService nominatedInstallationDetailService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private InstallationQueryService installationQueryService;

  @Test
  void getNominatedInstallationDetail_assertModelProperties() throws Exception {
    var installationDto1 = new InstallationDto(1, "installation1");
    var installationDto2 = new InstallationDto(2, "installation2");
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(installationDto1.id(), installationDto2.id()))
        .build();
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedInstallationDetailService.getForm(NOMINATION_DETAIL)).thenReturn(form);
    when(installationQueryService.getInstallationsByIdIn(List.of(installationDto1.id(), installationDto2.id())))
        .thenReturn(List.of(installationDto2, installationDto1));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var model = modelAndView.getModel();

    assertThat(model).containsOnlyKeys(
        "form",
        "pageTitle",
        "backLinkUrl",
        "actionUrl",
        "installationPhases",
        "alreadyAddedInstallations",
        "installationsRestUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form"
    );
    var expectedBackLinkUrl =  ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID));
    var expectedActionUrl =
        ReverseRouter.route(on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null));
    var expectedInstallationsRestUrl =  RestApiUtil.route(on(InstallationRestController.class).searchInstallationsByName(null));
    var expectedInstallationView1 = new InstallationAddToListView(installationDto1.id(), installationDto1.name(), true);
    var expectedInstallationView2 = new InstallationAddToListView(installationDto2.id(), installationDto2.name(), true);
    assertEquals(NominatedInstallationDetailForm.class, model.get("form").getClass());
    assertEquals(NominatedInstallationController.PAGE_TITLE, model.get("pageTitle"));
    assertEquals(expectedBackLinkUrl, model.get("backLinkUrl"));
    assertEquals(expectedActionUrl, model.get("actionUrl"));
    assertEquals(DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class), model.get("installationPhases"));
    assertEquals(expectedInstallationsRestUrl, model.get("installationsRestUrl"));

    @SuppressWarnings("unchecked")
    var expectedInstallationsViews = (List<InstallationAddToListView>) model.get("alreadyAddedInstallations");
    assertThat(expectedInstallationsViews).containsExactly(
        expectedInstallationView1,
        expectedInstallationView2
    );
  }

  @Test
  void saveNominatedInstallationDetail_whenNoErrors_verifyServiceMethodCall() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominatedInstallationDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageInstallationsController.class)
            .getManageInstallations(NOMINATION_ID))));

    verify(nominatedInstallationDetailService, times(1))
        .createOrUpdateNominatedInstallationDetail(eq(NOMINATION_DETAIL), any(NominatedInstallationDetailForm.class));
  }

  @Test
  void saveNominatedInstallationDetail_whenErrors_assertStatusOk() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(nominatedInstallationDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"));

    verify(nominatedInstallationDetailService, never()).createOrUpdateNominatedInstallationDetail(any(), any());
  }
}