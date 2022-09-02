package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationSubmissionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder().build();

  @Mock
  private InstallationInclusionFormService installationInclusionFormService;

  @Mock
  private NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @Mock
  private InstallationInclusionValidationService installationInclusionValidationService;

  @Mock
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Mock
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @InjectMocks
  private InstallationSubmissionService installationSubmissionService;

  @Test
  void isSectionSubmittable_whenInstallationsInclusionFormNotValid_thenTrue() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(null)
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);

    doAnswer(invocation -> {
      BindingResult bindingResult = invocation.getArgument(1);
      bindingResult.rejectValue("includeInstallationsInNomination", "includeInstallationsInNomination.required", "test message");
      return bindingResult;
    }).when(installationInclusionValidationService).validate(eq(installationInclusionForm), any(), any());

    assertFalse(installationSubmissionService.isSectionSubmittable(NOMINATION_DETAIL));
  }

  @Test
  void isSectionSubmittable_whenNoInstallationsInNomination_thenTrue() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(false)
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);

    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(installationInclusionValidationService).validate(eq(installationInclusionForm), any(), any());

    assertTrue(installationSubmissionService.isSectionSubmittable(NOMINATION_DETAIL));
  }

  @Test
  void isSectionSubmittable_whenInstallationsInNominationAndInstallationDetailFormValid_thenTrue() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(true)
        .build();
    var nominatedInstallationDetailForm = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);
    when(nominatedInstallationDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedInstallationDetailForm);

    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(installationInclusionValidationService).validate(eq(installationInclusionForm), any(), any());
    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(nominatedInstallationDetailFormService).validate(eq(nominatedInstallationDetailForm), any());

    assertTrue(installationSubmissionService.isSectionSubmittable(NOMINATION_DETAIL));
  }

  @Test
  void isSectionSubmittable_whenInstallationsInNominationAndInstallationDetailFormNotValid_thenFalse() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(true)
        .build();
    var nominatedInstallationDetailForm = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);
    when(nominatedInstallationDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(nominatedInstallationDetailForm);

    doAnswer(invocation -> invocation.<BindingResult>getArgument(1))
        .when(installationInclusionValidationService).validate(eq(installationInclusionForm), any(), any());
    doAnswer(invocation -> {
      BindingResult bindingResult = invocation.getArgument(1);
      bindingResult.rejectValue("forAllInstallationPhases", "forAllInstallationPhases.required", "test message");
      return bindingResult;
    }).when(nominatedInstallationDetailFormService).validate(eq(nominatedInstallationDetailForm), any());

    assertFalse(installationSubmissionService.isSectionSubmittable(NOMINATION_DETAIL));
  }

  @Test
  void onSubmission_whenIncludeInstallations_thenDontCleanDB() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(true)
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);

    installationSubmissionService.onSubmission(NOMINATION_DETAIL);

    verify(nominatedInstallationPersistenceService, never()).deleteByNominationDetail(any());
    verify(nominatedInstallationDetailPersistenceService, never()).deleteByNominationDetail(any());
  }

  @Test
  void onSubmission_whenNotIncludeInstallations_thenCleanDB() {
    var installationInclusionForm = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder()
        .includeInstallationsInNomination(false)
        .build();

    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(installationInclusionForm);

    installationSubmissionService.onSubmission(NOMINATION_DETAIL);

    verify(nominatedInstallationPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);
    verify(nominatedInstallationDetailPersistenceService, times(1)).deleteByNominationDetail(NOMINATION_DETAIL);
  }
}