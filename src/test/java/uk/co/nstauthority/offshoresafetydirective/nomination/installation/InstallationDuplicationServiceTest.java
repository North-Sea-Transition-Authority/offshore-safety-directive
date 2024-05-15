package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicence;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class InstallationDuplicationServiceTest {

  @Mock
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @Mock
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Mock
  private InstallationInclusionPersistenceService installationInclusionPersistenceService;
  
  @Mock
  private InstallationInclusionAccessService installationInclusionAccessService;

  @Mock
  private NominatedInstallationAccessService nominatedInstallationAccessService;

  @Mock
  private NominationLicenceService nominationLicenceService;

  @InjectMocks
  private InstallationDuplicationService installationDuplicationService;

  @Test
  void duplicate_installationInclusion_whenNoInclusion_thenVerifyNoDuplication() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(sourceNominationDetail))
        .thenReturn(Optional.empty());

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(installationInclusionPersistenceService, never()).saveInstallationInclusion(any());

    // Verify duplication of other areas continues as expected
    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }

  @Test
  void duplicate_installationInclusion_whenInclusion_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withId(UUID.randomUUID())
        .withNominationDetail(sourceNominationDetail)
        .includeInstallationsInNomination(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(sourceNominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var captor = ArgumentCaptor.forClass(InstallationInclusion.class);
    verify(installationInclusionPersistenceService).saveInstallationInclusion(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue(
            "includeInstallationsInNomination",
            installationInclusion.getIncludeInstallationsInNomination()
        )
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue())
        .extracting(InstallationInclusion::getId)
        .isNotEqualTo(installationInclusion.getId());

    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }


  @Test
  void duplicate_nominatedInstallations_whenNoLinkedInstallations_thenVerifyNoDuplication() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominatedInstallationAccessService.getNominatedInstallations(sourceNominationDetail))
        .thenReturn(List.of());

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedInstallationPersistenceService, never()).saveAllNominatedInstallations(any());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedInstallations_whenLinkedInstallations_thenVerifyLinksDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var nominatedInstallation = NominatedInstallationTestUtil.builder()
        .withId(UUID.randomUUID())
        .withInstallationId(255)
        .withNominationDetail(sourceNominationDetail)
        .build();

    when(nominatedInstallationAccessService.getNominatedInstallations(sourceNominationDetail))
        .thenReturn(List.of(nominatedInstallation));

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedInstallation>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominatedInstallationPersistenceService).saveAllNominatedInstallations(captor.capture());

    var capturedNominatedInstallationAssertion = assertThat(captor.getValue())
        .hasSize(1)
        .first();

    new PropertyObjectAssert(capturedNominatedInstallationAssertion)
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("installationId", nominatedInstallation.getInstallationId())
        .hasAssertedAllPropertiesExcept("id");

    capturedNominatedInstallationAssertion
        .extracting(NominatedInstallation::getId)
        .isNotEqualTo(nominatedInstallation.getId());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedInstallationDetail_whenNoInstallationDetail_thenVerifyNoDuplication() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedInstallationDetailPersistenceService, never()).saveNominatedInstallationDetail(any());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedInstallationDetail_whenInstallationDetailExists_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var installationDetail = NominatedInstallationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withNominationDetail(sourceNominationDetail)
        .withForAllInstallationPhases(true)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(sourceNominationDetail))
        .thenReturn(Optional.of(installationDetail));

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var captor = ArgumentCaptor.forClass(NominatedInstallationDetail.class);
    verify(nominatedInstallationDetailPersistenceService).saveNominatedInstallationDetail(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("forAllInstallationPhases", installationDetail.getForAllInstallationPhases())
        .hasFieldOrPropertyWithValue("developmentDesignPhase", installationDetail.getDevelopmentDesignPhase())
        .hasFieldOrPropertyWithValue(
            "developmentConstructionPhase",
            installationDetail.getDevelopmentConstructionPhase()
        )
        .hasFieldOrPropertyWithValue(
            "developmentInstallationPhase",
            installationDetail.getDevelopmentInstallationPhase()
        )
        .hasFieldOrPropertyWithValue(
            "developmentCommissioningPhase",
            installationDetail.getDevelopmentCommissioningPhase()
        )
        .hasFieldOrPropertyWithValue(
            "developmentProductionPhase",
            installationDetail.getDevelopmentProductionPhase()
        )
        .hasFieldOrPropertyWithValue("decommissioningPhase", installationDetail.getDecommissioningPhase())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue())
        .extracting(NominatedInstallationDetail::getId)
        .isNotEqualTo(installationDetail.getId());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
    verify(nominationLicenceService).getRelatedLicences(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedLicences_whenNoLinkedLicences_thenVerifyNoDuplication() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominationLicenceService.getRelatedLicences(sourceNominationDetail))
        .thenReturn(List.of());

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominationLicenceService, never()).saveAllNominationLicences(any());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedLicences_whenLinkedLicences_thenVerifyLinksDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var nominationLicence = NominationLicenceTestUtil.builder()
        .withId(UUID.randomUUID())
        .withLicenceId(255)
        .withNominationDetail(sourceNominationDetail)
        .build();

    when(nominationLicenceService.getRelatedLicences(sourceNominationDetail))
        .thenReturn(List.of(nominationLicence));

    installationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominationLicence>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominationLicenceService).saveAllNominationLicences(captor.capture());

    var capturedNominationLicenceAssertion = assertThat(captor.getValue())
        .hasSize(1)
        .first();

    new PropertyObjectAssert(capturedNominationLicenceAssertion)
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("licenceId", nominationLicence.getLicenceId())
        .hasAssertedAllPropertiesExcept("id");

    capturedNominationLicenceAssertion
        .extracting(NominationLicence::getId)
        .isNotEqualTo(nominationLicence.getId());

    // Verify duplication of other areas continues as expected
    verify(installationInclusionAccessService).getInstallationInclusion(sourceNominationDetail);
    verify(nominatedInstallationDetailPersistenceService).findNominatedInstallationDetail(sourceNominationDetail);
    verify(nominatedInstallationAccessService).getNominatedInstallations(sourceNominationDetail);
  }

}