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
class InstallationAdviceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private InstallationAdviceRepository installationAdviceRepository;

  @Mock
  private InstallationAdviceFormValidator installationAdviceFormValidator;

  @InjectMocks
  private InstallationAdviceService installationAdviceService;

  @Test
  void createOrUpdateInstallationAdvice_givenAForm_assertEntityFields() {
    var form = new InstallationAdviceFormTestUtil.InstallationAdviceFormBuilder().build();

    when(installationAdviceRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    installationAdviceService.createOrUpdateInstallationAdvice(NOMINATION_DETAIL, form);

    var installationAdviceCaptor = ArgumentCaptor.forClass(InstallationAdvice.class);
    verify(installationAdviceRepository, times(1)).save(installationAdviceCaptor.capture());

    var savedEntity = installationAdviceCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            InstallationAdvice::getNominationDetail,
            InstallationAdvice::getIncludeInstallationsInNomination
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getIncludeInstallationsInNomination()
        );
  }

  @Test
  void getForm_whenEntityExist_assertFormFieldsMatchEntity() {
    var installationAdvice = new InstallationAdviceTestUtil.InstallationAdviceBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    when(installationAdviceRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(installationAdvice));

    var form = installationAdviceService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationAdviceForm::getIncludeInstallationsInNomination)
        .isEqualTo(installationAdvice.getIncludeInstallationsInNomination());
  }

  @Test
  void getForm_whenNoEntityExist_assertFormIsEmpty() {
    when(installationAdviceRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = installationAdviceService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(InstallationAdviceForm::getIncludeInstallationsInNomination)
        .isEqualTo(null);
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new InstallationAdviceFormTestUtil.InstallationAdviceFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    installationAdviceService.validate(form, bindingResult);

    verify(installationAdviceFormValidator, times(1)).validate(form, bindingResult);
  }
}