package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@DatabaseIntegrationTest
class NominationSnsQueryServiceTest {

  @Autowired
  private NominationSnsQueryService nominationSnsQueryService;

  @Autowired
  private EntityManager entityManager;

  @Test
  @Transactional
  void getNominationSnsDto() {

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .build();

    var wellSelectionType = WellSelectionType.NO_WELLS;
    var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withWellSelectionType(wellSelectionType)
        .build();

    var installationsIncluded = true;
    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(installationsIncluded)
        .build();

    var applicantOrgId = 123;
    var applicantDetails = ApplicantDetailTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withPortalOrganisationId(applicantOrgId)
        .build();

    var nominatedOrgId = 456;
    var nomineeDetails = NomineeDetailTestingUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(nominatedOrgId)
        .build();

    entityManager.persist(nomination);
    entityManager.persist(nominationDetail);
    entityManager.persist(wellSelectionSetup);
    entityManager.persist(installationInclusion);
    entityManager.persist(applicantDetails);
    entityManager.persist(nomineeDetails);
    entityManager.flush();

    var result = nominationSnsQueryService.getNominationSnsDto(nominationDetail);
    assertThat(result)
        .extracting(
            NominationSnsDto::nominationDetailId,
            NominationSnsDto::wellSelectionType,
            NominationSnsDto::hasInstallations,
            NominationSnsDto::applicantOrganisationUnitId,
            NominationSnsDto::nominatedOrganisationUnitId
        )
        .containsExactly(
            nominationDetail.getId(),
            wellSelectionType,
            installationsIncluded,
            applicantOrgId,
            nominatedOrgId
        );
  }
}