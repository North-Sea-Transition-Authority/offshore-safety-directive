package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeAccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.LicenceAddToListView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@IncludeAccidentRegulatorConfigurationProperties
@ContextConfiguration(classes = NominatedInstallationController.class)
class NominatedInstallationControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockBean
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @MockBean
  private NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @MockBean
  private InstallationQueryService installationQueryService;

  @MockBean
  private LicenceQueryService licenceQueryService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void getNominatedInstallationDetail_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominatedInstallationController.class)
        .getNominatedInstallationDetail(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getNominatedInstallationDetail_whenNotInApplicantGroupTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominatedInstallationController.class)
        .getNominatedInstallationDetail(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void saveNominatedInstallationDetail_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominatedInstallationController.class)
        .saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveNominatedInstallationDetail_whenNotInApplicantGroupTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedInstallationController.class)
        .saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .build();

    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getNominatedInstallationDetail_assertModelProperties() throws Exception {

    var firstInstallationAlphabeticallyByName = InstallationDtoTestUtil.builder()
        .withId(1)
        .withName("A installation")
        .build();

    var lastInstallationAlphabeticallyByName = InstallationDtoTestUtil.builder()
        .withId(2)
        .withName("B installation")
        .build();

    var firstLicence = LicenceDtoTestUtil.builder()
        .withLicenceId(1)
        .withLicenceReference("reference 1")
        .build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(
            String.valueOf(firstInstallationAlphabeticallyByName.id()),
            String.valueOf(lastInstallationAlphabeticallyByName.id())))
        .withLicence(firstLicence.licenceId().id())
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    when(installationQueryService.getInstallationsByIdIn(
        List.of(firstInstallationAlphabeticallyByName.id(), lastInstallationAlphabeticallyByName.id()),
        NominatedInstallationController.ALREADY_ADDED_INSTALLATIONS_PURPOSE
    ))
        .thenReturn(List.of(lastInstallationAlphabeticallyByName, firstInstallationAlphabeticallyByName));

    when(licenceQueryService.getLicencesByIdIn(List.of(firstLicence.licenceId().id()),
        NominatedInstallationController.ALREADY_ADDED_LICENCES_PURPOSE))
        .thenReturn(List.of(firstLicence));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominatedInstallationController.class)
        .getNominatedInstallationDetail(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"))
        .andExpect(model().attribute("pageTitle", NominatedInstallationController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null))
        ))
        .andExpect(model().attribute(
            "installationPhases",
            DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class)
        ))
        .andExpect(model().attribute(
            "alreadyAddedInstallations",
            List.of(
                new InstallationAddToListView(firstInstallationAlphabeticallyByName),
                new InstallationAddToListView(lastInstallationAlphabeticallyByName)
            )
        ))
        .andExpect(model().attribute(
            "installationsRestUrl",
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, NominatedInstallationController.PERMITTED_INSTALLATION_TYPES))
        ))
        .andExpect(model().attribute(
            "alreadyAddedLicences",
            List.of(new LicenceAddToListView(firstLicence))
        ))
        .andExpect(model().attribute("licencesRestUrl", RestApiUtil.route(on(LicenceRestController.class)
            .searchLicences(null))))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attributeExists("accidentRegulatorBranding"))
        .andExpect(model().attributeExists("org.springframework.validation.BindingResult.accidentRegulatorBranding"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(
        (AccidentRegulatorConfigurationProperties) modelAndView.getModel().get("accidentRegulatorBranding")
    )
        .hasNoNullFieldsOrProperties();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getNominatedInstallationDetail_whenAlreadyAddedLicencesNull_assertEmptyList(List<String> nullOrEmptyList)
      throws Exception {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withLicences(nullOrEmptyList)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    verify(licenceQueryService, never()).getLicencesByIdIn(any(), any());

    mockMvc.perform(get(ReverseRouter.route(on(NominatedInstallationController.class)
        .getNominatedInstallationDetail(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(model().attribute("alreadyAddedLicences", Collections.emptyList()));
  }

  @Test
  void getNominatedInstallationDetail_whenAlreadyAddedLicencesEmpty_assertEmptyList() throws Exception {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withLicences(Collections.emptyList())
        .build();

    var firstLicence = LicenceDtoTestUtil.builder()
        .withLicenceId(1)
        .withLicenceReference("reference 1")
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    when(licenceQueryService.getLicencesByIdIn(
        List.of(firstLicence.licenceId().id()),
        NominatedInstallationController.ALREADY_ADDED_LICENCES_PURPOSE
    ))
        .thenReturn(List.of(firstLicence));

    mockMvc.perform(get(ReverseRouter.route(on(NominatedInstallationController.class)
        .getNominatedInstallationDetail(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(model().attribute("alreadyAddedLicences", Collections.emptyList()));
  }

  @Test
  void saveNominatedInstallationDetail_whenNoErrors_verifyServiceMethodCall() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedInstallationController.class)
        .saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageInstallationsController.class)
            .getManageInstallations(NOMINATION_ID))));

    verify(nominatedInstallationDetailPersistenceService, times(1))
        .createOrUpdateNominatedInstallationDetail(eq(nominationDetail), any(NominatedInstallationDetailForm.class));
  }

  @Test
  void saveNominatedInstallationDetail_whenErrors_assertStatusOk() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedInstallationController.class)
        .saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"));

    verify(nominatedInstallationDetailPersistenceService, never())
        .createOrUpdateNominatedInstallationDetail(any(), any());
  }
}