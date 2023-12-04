package uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences;

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
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailFormTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationLicenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominationLicenceRepository nominationLicenceRepository;

  @Mock
  private LicenceQueryService licenceQueryService;

  @InjectMocks
  private NominationLicenceService nominationLicenceService;

  @Test
  void saveNominationLicence_assertNoDuplicates() {
    var licenceId = 10;
    var licenceDto = LicenceDtoTestUtil.builder().withLicenceId(licenceId).build();

    when(licenceQueryService.getLicencesByIdIn(List.of(licenceId), NominationLicenceService.SAVE_LICENCES_PURPOSE))
        .thenReturn(List.of(licenceDto));

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withLicences(List.of(String.valueOf(licenceId), String.valueOf(licenceId)))
        .build();

    nominationLicenceService.saveNominationLicence(NOMINATION_DETAIL, form);
    verify(nominationLicenceRepository).deleteAllByNominationDetail(NOMINATION_DETAIL);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominationLicence>> licenceArgumentCaptor = ArgumentCaptor.forClass(List.class);

    verify(nominationLicenceRepository, times(1)).saveAll(licenceArgumentCaptor.capture());

    var savedLicences = licenceArgumentCaptor.getValue();
    assertThat(savedLicences).extracting(
        NominationLicence::getNominationDetail,
        NominationLicence::getLicenceId
    ).containsExactly(
        tuple(NOMINATION_DETAIL, licenceDto.licenceId().id())
    );
  }

  @Test
  void getRelatedLicences_verifyCall() {
    nominationLicenceService.getRelatedLicences(NOMINATION_DETAIL);
    verify(nominationLicenceRepository).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}