package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@ExtendWith(MockitoExtension.class)
class LicenceBlockSubareaWellboreServiceTest {

  @Mock
  private LicenceBlockSubareaQueryService subareaQueryService;

  @InjectMocks
  private LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @Test
  void getSubareaRelatedWellbores_whenNoSubareaMatches_thenEmptyList() {

    var noMatchingLicenceBlockSubareaId = new LicenceBlockSubareaId("not a match");

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(noMatchingLicenceBlockSubareaId)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(noMatchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void getSubareaRelatedWellbores_whenNoWellboresInSubarea_thenEmptyList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var subareaWithNoWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbores(Collections.emptyList())
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId)
    ))
        .willReturn(List.of(subareaWithNoWellbores));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void getSubareaRelatedWellbores_whenWellboresInSubarea_thenPopulatedWellboreList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var wellbore = WellDtoTestUtil.builder()
        .withWellboreId(100)
        .build();

    var subareaWithWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(wellbore)
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId)
    ))
        .willReturn(List.of(subareaWithWellbores));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores)
        .extracting(WellDto::wellboreId)
        .containsExactly(new WellboreId(100));
  }

  @Test
  void getSubareaRelatedWellbores_whenSameWellboreInMultipleSubareas_thenOnlyOneInstanceInResultingList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var wellbore = WellDtoTestUtil.builder()
        .withWellboreId(100)
        .build();

    var subareaWithDuplicateWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(wellbore)
        .withWellbore(wellbore)
        .build();

    var subareaWithSameWellboreAsAnother = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(wellbore)
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId)
    ))
        .willReturn(List.of(subareaWithDuplicateWellbores, subareaWithSameWellboreAsAnother));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores)
        .extracting(WellDto::wellboreId)
        .containsExactly(new WellboreId(100));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getSubareaRelatedWellbores_whenEmptyListNoSubareaMatches_thenEmptyList(
      List<LicenceBlockSubareaId> licenceBlockSubareaIds
  ) {

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        licenceBlockSubareaIds
    );

    assertThat(resultingWellbores).isEmpty();
  }

}