package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

@ExtendWith(MockitoExtension.class)
class InstallationSummaryServiceTest {

  @Mock
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Mock
  private InstallationSubmissionService installationSubmissionService;

  @Mock
  private InstallationInclusionPersistenceService installationInclusionPersistenceService;

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

    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.of(nominatedInstallationDetail));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

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

    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.of(nominatedInstallationDetail));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

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

    var installationA = new InstallationDto(1, "Installation A");
    var installationB = new InstallationDto(2, "Installation B");

    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    when(nominatedInstallationPersistenceService.findAllByNominationDetail(nominationDetail))
        .thenReturn(List.of(nominatedInstallationB, nominatedInstallationA));

    when(installationQueryService.getInstallationsByIdIn(List.of(2, 1)))
        .thenReturn(List.of(installationB, installationA));

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

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

    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    when(nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

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

    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(installationInclusion));

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

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
    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

    assertThat(result).isEqualTo(new InstallationSummaryView(null));
  }

  @Test
  void getInstallationSummaryView_whenNoAnswer_andSectionIsNotSubmittable_thenAssertFields() {
    when(installationInclusionPersistenceService.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = installationSummaryService.getInstallationSummaryView(nominationDetail);

    assertThat(result)
        .isEqualTo(new InstallationSummaryView(SummarySectionError.createWithDefaultMessage("installations")));
  }

}