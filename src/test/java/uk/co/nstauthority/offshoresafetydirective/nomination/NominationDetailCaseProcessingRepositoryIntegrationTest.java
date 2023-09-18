package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeaderDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@Transactional
@DatabaseIntegrationTest
class NominationDetailCaseProcessingRepositoryIntegrationTest {

  @Autowired
  private NominationDetailCaseProcessingRepository nominationDetailCaseProcessingRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  void findCaseProcessingHeaderDto_verifyDataCorrectlyMapped() {

    var nominationReference = "nomination/reference";
    var applicantReference = "applicant/reference";

    var applicantOrgUnitId = 1;
    var nominatedOrgUnitId = 2;

    var wellSelectionType = WellSelectionType.NO_WELLS;
    var isInstallationIncluded = true;

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference(nominationReference)
        .build();

    persistAndFlush(nomination);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    persistAndFlush(nominationDetail);

    var applicantDetails = ApplicantDetailTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withPortalOrganisationId(applicantOrgUnitId)
        .withApplicantReference(applicantReference)
        .build();

    persistAndFlush(applicantDetails);

    var nomineeDetails = NomineeDetailTestingUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(nominatedOrgUnitId)
        .build();

    persistAndFlush(nomineeDetails);

    var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .withWellSelectionType(wellSelectionType)
        .build();

    persistAndFlush(wellSelectionSetup);

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(isInstallationIncluded)
        .build();

    persistAndFlush(installationInclusion);

    var result = nominationDetailCaseProcessingRepository.findCaseProcessingHeaderDto(nominationDetail);

    assertThat(result).isPresent();
    assertThat(result.get())
        .extracting(
            NominationCaseProcessingHeaderDto::nominationReference,
            NominationCaseProcessingHeaderDto::applicantOrganisationId,
            NominationCaseProcessingHeaderDto::nominatedOrganisationId,
            NominationCaseProcessingHeaderDto::selectionType,
            NominationCaseProcessingHeaderDto::includeInstallationsInNomination,
            NominationCaseProcessingHeaderDto::status
        ).contains(
            nomination.getReference(),
            applicantOrgUnitId,
            nominatedOrgUnitId,
            wellSelectionType,
            isInstallationIncluded,
            nominationDetail.getStatus()
        );
  }

  @Test
  void findCaseProcessingHeaderDto_whenNoNominationDetail_thenEmptyOptional() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var result = nominationDetailCaseProcessingRepository.findCaseProcessingHeaderDto(nominationDetail);

    assertThat(result).isEmpty();
  }

  private void persistAndFlush(Object entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }
}