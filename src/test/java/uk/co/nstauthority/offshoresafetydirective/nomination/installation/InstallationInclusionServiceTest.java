package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationInclusionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private InstallationInclusionRepository installationInclusionRepository;

  @Mock
  private InstallationInclusionFormValidator installationInclusionFormValidator;

  @InjectMocks
  private InstallationInclusionService installationInclusionService;

  @Test
  void createOrUpdateInstallationInclusion_givenAForm_assertEntityFields() {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();

    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    installationInclusionService.createOrUpdateInstallationInclusion(NOMINATION_DETAIL, form);

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
            form.getIncludeInstallationsInNomination()
        );
  }

  @Test
  void getForm_whenEntityExist_assertFormFieldsMatchEntity() {
    var installationInclusion = new InstallationInclusionTestUtil.InstallationInclusionBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(installationInclusion));

    var form = installationInclusionService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationInclusionForm::getIncludeInstallationsInNomination)
        .isEqualTo(installationInclusion.getIncludeInstallationsInNomination());
  }

  @Test
  void getForm_whenNoEntityExist_assertFormIsEmpty() {
    when(installationInclusionRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = installationInclusionService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationInclusionForm::getIncludeInstallationsInNomination)
        .isNull();
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    installationInclusionService.validate(form, bindingResult);

    verify(installationInclusionFormValidator, times(1)).validate(form, bindingResult);
  }
}