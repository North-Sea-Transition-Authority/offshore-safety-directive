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
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationRepository nominatedInstallationRepository;

  @Mock
  private InstallationQueryService installationQueryService;

  @InjectMocks
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Test
  void saveNominatedInstallations_whenFormHasDuplicateInstallations_verifyNoDuplicateInstallationsSaved() {

    var installationId = 1;

    var installationDto = InstallationDtoTestUtil.builder()
        .withId(installationId)
        .build();

    var formWithDuplicateInstallation = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(installationId, installationId))
        .build();

    when(installationQueryService.getInstallationsByIdIn(List.of(installationId)))
        .thenReturn(List.of(installationDto));

    nominatedInstallationPersistenceService.saveNominatedInstallations(NOMINATION_DETAIL, formWithDuplicateInstallation);

    verify(nominatedInstallationRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedInstallation>> installationArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(nominatedInstallationRepository, times(1)).saveAll(installationArgumentCaptor.capture());

    var savedInstallations = installationArgumentCaptor.getValue();
    assertThat(savedInstallations).extracting(
        NominatedInstallation::getInstallationId,
        NominatedInstallation::getNominationDetail
    ).containsExactly(
        tuple(installationDto.id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void deleteByNominationDetail_verifyRepoCall() {
    nominatedInstallationPersistenceService.deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedInstallationRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);
  }
}