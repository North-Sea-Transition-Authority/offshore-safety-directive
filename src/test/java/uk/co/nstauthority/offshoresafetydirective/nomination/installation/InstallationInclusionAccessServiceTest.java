package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionAccessServiceTest {

  @Mock
  private InstallationInclusionRepository installationInclusionRepository;

  @InjectMocks
  private InstallationInclusionAccessService installationInclusionAccessService;

  @Test
  void getInstallationInclusion_whenNotFound_thenEmptyOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(installationInclusionRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingInstallationInclusion = installationInclusionAccessService.getInstallationInclusion(nominationDetail);

    assertThat(resultingInstallationInclusion).isEmpty();
  }

  @Test
  void getInstallationInclusion_whenFound_thenPopulatedOptionalReturned() {
    
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedInstallationInclusion = InstallationInclusionTestUtil.builder().build();

    given(installationInclusionRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(expectedInstallationInclusion));

    var resultingInstallationInclusion = installationInclusionAccessService.getInstallationInclusion(nominationDetail);

    assertThat(resultingInstallationInclusion).contains(expectedInstallationInclusion);
  }

}