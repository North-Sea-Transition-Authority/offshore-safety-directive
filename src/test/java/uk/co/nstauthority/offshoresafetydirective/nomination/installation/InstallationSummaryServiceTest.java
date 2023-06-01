package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@ExtendWith(MockitoExtension.class)
class InstallationSummaryServiceTest {

  private static final SummaryValidationBehaviour VALIDATION_BEHAVIOUR = SummaryValidationBehaviour.VALIDATED;

  @Mock
  private NominatedInstallationAccessService nominatedInstallationAccessService;

  @Mock
  private InstallationSubmissionService installationSubmissionService;

  @Mock
  private InstallationInclusionAccessService installationInclusionAccessService;

  @Mock
  private InstallationQueryService installationQueryService;

  @Mock
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @InjectMocks
  private InstallationSummaryService installationSummaryService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    nominationDetail = NominationDetailTestUtil.builder().build();
  }

  @Test
  void getInstallationSummaryView_whenRelated_andNotRelatedToAllPhases_thenAssertFields() {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(true)
        .build();

    var nominatedInstallationDetail = NominatedInstallationDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withForAllInstallationPhases(false)
        .withDecommissioningPhase(true)
        .withDevelopmentInstallationPhase(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.of(nominatedInstallationDetail));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    var expectedPhases = Stream.of(InstallationPhase.DEVELOPMENT_INSTALLATION, InstallationPhase.DECOMMISSIONING)
        .sorted(Comparator.comparing(InstallationPhase::getDisplayOrder))
        .toList();

    assertThat(result)
        .extracting(
            InstallationSummaryView::installationRelatedToNomination,
            InstallationSummaryView::installationForAllPhases
        ).containsExactly(
            new InstallationRelatedToNomination(true, List.of()),
            new InstallationForAllPhases(false, List.of(
                expectedPhases.get(0).getScreenDisplayText(),
                expectedPhases.get(1).getScreenDisplayText()
            ))
        );
  }

  @Test
  void getInstallationSummaryView_whenRelated_andRelatedToAllPhases_thenAssertFields() {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(true)
        .build();

    var nominatedInstallationDetail = NominatedInstallationDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withForAllInstallationPhases(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.of(nominatedInstallationDetail));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(
            InstallationSummaryView::installationRelatedToNomination,
            InstallationSummaryView::installationForAllPhases
        ).containsExactly(
            new InstallationRelatedToNomination(true, List.of()),
            new InstallationForAllPhases(true, List.of())
        );
  }

  @Test
  void getInstallationSummaryView_whenRelated_thenAssertInstallationNames() {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(true)
        .build();

    var nominatedInstallationA = NominatedInstallationTestUtil.builder()
        .withInstallationId(1)
        .build();

    var nominatedInstallationB = NominatedInstallationTestUtil.builder()
        .withInstallationId(2)
        .build();

    var installationA = InstallationDtoTestUtil.builder()
        .withId(1)
        .withName("Installation A")
        .build();

    var installationB = InstallationDtoTestUtil.builder()
        .withId(2)
        .withName("Installation B")
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    when(nominatedInstallationAccessService.getNominatedInstallations(nominationDetail))
        .thenReturn(List.of(nominatedInstallationB, nominatedInstallationA));

    when(installationQueryService.getInstallationsByIdIn(List.of(2, 1)))
        .thenReturn(List.of(installationB, installationA));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(InstallationSummaryView::installationRelatedToNomination)
        .extracting(InstallationRelatedToNomination::relatedInstallations)
        .isEqualTo(List.of(
            installationA.name(),
            installationB.name()
        ));
  }

  @Test
  void getInstallationSummaryView_whenRelated_andNoNominatedInstallationDetail_thenAssertFields() {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(
            InstallationSummaryView::installationRelatedToNomination,
            InstallationSummaryView::installationForAllPhases
        ).containsExactly(
            new InstallationRelatedToNomination(true, List.of()),
            null
        );
  }

  @Test
  void getInstallationSummaryView_whenNotRelated_thenAssertFields() {

    var installationInclusion = InstallationInclusionTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .includeInstallationsInNomination(false)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(
            InstallationSummaryView::installationRelatedToNomination,
            InstallationSummaryView::installationForAllPhases
        ).containsExactly(
            new InstallationRelatedToNomination(false, List.of()),
            null
        );
  }

  @Test
  void getInstallationSummaryView_whenNoAnswer_andSectionSubmittable_thenAssertFields() {
    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.empty());

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result).isEqualTo(new InstallationSummaryView(null));
  }

  @Test
  void getInstallationSummaryView_whenNoAnswer_andSectionIsNotSubmittable_thenAssertFields() {
    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.empty());

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .isEqualTo(new InstallationSummaryView(SummarySectionError.createWithDefaultMessage("installations")));
  }

  @ParameterizedTest
  @EnumSource(SummaryValidationBehaviour.class)
  void getInstallationSummaryView_verifyValidationBehaviourInteractions(
      SummaryValidationBehaviour validationBehaviour
  ) {
    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.empty());

    installationSummaryService.getInstallationSummaryView(nominationDetail, validationBehaviour);

    switch (validationBehaviour) {
      case VALIDATED -> verify(installationSubmissionService).isSectionSubmittable(nominationDetail);
      case NOT_VALIDATED -> verify(installationSubmissionService, never()).isSectionSubmittable(nominationDetail);
    }
  }

}