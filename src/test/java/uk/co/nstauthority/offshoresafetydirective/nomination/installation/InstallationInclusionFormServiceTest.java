package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class InstallationInclusionFormServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private InstallationInclusionAccessService installationInclusionAccessService;

  @InjectMocks
  private InstallationInclusionFormService installationInclusionFormService;

  @Test
  void getForm_whenEntityExist_assertFormFieldsMatchEntity() {
    var installationInclusion = new InstallationInclusionTestUtil.InstallationInclusionBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    when(installationInclusionAccessService.getInstallationInclusion(NOMINATION_DETAIL))
        .thenReturn(Optional.of(installationInclusion));

    var form = installationInclusionFormService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationInclusionForm::getIncludeInstallationsInNomination)
        .isEqualTo(installationInclusion.getIncludeInstallationsInNomination());
  }

  @Test
  void getForm_whenNoEntityExist_assertFormIsEmpty() {
    when(installationInclusionAccessService.getInstallationInclusion(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var form = installationInclusionFormService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationInclusionForm::getIncludeInstallationsInNomination)
        .isNull();
  }

  @Test
  void isNotRelatedToInstallationOperatorship_whenFormNotYetAnswered_thenFalse() {
    when(installationInclusionAccessService.getInstallationInclusion(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    assertFalse(installationInclusionFormService.isNotRelatedToInstallationOperatorship(NOMINATION_DETAIL));
  }

  @Test
  void isNotRelatedToInstallationOperatorship_whenNotIncludingInstallation_thenTrue() {
    var installationInclusion = new InstallationInclusionTestUtil.InstallationInclusionBuilder()
        .includeInstallationsInNomination(false)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(NOMINATION_DETAIL))
        .thenReturn(Optional.of(installationInclusion));

    assertTrue(installationInclusionFormService.isNotRelatedToInstallationOperatorship(NOMINATION_DETAIL));
  }

  @Test
  void isNotRelatedToInstallationOperatorship_whenIncludingInstallation_thenFalse() {
    var installationInclusion = new InstallationInclusionTestUtil.InstallationInclusionBuilder()
        .includeInstallationsInNomination(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(NOMINATION_DETAIL))
        .thenReturn(Optional.of(installationInclusion));

    assertFalse(installationInclusionFormService.isNotRelatedToInstallationOperatorship(NOMINATION_DETAIL));
  }
}