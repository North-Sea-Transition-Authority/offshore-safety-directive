package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellSubmissionService;

@ExtendWith(MockitoExtension.class)
class WellSubmissionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

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

  @Mock
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @Mock
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @Mock
  private ExcludedWellSubmissionService excludedWellSubmissionService;

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

    var nominatedWellDetailForm = NominatedWellFormTestUtil.builder().build();

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

    var nominatedWellDetailForm = NominatedWellFormTestUtil.builder().build();

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
  void isSectionSubmittable_whenSubareaSelectionAndSubareasJourneyCompleteAndExclusionComplete_thenTrue() {

    // given a licence block subarea wellbore selection form
    var subareaWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    var nominatedBlockSubareaForm = NominatedBlockSubareaFormTestUtil.builder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(subareaWellSelectionSetup));

    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL))
        .thenReturn(nominatedBlockSubareaForm);

    // and no errors in the subarea selection form
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    // and no errors in well exclusion form
    when(excludedWellSubmissionService.isExcludedWellJourneyComplete(NOMINATION_DETAIL))
        .thenReturn(true);

    // then the section is submittable
    assertTrue(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenSubareaSelectionAndSubareasJourneyIncomplete_thenFalse() {

    // given a licence block subarea wellbore selection form
    var subareaWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    var nominatedBlockSubareaForm = NominatedBlockSubareaFormTestUtil.builder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(subareaWellSelectionSetup));

    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL))
        .thenReturn(nominatedBlockSubareaForm);

    // and errors in the subarea selection form
    doAnswer(invocation -> {
      BindingResult bindingResult = invocation.getArgument(1);
      bindingResult.rejectValue("subareas", "subareas.required", "test message");
      return bindingResult;
    })
        .when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    // then the section is not submittable
    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void isSectionSubmittable_whenSubareaSelectionAndSubareasJourneyCompleteAndExclusionIncomplete_thenFalse() {

    // given a licence block subarea wellbore selection form
    var subareaWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    var nominatedBlockSubareaForm = NominatedBlockSubareaFormTestUtil.builder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(subareaWellSelectionSetup));

    when(nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedBlockSubareaForm);

    // and no errors in the subarea selection form
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedBlockSubareaFormService).validate(eq(nominatedBlockSubareaForm), any());

    // and errors in well exclusion form
    when(excludedWellSubmissionService.isExcludedWellJourneyComplete(NOMINATION_DETAIL))
        .thenReturn(false);

    // then the section is not submittable
    assertFalse(
        wellSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)
    );
  }

  @Test
  void onSubmission_whenNoWellsRelevantToNomination_thenSpecificAndSubareaWellsCleanedUp() {

    // given a no wellbore selection
    var noWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.NO_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(noWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    // then specific well data is cleaned up
    verify(nominatedWellDetailPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedWellPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    // and subarea selection data is cleaned up
    verify(nominatedBlockSubareaDetailPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    // and excluded well data is cleaned up
    verify(excludedWellSubmissionService, times(1))
        .cleanUpExcludedWellData(NOMINATION_DETAIL);
  }

  @Test
  void onSubmission_whenSpecificWellsRelevantToNomination_thenSubareaWellsCleanedUp() {

    // given a specific wellbore selection
    var specificWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(specificWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    // then subarea selection data is cleaned up
    verify(nominatedBlockSubareaDetailPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    // and excluded well data is cleaned up
    verify(excludedWellSubmissionService, times(1))
        .cleanUpExcludedWellData(NOMINATION_DETAIL);

    // and no specific well data is cleaned up
    verify(nominatedWellDetailPersistenceService, never())
        .deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedWellPersistenceService, never())
        .deleteByNominationDetail(NOMINATION_DETAIL);

  }

  @Test
  void onSubmission_whenSubareaWellsRelevantToNomination_thenSpecificWellsCleanedUp() {

    // given a licence block subarea selection
    var subareaWellSelection = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(subareaWellSelection));

    wellSubmissionService.onSubmission(NOMINATION_DETAIL);

    // then specific well data is cleaned up
    verify(nominatedWellDetailPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedWellPersistenceService, times(1))
        .deleteByNominationDetail(NOMINATION_DETAIL);

    // and no subarea data is cleaned up
    verify(nominatedBlockSubareaDetailPersistenceService, never())
        .deleteByNominationDetail(any());

    verify(nominatedBlockSubareaPersistenceService, never())
        .deleteByNominationDetail(any());

    // and no excluded well data is cleaned up
    verify(excludedWellSubmissionService, never())
        .cleanUpExcludedWellData(any());
  }
}