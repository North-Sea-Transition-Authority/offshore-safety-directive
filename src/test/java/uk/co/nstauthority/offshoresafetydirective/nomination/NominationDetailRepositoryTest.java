package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@DatabaseIntegrationTest
@Transactional
class NominationDetailRepositoryTest {

  @Autowired
  private NominationDetailRepository nominationDetailRepository;

  @Autowired
  private EntityManager entityManager;

  @Nested
  class GetNominationType {

    @Test
    void whenNoWellOrInstallationSetup() {

      var nominationDetail = givenNominationDetail();

      NominationType nominationType = nominationDetailRepository.getNominationType(nominationDetail);

      assertThat(nominationType.isWellNomination()).isFalse();
      assertThat(nominationType.isInstallationNomination()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
        value = WellSelectionType.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"SPECIFIC_WELLS", "LICENCE_BLOCK_SUBAREA"}
    )
    void whenNoWellsTypeSelected(WellSelectionType wellSelectionType) {

      var nominationDetail = givenNominationDetail();

      var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
          .withId(null)
          .withNominationDetail(nominationDetail)
          .withWellSelectionType(wellSelectionType)
          .build();

      persistAndFlush(wellSelectionSetup);

      NominationType nominationType = nominationDetailRepository.getNominationType(nominationDetail);

      assertThat(nominationType.isWellNomination()).isFalse();
      assertThat(nominationType.isInstallationNomination()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
        value = WellSelectionType.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"SPECIFIC_WELLS", "LICENCE_BLOCK_SUBAREA"}
    )
    void whenWellsTypeSelected(WellSelectionType wellSelectionType) {

      var nominationDetail = givenNominationDetail();

      var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
          .withId(null)
          .withNominationDetail(nominationDetail)
          .withWellSelectionType(wellSelectionType)
          .build();

      persistAndFlush(wellSelectionSetup);

      NominationType nominationType = nominationDetailRepository.getNominationType(nominationDetail);

      assertThat(nominationType.isWellNomination()).isTrue();
      assertThat(nominationType.isInstallationNomination()).isFalse();
    }

    @Test
    void whenNotInstallations() {

      var nominationDetail = givenNominationDetail();

      var installationInclusion = InstallationInclusionTestUtil.builder()
          .withId(null)
          .withNominationDetail(nominationDetail)
          .includeInstallationsInNomination(false)
          .build();

      persistAndFlush(installationInclusion);

      NominationType nominationType = nominationDetailRepository.getNominationType(nominationDetail);

      assertThat(nominationType.isInstallationNomination()).isFalse();
      assertThat(nominationType.isWellNomination()).isFalse();
    }

    @Test
    void whenInstallations() {

      var nominationDetail = givenNominationDetail();

      var installationInclusion = InstallationInclusionTestUtil.builder()
          .withId(null)
          .withNominationDetail(nominationDetail)
          .includeInstallationsInNomination(true)
          .build();

      persistAndFlush(installationInclusion);

      NominationType nominationType = nominationDetailRepository.getNominationType(nominationDetail);

      assertThat(nominationType.isInstallationNomination()).isTrue();
      assertThat(nominationType.isWellNomination()).isFalse();
    }
  }

  private NominationDetail givenNominationDetail() {

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .build();

    persistAndFlush(nomination, nominationDetail);

    return nominationDetail;
  }

  private void persistAndFlush(Object... entities) {
    Arrays.asList(entities).forEach(entity -> entityManager.persist(entity));
    entityManager.flush();
  }
}