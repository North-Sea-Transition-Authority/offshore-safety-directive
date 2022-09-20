package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellSubmissionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @Mock
  private NominatedBlockSubareaFormService nominatedBlockSubareaFormService;

  @Mock
  private NominatedWellDetailFormService nominatedWellDetailFormService;

  @Mock
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @Mock
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @InjectMocks
  private WellSubmissionService wellSubmissionService;

  @Test
  void isSectionSubmittable_whenNotAnsweredSelectionSetup_thenFalse() {

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenNotIncludingWellsInNomination_thenTrue() {

    var noWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.NO_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(noWellSelectionSetup));

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingWellsInNominationAndSpecificWellsJourneyComplete_thenTrue() {

    var specificWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .build();

    var nominatedWellDetailForm = NominatedWellDetailTestUtil.getValidForm();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(specificWellSelectionSetup));
    when(nominatedWellDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedWellDetailForm);
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedWellDetailFormService).validate(eq(nominatedWellDetailForm), any());

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingWellsInNominationAndSpecificWellsJourneyIncomplete_thenFalse() {

    var specificWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .build();

    var nominatedWellDetailForm = NominatedWellDetailTestUtil.getValidForm();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(specificWellSelectionSetup));
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

    var subareaWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    var nominatedBlockSubareaForm = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(subareaWellSelectionSetup));
    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedBlockSubareaForm);
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenIncludingBlockSubareasInNominationAndLicenceBlockSubareasJourneyIncomplete_thenFalse() {

    var subareaWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    var nominatedBlockSubareaForm = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(subareaWellSelectionSetup));
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

  @Test
  void onSubmission_whenNoWellsRelevantToNomination_thenSpecificAndSubareaWellsCleanedUp() {

    var noWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.NO_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(noWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    verify(nominatedWellDetailPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);
    verify(nominatedBlockSubareaDetailPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);

  }

  @Test
  void onSubmission_whenSpecificWellsRelevantToNomination_thenSubareaWellsCleanedUp() {

    var specificWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(specificWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaDetailPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);

  }

  @Test
  void onSubmission_whenSubareaWellsRelevantToNomination_thenSpecificWellsCleanedUp() {

    var subareaWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(subareaWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    verify(nominatedWellDetailPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);

  }
}