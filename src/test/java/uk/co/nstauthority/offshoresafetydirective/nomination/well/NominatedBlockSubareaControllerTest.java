package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeAccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellboreController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@ContextConfiguration(classes = NominatedBlockSubareaController.class)
@IncludeAccidentRegulatorConfigurationProperties
class NominatedBlockSubareaControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockitoBean
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @MockitoBean
  NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @MockitoBean
  private NominatedBlockSubareaFormService nominatedBlockSubareaFormService;

  @MockitoBean
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

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
  void getLicenceBlockSubareas_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveLicenceBlockSubareas_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getLicenceBlockSubareas_whenNotMemberOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void saveLicenceBlockSubareas_whenNotMemberOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .build();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedBlockSubareaFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedBlockSubareaController.class)
                .saveLicenceBlockSubareas(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getLicenceBlockSubareas_assertModelAndViewProperties() throws Exception {

    var form = new NominatedBlockSubareaForm();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(form);

    mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/blockSubarea"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("alreadyAddedSubareas", Collections.emptyList()))
        .andExpectAll(
            getLicenceBlockSubareasBaseModelAndViewAttrMatchers().toArray(new ResultMatcher[0])
        );
  }

  @Test
  void getLicenceBlockSubareas_whenExistingSubareas_assertModelAndViewProperties() throws Exception {

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("subarea-id")
        .build();

    var form = NominatedBlockSubareaFormTestUtil.builder()
        .withSubareas(List.of(expectedSubarea.subareaId().id()))
        .build();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(form);

    var subareaIds = form.getSubareas()
        .stream()
        .map(LicenceBlockSubareaId::new)
        .toList();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        subareaIds,
        NominatedBlockSubareaController.ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE
    ))
        .thenReturn(List.of(expectedSubarea));

    var nominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(expectedSubarea.subareaId())
        .build();

    when(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .thenReturn(List.of(nominatedBlockSubareaDto));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/blockSubarea"))
        .andExpectAll(getLicenceBlockSubareasBaseModelAndViewAttrMatchers().toArray(new ResultMatcher[0]))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attributeExists("alreadyAddedSubareas"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    @SuppressWarnings("unchecked")
    var alreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");

    assertThat(alreadyAddedSubareas)
        .extracting(
            LicenceBlockSubareaAddToListView::getId,
            LicenceBlockSubareaAddToListView::getName,
            LicenceBlockSubareaAddToListView::isValid
        )
        .containsExactly(
            tuple(
                expectedSubarea.subareaId().id(),
                expectedSubarea.displayName(),
                true
            )
        );
  }

  private List<ResultMatcher> getLicenceBlockSubareasBaseModelAndViewAttrMatchers() {
    return List.of(
        model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))
        ),
        model().attribute(
            "blockSubareaRestUrl",
            RestApiUtil.route(on(LicenceBlockSubareaRestController.class).searchSubareas(null))
        ),
        model().attribute("pageTitle", NominatedBlockSubareaController.PAGE_TITLE),
        model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
        ),
        model().attributeHasNoErrors("accidentRegulatorBranding"),
        model().attributeExists("org.springframework.validation.BindingResult.accidentRegulatorBranding"));
  }

  @Test
  void getLicenceBlockSubareas_whenMultipleSubareaWithSameBlockAndName_thenSortedByLicenceComponents() throws Exception {

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .withSubareaId("first")
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .withSubareaId("second")
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withSubareaId("third")
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .withSubareaId("fourth")
        .build();

    var formWithSubareas = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(
            List.of(
                firstSubareaByLicence.subareaId().id(),
                thirdSubareaByLicence.subareaId().id(),
                secondSubareaByLicence.subareaId().id(),
                fourthSubareaByLicence.subareaId().id()
            )
        )
        .build();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(formWithSubareas);

    var unsortedSubareaList = List.of(
        thirdSubareaByLicence,
        firstSubareaByLicence,
        fourthSubareaByLicence,
        secondSubareaByLicence
    );

    var subareaIds = formWithSubareas.getSubareas()
            .stream()
            .map(LicenceBlockSubareaId::new)
            .toList();

    var firstNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstSubareaByLicence.subareaId())
        .build();
    var secondNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(secondSubareaByLicence.subareaId())
        .build();
    var thirdNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(thirdSubareaByLicence.subareaId())
        .build();
    var fourthNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(fourthSubareaByLicence.subareaId())
        .build();

    when(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .thenReturn(List.of(
            firstNominatedBlockSubareaDto,
            secondNominatedBlockSubareaDto,
            thirdNominatedBlockSubareaDto,
            fourthNominatedBlockSubareaDto
        ));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        subareaIds,
        NominatedBlockSubareaController.ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE
    ))
        .thenReturn(unsortedSubareaList);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("alreadyAddedSubareas"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    @SuppressWarnings("unchecked")
    var returnedAlreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");

    assertThat(returnedAlreadyAddedSubareas)
        .extracting(LicenceBlockSubareaAddToListView::getId)
        .containsExactly(
            firstSubareaByLicence.subareaId().id(),
            secondSubareaByLicence.subareaId().id(),
            thirdSubareaByLicence.subareaId().id(),
            fourthSubareaByLicence.subareaId().id()
        );
  }

  @Test
  void getLicenceBlockSubareas_whenMultipleSubareaWithSameLicenceAndName_thenSortedByBlockComponents() throws Exception {

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .withSubareaId("first")
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .withSubareaId("second")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .withSubareaId("third")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .withSubareaId("fourth")
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withQuadrantNumber("10")
        .withSubareaId("fifth")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("name")
        .withBlockReference("block ref")
        .withQuadrantNumber("2")
        .withSubareaId("sixth")
        .build();

    var formWithSubareas = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(
            sixthSubareaByBlock.subareaId().id(),
            firstSubareaByBlock.subareaId().id(),
            thirdSubareaByBlock.subareaId().id(),
            secondSubareaByBlock.subareaId().id(),
            fifthSubareaByBlock.subareaId().id(),
            fourthSubareaByBlock.subareaId().id()
        ))
        .build();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(formWithSubareas);

    var unsortedSubareaList = List.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    var subareaIds = formWithSubareas.getSubareas()
        .stream()
        .map(LicenceBlockSubareaId::new)
        .toList();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        subareaIds,
        NominatedBlockSubareaController.ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE
    ))
        .thenReturn(unsortedSubareaList);

    var firstNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstSubareaByBlock.subareaId())
        .withName(firstSubareaByBlock.displayName())
        .build();
    var secondNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(secondSubareaByBlock.subareaId())
        .withName(secondSubareaByBlock.displayName())
        .build();
    var thirdNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(thirdSubareaByBlock.subareaId())
        .withName(thirdSubareaByBlock.displayName())
        .build();
    var fourthNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(fourthSubareaByBlock.subareaId())
        .withName(fourthSubareaByBlock.displayName())
        .build();
    var fifthNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(fifthSubareaByBlock.subareaId())
        .withName(fifthSubareaByBlock.displayName())
        .build();
    var sixthNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(sixthSubareaByBlock.subareaId())
        .withName(sixthSubareaByBlock.displayName())
        .build();

    when(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .thenReturn(List.of(
            firstNominatedBlockSubareaDto,
            thirdNominatedBlockSubareaDto,
            secondNominatedBlockSubareaDto,
            sixthNominatedBlockSubareaDto,
            fifthNominatedBlockSubareaDto,
            fourthNominatedBlockSubareaDto
        ));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("alreadyAddedSubareas"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    @SuppressWarnings("unchecked")
    var returnedAlreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");

    assertThat(returnedAlreadyAddedSubareas)
        .extracting(LicenceBlockSubareaAddToListView::getId)
        .containsExactly(
            firstSubareaByBlock.subareaId().id(),
            secondSubareaByBlock.subareaId().id(),
            thirdSubareaByBlock.subareaId().id(),
            fourthSubareaByBlock.subareaId().id(),
            fifthSubareaByBlock.subareaId().id(),
            sixthSubareaByBlock.subareaId().id()
        );
  }

  @Test
  void getLicenceBlockSubareas_whenMultipleSubareaWithSameLicenceAndBlock_thenSortedBySubareaName() throws Exception {

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("1")
        .withSubareaName("a name")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("2")
        .withSubareaName("B name")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("3")
        .withSubareaName("c name")
        .build();

    var formWithSubareas = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(
            firstSubareaByName.subareaId().id(),
            thirdSubareaByName.subareaId().id(),
            secondSubareaByName.subareaId().id()
        ))
        .build();

    when(nominatedBlockSubareaFormService.getForm(nominationDetail)).thenReturn(formWithSubareas);

    var unsortedSubareaList = List.of(
        thirdSubareaByName,
        firstSubareaByName,
        secondSubareaByName
    );

    var subareaIds = formWithSubareas.getSubareas()
        .stream()
        .map(LicenceBlockSubareaId::new)
        .toList();

    var firstNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstSubareaByName.subareaId())
        .build();
    var secondNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(secondSubareaByName.subareaId())
        .build();
    var thirdNominatedBlockSubareaDto = NominatedBlockSubareaDtoTestUtil.builder()
        .withSubareaId(thirdSubareaByName.subareaId())
        .build();

    when(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .thenReturn(List.of(
            firstNominatedBlockSubareaDto,
            thirdNominatedBlockSubareaDto,
            secondNominatedBlockSubareaDto
        ));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        subareaIds,
        NominatedBlockSubareaController.ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE
    ))
        .thenReturn(unsortedSubareaList);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("alreadyAddedSubareas"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    @SuppressWarnings("unchecked")
    var returnedAlreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");

    assertThat(returnedAlreadyAddedSubareas)
        .extracting(LicenceBlockSubareaAddToListView::getId)
        .containsExactly(
            firstSubareaByName.subareaId().id(),
            secondSubareaByName.subareaId().id(),
            thirdSubareaByName.subareaId().id()
        );
  }

  @Test
  void saveLicenceBlockSubareas_whenNoErrors_thenVerifyServiceCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");

    when(nominatedBlockSubareaFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(ExcludedWellboreController.class)
                .renderPossibleWellsToExclude(NOMINATION_ID)))
        );

    verify(nominatedBlockSubareaDetailPersistenceService, times(1))
        .createOrUpdateNominatedBlockSubareaDetail(eq(nominationDetail), any());
  }

  @Test
  void saveLicenceBlockSubareas_whenErrors_thenStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedBlockSubareaFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER))
        )
        .andExpect(status().isOk());

    verify(nominatedBlockSubareaDetailPersistenceService, never()).createOrUpdateNominatedBlockSubareaDetail(any(), any());
  }
}