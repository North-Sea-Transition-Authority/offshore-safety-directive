package uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationType;

@ExtendWith(MockitoExtension.class)
class NominationTypeServiceTest {

  @Mock
  private NominationDetailRepository nominationDetailRepository;

  @InjectMocks
  private NominationTypeService nominationTypeService;

  @Nested
  class GetNominationDisplayType {

    private final NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    @Test
    void whenWellAndInstallationNomination() {

      var nominationType = NominationType.builder()
          .withInstallationNomination(true)
          .withWellNomination(true)
          .build();

      given(nominationDetailRepository.getNominationType(nominationDetail))
          .willReturn(nominationType);

      var resultingDisplayType = nominationTypeService.getNominationDisplayType(nominationDetail);

      assertThat(resultingDisplayType).isEqualTo(NominationDisplayType.WELL_AND_INSTALLATION);
    }

    @Test
    void whenWellOnlyNomination() {

      var nominationType = NominationType.builder()
          .withWellNomination(true)
          .withInstallationNomination(false)
          .build();

      given(nominationDetailRepository.getNominationType(nominationDetail))
          .willReturn(nominationType);

      var resultingDisplayType = nominationTypeService.getNominationDisplayType(nominationDetail);

      assertThat(resultingDisplayType).isEqualTo(NominationDisplayType.WELL);
    }

    @Test
    void whenInstallationOnlyNomination() {

      var nominationType = NominationType.builder()
          .withInstallationNomination(true)
          .withWellNomination(false)
          .build();

      given(nominationDetailRepository.getNominationType(nominationDetail))
          .willReturn(nominationType);

      var resultingDisplayType = nominationTypeService.getNominationDisplayType(nominationDetail);

      assertThat(resultingDisplayType).isEqualTo(NominationDisplayType.INSTALLATION);
    }

    @Test
    void whenNotWellOrInstallationNomination() {

      var nominationType = NominationType.builder()
          .withInstallationNomination(false)
          .withWellNomination(false)
          .build();

      given(nominationDetailRepository.getNominationType(nominationDetail))
          .willReturn(nominationType);

      var resultingDisplayType = nominationTypeService.getNominationDisplayType(nominationDetail);

      assertThat(resultingDisplayType).isEqualTo(NominationDisplayType.NOT_PROVIDED);
    }
  }
}
