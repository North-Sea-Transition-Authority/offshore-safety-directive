package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private InstallationInclusionRepository installationInclusionRepository;

  @InjectMocks
  private InstallationInclusionViewService installationInclusionViewService;

  @Test
  void getInstallationAdviceView_whenEntityExist_validateFields() {
    var installationInclusion = new InstallationInclusionTestUtil.InstallationInclusionBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(installationInclusion));

    var installationInclusionView = installationInclusionViewService.getInstallationInclusionView(NOMINATION_DETAIL);

    assertTrue(installationInclusionView.isPresent());
    assertEquals(
        installationInclusion.getIncludeInstallationsInNomination(),
        installationInclusionView.get().getIncludeInstallationsInNomination()
    );
  }

  @Test
  void getInstallationAdviceView_whenNoEntityExist_assertEmptyOptional() {
    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var installationInclusionView = installationInclusionViewService.getInstallationInclusionView(NOMINATION_DETAIL);

    assertTrue(installationInclusionView.isEmpty());
  }
}