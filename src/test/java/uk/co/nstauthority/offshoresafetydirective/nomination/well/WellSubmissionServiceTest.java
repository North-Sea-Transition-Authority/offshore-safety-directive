package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea.NominatedBlockSubareaFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea.NominatedBlockSubareaFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellSubmissionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupFormService wellSelectionSetupFormService;

  @Mock
  private NominatedBlockSubareaFormService nominatedBlockSubareaFormService;

  @Mock
  private NominatedWellDetailFormService nominatedWellDetailFormService;

  @InjectMocks
  private WellSubmissionService wellSubmissionService;

  @Test
  void isSectionSubmittable_whenNotAnsweredSelectionSetup_thenFalse() {
    var emptyWellSelectionSetupForm = new WellSelectionSetupForm();

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(emptyWellSelectionSetupForm);

    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenNotIncludingWellsInNomination_thenTrue() {
    var emptyWellSelectionSetupForm = new WellSelectionSetupForm();
    emptyWellSelectionSetupForm.setWellSelectionType(WellSelectionType.NO_WELLS.name());

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(emptyWellSelectionSetupForm);

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingWellsInNominationAndSpecificWellsJourneyComplete_thenTrue() {
    var wellSelectionSetupForm = new WellSelectionSetupForm();
    wellSelectionSetupForm.setWellSelectionType(WellSelectionType.SPECIFIC_WELLS.name());
    var nominatedWellDetailForm = NominatedWellDetailTestUtil.getValidForm();

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(wellSelectionSetupForm);
    when(nominatedWellDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedWellDetailForm);
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedWellDetailFormService).validate(eq(nominatedWellDetailForm), any());

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingWellsInNominationAndSpecificWellsJourneyIncomplete_thenFalse() {
    var wellSelectionSetupForm = new WellSelectionSetupForm();
    wellSelectionSetupForm.setWellSelectionType(WellSelectionType.SPECIFIC_WELLS.name());
    var nominatedWellDetailForm = NominatedWellDetailTestUtil.getValidForm();

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(wellSelectionSetupForm);
    when(nominatedWellDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedWellDetailForm);
    doAnswer(invocation -> {
      BindingResult bindingResult = invocation.getArgument(1);
      bindingResult.rejectValue("wells", "wells.required", "test message");
      return bindingResult;
    }).when(nominatedWellDetailFormService).validate(eq(nominatedWellDetailForm), any());

    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingBlockSubareasInNominationAndLicenceBlockSubareasJourneyComplete_thenTrue() {
    var wellSelectionSetupForm = new WellSelectionSetupForm();
    wellSelectionSetupForm.setWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA.name());
    var nominatedBlockSubareaForm = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(wellSelectionSetupForm);
    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedBlockSubareaForm);
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingBlockSubareasInNominationAndLicenceBlockSubareasJourneyIncomplete_thenFalse() {
    var wellSelectionSetupForm = new WellSelectionSetupForm();
    wellSelectionSetupForm.setWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA.name());
    var nominatedBlockSubareaForm = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();

    when(wellSelectionSetupFormService.getForm(NOMINATION_DETAIL)).thenReturn(wellSelectionSetupForm);
    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedBlockSubareaForm);
    doAnswer(invocation -> {
      BindingResult bindingResult = invocation.getArgument(1);
      bindingResult.rejectValue("subareas", "subareas.required", "test message");
      return bindingResult;
    }).when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }
}