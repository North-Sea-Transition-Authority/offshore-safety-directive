package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NomineeDetailController.class)
class NomineeDetailControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private final NominationId nominationId = new NominationId(1);
  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
      .withNominationId(nominationId)
      .build();
  private NomineeDetailForm form;

  @MockBean
  private NomineeDetailFormService nomineeDetailFormService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @BeforeEach
  void setup() {
    form = new NomineeDetailForm();
    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(nomineeDetailFormService.getForm(nominationDetail)).thenReturn(form);
    when(portalOrganisationUnitQueryService.getOrganisationById(any())).thenReturn(Optional.empty());
  }

  @Test
  void getNomineeDetail_assertModelProperties() throws Exception {

    form.setNominatedOrganisationId(100);

    var portalOrganisationUnit = new PortalOrganisationDto("20", "name");

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(portalOrganisationUnit));

    mockMvc.perform(
            get(ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(nominationId)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/nomineeDetails/nomineeDetail"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("pageTitle", NomineeDetailController.PAGE_NAME))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
        ))
        .andExpect(model().attribute(
            "preselectedItems",
            Map.of(portalOrganisationUnit.id(), portalOrganisationUnit.name())
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, null, null))
        ))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)),
                NominationTaskListController.PAGE_NAME
            )
        ));
  }

  @Test
  void saveNomineeDetail_whenValidForm_verifyMethodCalls() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().is3xxRedirection());

    verify(nomineeDetailFormService, times(1)).validate(any(), any());
    verify(nominationDetailService, times(1)).getLatestNominationDetail(nominationId);
    verify(nomineeDetailPersistenceService, times(1)).createOrUpdateNomineeDetail(eq(nominationDetail), any());
  }

  @Test
  void saveNomineeDetail_whenInvalidForm_verifyStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nomineeDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    mockMvc.perform(
            post(ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk());

    verify(nomineeDetailFormService, times(1)).validate(any(), any());
    verify(nomineeDetailPersistenceService, never()).createOrUpdateNomineeDetail(any(), any());
  }
}