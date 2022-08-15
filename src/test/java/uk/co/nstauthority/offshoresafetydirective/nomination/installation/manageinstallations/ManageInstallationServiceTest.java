package uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailViewService;

@ExtendWith(MockitoExtension.class)
class ManageInstallationServiceTest {
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private InstallationInclusionViewService installationInclusionViewService;

  @Mock
  private NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @InjectMocks
  private ManageInstallationService manageInstallationService;

  @Test
  void getInstallationInclusionView_verifyMethodCall() {
    manageInstallationService.getInstallationInclusionView(NOMINATION_DETAIL);

    verify(installationInclusionViewService, times(1)).getInstallationInclusionView(NOMINATION_DETAIL);
  }

  @Test
  void getNominatedInstallationDetailView_verifyMethodCall() {
    manageInstallationService.getNominatedInstallationDetailView(NOMINATION_DETAIL);

    verify(nominatedInstallationDetailViewService, times(1)).getNominatedInstallationDetailView(NOMINATION_DETAIL);
  }
}