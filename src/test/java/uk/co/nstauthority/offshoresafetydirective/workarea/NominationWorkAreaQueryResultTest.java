package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@ExtendWith(MockitoExtension.class)
class NominationWorkAreaQueryResultTest {

  @ParameterizedTest
  @EnumSource(value = WellSelectionType.class)
  void getNominationDisplayType_whenInstallationsIncluded_thenTestWellSelection(WellSelectionType selectionType) {

    var transformer = NominationWorkAreaQueryResultTestUtil.builder()
        .withWellSelectionType(selectionType)
        .withHasInstallations(true)
        .build();

    switch (selectionType) {
      case SPECIFIC_WELLS, LICENCE_BLOCK_SUBAREA ->
          assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.BOTH);
      case NO_WELLS ->
          assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.INSTALLATION);
    }

  }

  @ParameterizedTest
  @EnumSource(value = WellSelectionType.class)
  void getNominationDisplayType_whenInstallationsNotIncluded_thenTestWellSelection(WellSelectionType selectionType) {

    var transformer = NominationWorkAreaQueryResultTestUtil.builder()
        .withWellSelectionType(selectionType)
        .withHasInstallations(false)
        .build();

    switch (selectionType) {
      case SPECIFIC_WELLS, LICENCE_BLOCK_SUBAREA ->
          assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.WELL);
      case NO_WELLS ->
          assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.NOT_PROVIDED);
    }

  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getNominationDisplayType_whenWellsIncluded_thenTestInstallationsIncluded(boolean installationsIncluded) {

    var transformer = NominationWorkAreaQueryResultTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .withHasInstallations(installationsIncluded)
        .build();

    if (installationsIncluded) {
      assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.BOTH);
    } else {
      assertThat(transformer.getNominationDisplayType()).isEqualTo(NominationDisplayType.WELL);
    }

  }

}