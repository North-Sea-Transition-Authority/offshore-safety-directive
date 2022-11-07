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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@ContextConfiguration(classes = NominatedBlockSubareaController.class)
class NominatedBlockSubareaControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @MockBean
  NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @MockBean
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @MockBean
  private NominatedBlockSubareaFormService nominatedBlockSubareaFormService;

  @Test
  void getLicenceBlockSubareas_assertModelAndViewProperties() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    var form = new NominatedBlockSubareaForm();

    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(form);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/blockSubarea"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("pageTitle", NominatedBlockSubareaController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))
        ))
        .andExpect(model().attribute("alreadyAddedSubareas", Collections.emptyList()))
        .andExpect(model().attribute(
            "blockSubareaRestUrl",
            RestApiUtil.route(on(LicenceBlockSubareaRestController.class).searchWells(null))
        ));
  }

  @Test
  void getLicenceBlockSubareas_whenSavedBlockSubareas_assertSubareasAreSorted() throws Exception {

    var formWithSubareas = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(formWithSubareas);

    var firstBlockSubareaBySortKey = new LicenceBlockSubareaDto(1, "blockSubarea1", "1");
    var secondBlockSubareaBySortKey = new LicenceBlockSubareaDto(2, "blockSubarea2", "2");
    var thirdBlockSubareaBySortKey = new LicenceBlockSubareaDto(3, "blockSubarea3", "3");

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(formWithSubareas.getSubareas()))
        .thenReturn(List.of(secondBlockSubareaBySortKey, thirdBlockSubareaBySortKey, firstBlockSubareaBySortKey));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("alreadyAddedSubareas"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    @SuppressWarnings("unchecked")
    var returnedAlreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");

    assertThat(returnedAlreadyAddedSubareas)
        .extracting(
            LicenceBlockSubareaAddToListView::getId,
            LicenceBlockSubareaAddToListView::getSortKey
        )
        .containsExactly(
            tuple(
                String.valueOf(firstBlockSubareaBySortKey.id()),
                firstBlockSubareaBySortKey.sortKey()
            ),
            tuple(
                String.valueOf(secondBlockSubareaBySortKey.id()),
                secondBlockSubareaBySortKey.sortKey()
            ),
            tuple(
                String.valueOf(thirdBlockSubareaBySortKey.id()),
                thirdBlockSubareaBySortKey.sortKey()
            )
        );
  }

  @Test
  void saveLicenceBlockSubareas_whenNoErrors_thenVerifyServiceCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");

    when(nominatedBlockSubareaFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(
                ReverseRouter.route(
                    on(NominatedBlockSubareaController.class).saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID))));

    verify(nominatedBlockSubareaDetailPersistenceService, times(1))
        .createOrUpdateNominatedBlockSubareaDetail(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveLicenceBlockSubareas_whenErrors_thenStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedBlockSubareaFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(
                ReverseRouter.route(on(NominatedBlockSubareaController.class)
                    .saveLicenceBlockSubareas(NOMINATION_ID, null, null))
            )
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk());

    verify(nominatedBlockSubareaDetailPersistenceService, never()).createOrUpdateNominatedBlockSubareaDetail(any(), any());
  }
}