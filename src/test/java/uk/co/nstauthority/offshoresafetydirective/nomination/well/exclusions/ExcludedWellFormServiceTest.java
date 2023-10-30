package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellFormServiceTest {

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @InjectMocks
  private ExcludedWellFormService excludedWellFormService;

  @Test
  void getExcludedWellForm_whenNoMatch_thenEmptyFormReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingForm = excludedWellFormService.getExcludedWellForm(nominationDetail);

    assertThat(resultingForm).hasAllNullFieldsOrPropertiesExcept("excludedWells");
    assertThat(resultingForm.getExcludedWells()).isEmpty();
  }

  @Test
  void getExcludedWellForm_whenMatch_thenPopulatedFormReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(true)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var expectedExcludedWell = ExcludedWellTestUtil.builder().build();

    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(expectedExcludedWell));

    var resultingForm = excludedWellFormService.getExcludedWellForm(nominationDetail);

    assertThat(resultingForm).hasNoNullFieldsOrProperties();
    assertThat(resultingForm.hasWellsToExclude()).isEqualTo("true");
    assertThat(resultingForm.getExcludedWells())
        .containsExactly(String.valueOf(expectedExcludedWell.getWellboreId()));
  }

  @Test
  void getExcludedWellForm_whenNotExcludingWells_thenExcludedWellListEmpty() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(false)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var resultingForm = excludedWellFormService.getExcludedWellForm(nominationDetail);

    then(excludedWellAccessService)
        .should(never())
        .getExcludedWells(nominationDetail);

    assertThat(resultingForm.hasWellsToExclude()).isEqualTo("false");
    assertThat(resultingForm.getExcludedWells()).isEmpty();
  }

}