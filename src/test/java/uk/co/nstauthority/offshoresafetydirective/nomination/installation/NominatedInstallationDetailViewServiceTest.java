package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Mock
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Mock
  private InstallationQueryService installationQueryService;

  @InjectMocks
  private NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @Test
  void getNominatedInstallationDetailView_whenEntityExist_assertViewFields() {
    var nominatedInstallationDetail = new NominatedInstallationDetailTestUtil.NominatedInstallationDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();
    var nominatedInstallation1 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(1)
        .build();
    var nominatedInstallation2 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(2)
        .build();

    var installationDto1 = InstallationDtoTestUtil.builder()
        .withId(nominatedInstallation1.getInstallationId())
        .build();

    var installationDto2 = InstallationDtoTestUtil.builder()
        .withId(nominatedInstallation2.getInstallationId())
        .build();

    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedInstallationDetail));
    when(nominatedInstallationPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedInstallation1, nominatedInstallation2));
    when(installationQueryService.getInstallationsByIdIn(
        List.of(nominatedInstallation1.getInstallationId(), nominatedInstallation2.getInstallationId())
    )).thenReturn(List.of(installationDto1, installationDto2));

    var nominatedInstallationDetailView =
        nominatedInstallationDetailViewService.getNominatedInstallationDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedInstallationDetailView.isPresent());
    assertThat(nominatedInstallationDetailView.get())
        .extracting(
            NominatedInstallationDetailView::getInstallations,
            NominatedInstallationDetailView::getForAllInstallationPhases,
            NominatedInstallationDetailView::getInstallationPhases
        )
        .containsExactly(
            List.of(installationDto1, installationDto2),
            nominatedInstallationDetail.getForAllInstallationPhases(),
            List.of(
                InstallationPhase.DEVELOPMENT_DESIGN,
                InstallationPhase.DEVELOPMENT_CONSTRUCTION,
                InstallationPhase.DEVELOPMENT_INSTALLATION,
                InstallationPhase.DEVELOPMENT_COMMISSIONING,
                InstallationPhase.DEVELOPMENT_PRODUCTION,
                InstallationPhase.DECOMMISSIONING
            )
        );
  }

  @Test
  void getNominatedInstallationDetailView_whenNoEntityExist_assertEmptyOptional() {

    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var nominatedInstallationDetailView = nominatedInstallationDetailViewService.getNominatedInstallationDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedInstallationDetailView.isEmpty());
  }
}