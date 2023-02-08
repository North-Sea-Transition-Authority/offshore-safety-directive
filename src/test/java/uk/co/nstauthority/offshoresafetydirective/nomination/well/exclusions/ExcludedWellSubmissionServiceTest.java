package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellSubmissionServiceTest {

  @Mock
  private ExcludedWellFormService excludedWellFormService;

  @Mock
  private ExcludedWellValidator excludedWellValidator;

  @Mock
  private ExcludedWellPersistenceService excludedWellPersistenceService;

  @InjectMocks
  private ExcludedWellSubmissionService excludedWellSubmissionService;

  @Test
  void isExcludedWellJourneyComplete_whenError_thenFalse() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var form = new WellExclusionForm();

    given(excludedWellFormService.getExcludedWellForm(nominationDetail))
        .willReturn(form);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue("hasWellsToExclude", "code", "message");
      return bindingResult;
    })
        .when(excludedWellValidator).validate(eq(form), any(), any());

    var isJourneyComplete = excludedWellSubmissionService.isExcludedWellJourneyComplete(nominationDetail);

    assertThat(isJourneyComplete).isFalse();
  }

  @Test
  void isExcludedWellJourneyComplete_whenNoError_thenTrue() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    doAnswer(invocation -> ReverseRouter.emptyBindingResult())
        .when(excludedWellValidator).validate(any(), any(), any());

    var isJourneyComplete = excludedWellSubmissionService.isExcludedWellJourneyComplete(nominationDetail);

    assertThat(isJourneyComplete).isTrue();
  }

  @Test
  void cleanUpExcludedWellData_verifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    excludedWellSubmissionService.cleanUpExcludedWellData(nominationDetail);

    then(excludedWellPersistenceService)
        .should()
        .deleteExcludedWells(nominationDetail);

    then(excludedWellPersistenceService)
        .should()
        .deleteExcludedWellDetail(nominationDetail);
  }

}