package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailFormTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationRepository nominatedInstallationRepository;

  @Mock
  private InstallationQueryService installationQueryService;

  @InjectMocks
  private NominatedInstallationService nominatedInstallationService;

  @Test
  void saveNominatedInstallations_whenFormHasDuplicateWells_verifyNoDuplicateWellsSaved() {
    var installationId1 = 1;
    var installationId2 = 2;
    var installationDto1 = new InstallationDto(installationId1, "installation1");
    var installationDto2 = new InstallationDto(installationId2, "installation2");
    var formWithDuplicateInstallation = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(installationId1, installationId2))
        .build();

    when(installationQueryService.getInstallationsByIdIn(List.of(installationId1, installationId2))).thenReturn(List.of(installationDto1, installationDto2));

    nominatedInstallationService.saveNominatedInstallations(NOMINATION_DETAIL, formWithDuplicateInstallation);

    verify(nominatedInstallationRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedInstallation>> installationArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(nominatedInstallationRepository, times(1)).saveAll(installationArgumentCaptor.capture());

    var savedWells = installationArgumentCaptor.getValue();
    assertThat(savedWells).extracting(
        NominatedInstallation::getInstallationId,
        NominatedInstallation::getNominationDetail
    ).containsExactly(
        tuple(installationDto1.id(), NOMINATION_DETAIL),
        tuple(installationDto2.id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void findAllByNominationDetail_verifyMethodCall() {
    nominatedInstallationService.findAllByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedInstallationRepository, times(1)).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}