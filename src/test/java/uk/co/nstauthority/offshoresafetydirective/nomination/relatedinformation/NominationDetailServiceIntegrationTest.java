package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;

@DatabaseIntegrationTest
class NominationDetailServiceIntegrationTest {

  @Autowired
  private NominationRepository nominationRepository;

  @Autowired
  private NominationDetailRepository nominationDetailRepository;

  @Autowired
  private NominationDetailService nominationDetailService;

  @Autowired
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Autowired
  private NominationPortalReferenceAccessService nominationPortalReferenceAccessService;

  @Autowired
  private NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @MockitoBean
  private Clock clock;

  private final Instant fixedInstant = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  private Nomination nomination;

  @BeforeEach
  void setUp() {
    SamlAuthenticationUtil.Builder().setSecurityContext();
    Mockito.when(clock.instant()).thenReturn(fixedInstant);
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    nomination = nominationRepository.save(new Nomination().setCreatedInstant(fixedInstant));
  }

  @Test
  void submitNomination_persistsDetail_andCopiesPortalReferences() {
    var draftDetail = nominationDetailRepository.save(NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withCreatedInstant(fixedInstant)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build());

    var form = new RelatedInformationForm();
    form.setRelatedToAnyLicenceApplications("true");
    form.setRelatedLicenceApplications("PEARS-123,PEARS-999");
    form.setRelatedToAnyWellApplications("true");
    form.setRelatedWellApplications("WONS-001;WONS-002");
    form.setRelatedToAnyFields("false");

    relatedInformationPersistenceService.createOrUpdateRelatedInformation(draftDetail, form);

    nominationDetailService.submitNomination(draftDetail);

    var submittedYear = LocalDate.ofInstant(fixedInstant, ZoneId.systemDefault()).getYear();
    var referenceNumber = nominationRepository.getTotalSubmissionsForYear(submittedYear);
    var expectedNomination = NominationTestUtil.builder()
        .withId(nomination.getId())
        .withCreatedInstant(fixedInstant)
        .withReference("WIO/%d/%d".formatted(submittedYear, referenceNumber))
        .build();

    var expectedNominationDetail = NominationDetailTestUtil.builder()
        .withId(draftDetail.getId())
        .withNomination(expectedNomination)
        .withCreatedInstant(fixedInstant)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(fixedInstant)
        .build();

    var persisted = nominationDetailRepository.findById(draftDetail.getId()).orElseThrow();
    assertThat(persisted).usingRecursiveComparison().isEqualTo(expectedNominationDetail);

    var references = nominationPortalReferenceAccessService.getNominationPortalReferenceDtosByNomination(nomination);

    var expectedPears = new NominationPortalReferenceDto(PortalReferenceType.PEARS, "PEARS-123,PEARS-999");
    var expectedWons = new NominationPortalReferenceDto(PortalReferenceType.WONS, "WONS-001;WONS-002");

    assertThat(references).containsExactlyInAnyOrder(expectedPears, expectedWons);
  }

  @Test
  void submitNomination_rollsBack_whenRelatedInformationIsMissing() {

    var draftDetail = nominationDetailRepository.save(NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withCreatedInstant(fixedInstant)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build()
    );

    assertThatThrownBy(() -> nominationDetailService.submitNomination(draftDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No related information found for nomination detail");

    var expectedNomination = NominationTestUtil.builder()
        .withId(nomination.getId())
        .withCreatedInstant(fixedInstant)
        .withReference(null)
        .build();

    var expectedNominationDetail = NominationDetailTestUtil.builder()
        .withId(draftDetail.getId())
        .withNomination(expectedNomination)
        .withCreatedInstant(fixedInstant)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .withSubmittedInstant(null)
        .build();

    var persisted = nominationDetailRepository.findById(draftDetail.getId()).orElseThrow();

    assertThat(persisted).usingRecursiveComparison().isEqualTo(expectedNominationDetail);

    assertThat(nominationPortalReferenceRepository.findAll()).isEmpty();
  }
}
