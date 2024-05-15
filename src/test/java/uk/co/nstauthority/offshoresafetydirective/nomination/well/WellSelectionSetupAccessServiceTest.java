package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupAccessServiceTest {

  @Mock
  private WellSelectionSetupRepository wellSelectionSetupRepository;

  @InjectMocks
  private WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @Test
  void getWellSelectionType_whenNoMatch_thenEmptyOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingWellSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail);

    assertThat(resultingWellSelectionType).isEmpty();
  }

  @Test
  void getWellSelectionType_whenMatch_thenWellSelectionTypeReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedWellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .build();

    given(wellSelectionSetupRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(expectedWellSelectionSetup));

    var resultingWellSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail);

    assertThat(resultingWellSelectionType)
        .contains(WellSelectionType.LICENCE_BLOCK_SUBAREA);
  }

  @Test
  void getWellSelectionSetup_whenNoMatch_thenEmptyOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingWellSelectionSetup = wellSelectionSetupAccessService.getWellSelectionSetup(nominationDetail);

    assertThat(resultingWellSelectionSetup).isEmpty();
  }

  @Test
  void getWellSelectionSetup_whenMatch_thenWellSelectionTypeReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedWellSelectionSetup = WellSelectionSetupTestUtil.builder().build();

    given(wellSelectionSetupRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(expectedWellSelectionSetup));

    var resultingWellSelectionSetup = wellSelectionSetupAccessService.getWellSelectionSetup(nominationDetail);

    assertThat(resultingWellSelectionSetup).contains(expectedWellSelectionSetup);
  }
}