package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreRegistrationNumber;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinalisedNominatedSubareaWellsAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@ExtendWith(MockitoExtension.class)
class WellSummaryServiceTest {

  @Mock
  private WellSelectionSetupViewService wellSelectionSetupViewService;

  @Mock
  private NominatedWellDetailViewService nominatedWellDetailViewService;

  @Mock
  private NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @Mock
  private FinalisedNominatedSubareaWellsAccessService finalisedNominatedSubareaWellsAccessService;

  @Mock
  private WellQueryService wellQueryService;

  @Mock
  private WellSubmissionService wellSubmissionService;

  @InjectMocks
  private WellSummaryService wellSummaryService;

  @Test
  void getWellSummaryView_whenNoWellData_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.empty());

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept("summarySectionError");

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(nominatedBlockSubareaDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenNoWellSelectionType_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.NO_WELLS)));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(WellSummaryView::getWellSelectionType)
        .isEqualTo(WellSelectionType.NO_WELLS);

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept("wellSelectionType", "summarySectionError");

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(nominatedBlockSubareaDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenSpecificWellSelectionTypeAndSpecificWellsSaved_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.SPECIFIC_WELLS)));

    var expectedWell = WellDtoTestUtil.builder().build();

    var specificWellSummaryView = NominatedWellDetailViewTestUtil.builder()
        .withWellDto(expectedWell)
        .withIsNominationForAllWellPhases(false)
        .withWellPhase(WellPhase.EXPLORATION_AND_APPRAISAL)
        .build();

    given(nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail))
        .willReturn(Optional.of(specificWellSummaryView));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getWells(),
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getIsNominationForAllWellPhases(),
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getWellPhases()
        )
        .containsExactly(
            WellSelectionType.SPECIFIC_WELLS,
            List.of(expectedWell),
            false,
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL)
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "specificWellSummaryView",
            "summarySectionError"
        );

    then(nominatedBlockSubareaDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenSpecificWellSelectionTypeAndNoSpecificWellsSaved_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.SPECIFIC_WELLS)));

    given(nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail))
        .willReturn(Optional.empty());

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getWells(),
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getIsNominationForAllWellPhases(),
            wellSummaryView -> wellSummaryView.getSpecificWellSummaryView().getWellPhases()
        )
        .containsExactly(
            WellSelectionType.SPECIFIC_WELLS,
            Collections.emptyList(),
            null,
            Collections.emptyList()
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "specificWellSummaryView",
            "summarySectionError"
        );

    then(nominatedBlockSubareaDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenNoSubareaDataSaved_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.empty());

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getLicenceBlockSubareas(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getValidForFutureWellsInSubarea(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getForAllWellPhases(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getWellPhases(),
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().hasWellsToExclude(),
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().excludedWells(),
            WellSummaryView::getSubareaWellsIncludedOnNomination
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenSubareaDataSaved_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        List.of(expectedSubarea),
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    // and we have saved some subarea data
    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    var expectedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100));

    var expectedWell = WellDtoTestUtil.builder()
        .withWellboreId(expectedSubareaWell.wellboreId().id())
        .build();

    // and the subarea selected contains a wellbore
    given(finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(nominationDetail))
        .willReturn(Set.of(expectedSubareaWell));

    given(wellQueryService.getWellsByIds(List.of(expectedSubareaWell.wellboreId()), WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE))
        .willReturn(List.of(expectedWell));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then the subarea properties and list of included wellbores will be populated
    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getLicenceBlockSubareas(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getValidForFutureWellsInSubarea(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getForAllWellPhases(),
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getWellPhases(),
            WellSummaryView::getSubareaWellsIncludedOnNomination
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            List.of(expectedSubarea),
            expectedSubareaSummary.getValidForFutureWellsInSubarea(),
            expectedSubareaSummary.getForAllWellPhases(),
            List.of(WellPhase.DEVELOPMENT),
            List.of(expectedWell)
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenSubareaDataSavedAndNoSubareaSelected_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    // and we have not provided a subarea
    List<LicenceBlockSubareaDto> selectedSubareas = Collections.emptyList();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        selectedSubareas,
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then subarea list in the view is empty
    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getSubareaWellSummaryView().getLicenceBlockSubareas()
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            Collections.emptyList()
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();

    then(finalisedNominatedSubareaWellsAccessService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenHasWellsToExcludeNull_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        List.of(expectedSubarea),
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    // and we have saved some subarea data
    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    // and we haven't answered if we are excluding wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(null);

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then the excluded wells properties are blank
    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().hasWellsToExclude(),
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().excludedWells()
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            null,
            Collections.emptyList()
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .should(never())
        .getExcludedWellIds(nominationDetail);
  }

  @Test
  void getWellSummaryView_whenHasWellsToExcludeTrue_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        List.of(expectedSubarea),
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    // and we have saved some subarea data
    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(true);

    var excludedWell = WellDtoTestUtil.builder()
        .withWellboreId(200)
        .build();

    // and a well to exclude has been provided
    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(Set.of(excludedWell.wellboreId()));

    given(wellQueryService.getWellsByIds(List.of(excludedWell.wellboreId()), WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE))
        .willReturn(List.of(excludedWell));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then the excluded wells properties are blank
    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().hasWellsToExclude(),
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().excludedWells()
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            true,
            List.of(new WellboreRegistrationNumber(excludedWell.name()))
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenHasWellsToExcludeFalse_thenAssertResultingView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        List.of(expectedSubarea),
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    // and we have saved some subarea data
    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    // and we have said No to needing to exclude wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(false);

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then the excluded wells properties are blank
    assertThat(resultingWellSummaryView)
        .extracting(
            WellSummaryView::getWellSelectionType,
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().hasWellsToExclude(),
            wellSummaryView -> wellSummaryView.getExcludedWellSummaryView().excludedWells()
        )
        .containsExactly(
            WellSelectionType.LICENCE_BLOCK_SUBAREA,
            false,
            Collections.emptyList()
        );

    assertThat(resultingWellSummaryView)
        .hasAllNullFieldsOrPropertiesExcept(
            "wellSelectionType",
            "subareaWellSummaryView",
            "excludedWellSummaryView",
            "subareaWellsIncludedOnNomination",
            "summarySectionError"
        );

    then(nominatedWellDetailViewService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .should(never())
        .getExcludedWellIds(nominationDetail);
  }

  @Test
  void getWellSummaryView_whenWellsToExcludeAndWellsInSubarea_thenVerifyOneRequestToApi() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we have selected licence block subarea as a journey
    given(wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail))
        .willReturn(Optional.of(new WellSelectionSetupView(WellSelectionType.LICENCE_BLOCK_SUBAREA)));

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var expectedSubareaSummary = new NominatedBlockSubareaDetailView(
        List.of(expectedSubarea),
        true,
        false,
        List.of(WellPhase.DEVELOPMENT)
    );

    // and we have saved some subarea data
    given(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .willReturn(Optional.of(expectedSubareaSummary));

    var expectedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100));

    var expectedWell = WellDtoTestUtil.builder()
        .withWellboreId(expectedSubareaWell.wellboreId().id())
        .build();

    // and the subarea selected contains a wellbore
    given(finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(nominationDetail))
        .willReturn(Set.of(expectedSubareaWell));

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(true);

    var excludedWell = WellDtoTestUtil.builder()
        .withWellboreId(200)
        .build();

    // and a well to exclude has been provided
    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(Set.of(excludedWell.wellboreId()));

    wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then we make one request to the energy portal api for
    // excluded and included wells
    then(wellQueryService)
        .should(onlyOnce())
        .getWellsByIds(
            List.of(expectedWell.wellboreId(), excludedWell.wellboreId()),
            WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE);
  }

  @Test
  void getWellSummaryView_whenSectionIsInError_thenSectionError() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSubmissionService.isSectionSubmittable(nominationDetail))
        .willReturn(false);

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(wellSummaryView -> wellSummaryView.getSummarySectionError().errorMessage())
        .isEqualTo("There are problems with the wells section. Return to the task list to fix the problems.");
  }

  @Test
  void getWellSummaryView_whenSectionIsNotInError_thenSectionErrorIsNull() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSubmissionService.isSectionSubmittable(nominationDetail))
        .willReturn(true);

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(WellSummaryView::getSummarySectionError)
        .isNull();
  }

  @Test
  void getWellSummaryView_whenValidationBehaviourIsNoValidation_thenVerifyInteraction() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.NOT_VALIDATED
    );

    assertThat(resultingWellSummaryView)
        .extracting(WellSummaryView::getSummarySectionError)
        .isNull();

    then(wellSubmissionService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getWellSummaryView_whenValidationBehaviourIsValidation_thenVerifyInteraction() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    then(wellSubmissionService)
        .should(onlyOnce())
        .isSectionSubmittable(nominationDetail);
  }

}