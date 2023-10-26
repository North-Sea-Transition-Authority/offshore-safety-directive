package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailFormServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Mock
  private NominatedInstallationAccessService nominatedInstallationAccessService;

  @Mock
  private NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;

  @Mock
  private NominationLicenceService nominationLicenceService;

  @InjectMocks
  private NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntity() {
    var nominatedInstallation1 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(1)
        .build();
    var nominatedInstallation2 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(2)
        .build();
    var nominatedInstallationDetail = new NominatedInstallationDetailTestUtil.NominatedInstallationDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    var nominationLicence = NominationLicenceTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withLicenceId(100)
        .build();

    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedInstallationDetail));
    when(nominatedInstallationAccessService.getNominatedInstallations(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedInstallation1, nominatedInstallation2));
    when(nominationLicenceService.getRelatedLicences(NOMINATION_DETAIL))
        .thenReturn(List.of(nominationLicence));

    var form = nominatedInstallationDetailFormService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedInstallationDetailForm::getInstallations,
            NominatedInstallationDetailForm::getForAllInstallationPhases,
            NominatedInstallationDetailForm::getDevelopmentDesignPhase,
            NominatedInstallationDetailForm::getDevelopmentConstructionPhase,
            NominatedInstallationDetailForm::getDevelopmentInstallationPhase,
            NominatedInstallationDetailForm::getDevelopmentCommissioningPhase,
            NominatedInstallationDetailForm::getDevelopmentProductionPhase,
            NominatedInstallationDetailForm::getDecommissioningPhase,
            NominatedInstallationDetailForm::getLicences
        )
        .containsExactly(
            List.of(nominatedInstallation1.getInstallationId(), nominatedInstallation2.getInstallationId()),
            Objects.toString(nominatedInstallationDetail.getForAllInstallationPhases(), null),
            Objects.toString(nominatedInstallationDetail.getDevelopmentDesignPhase(), null),
            Objects.toString(nominatedInstallationDetail.getDevelopmentConstructionPhase(), null),
            Objects.toString(nominatedInstallationDetail.getDevelopmentInstallationPhase(), null),
            Objects.toString(nominatedInstallationDetail.getDevelopmentCommissioningPhase(), null),
            Objects.toString(nominatedInstallationDetail.getDevelopmentProductionPhase(), null),
            Objects.toString(nominatedInstallationDetail.getDecommissioningPhase(), null),
            List.of(nominationLicence.getLicenceId())
        );
  }

  @Test
  void getForm_whenNoEntityExist_thenFormEmpty() {
    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = nominatedInstallationDetailFormService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormService.validate(form, bindingResult);

    verify(nominatedInstallationDetailFormValidator, times(1)).validate(form, bindingResult);
  }
}