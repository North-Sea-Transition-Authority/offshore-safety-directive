package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private InstallationInclusionRepository installationInclusionRepository;

  @InjectMocks
  private InstallationInclusionPersistenceService installationInclusionPersistenceService;

  @Test
  void createOrUpdateInstallationInclusion_givenAForm_assertEntityFields() {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();

    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    installationInclusionPersistenceService.createOrUpdateInstallationInclusion(NOMINATION_DETAIL, form);

    var installationInclusionCaptor = ArgumentCaptor.forClass(InstallationInclusion.class);
    verify(installationInclusionRepository, times(1)).save(installationInclusionCaptor.capture());

    var savedEntity = installationInclusionCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            InstallationInclusion::getNominationDetail,
            InstallationInclusion::getIncludeInstallationsInNomination
        )
        .containsExactly(
            NOMINATION_DETAIL,
            BooleanUtils.toBooleanObject(form.getIncludeInstallationsInNomination())
        );
  }
}