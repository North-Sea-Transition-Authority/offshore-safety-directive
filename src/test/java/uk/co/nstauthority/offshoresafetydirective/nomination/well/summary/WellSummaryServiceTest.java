package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.groups.Tuple;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellTestUtil;
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
    var expectedWellSummaryView = WellSummaryItemView.fromWellDto(expectedWell);

    var specificWellSummaryView = NominatedWellDetailViewTestUtil.builder()
        .withWellSummaryItemView(expectedWellSummaryView)
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
            List.of(expectedWellSummaryView),
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

    var expectedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100), "subarea name");

    var expectedWell = WellDtoTestUtil.builder()
        .withWellboreId(expectedSubareaWell.wellboreId().id())
        .build();

    // and the subarea selected contains a wellbore
    given(finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(nominationDetail))
        .willReturn(Set.of(expectedSubareaWell));

    given(wellQueryService.getWellsByIds(List.of(expectedSubareaWell.wellboreId()),
        WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE))
        .willReturn(List.of(expectedWell));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    var expectedWellSummaryView = WellSummaryItemView.fromWellDto(expectedWell);

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
            List.of(expectedWellSummaryView)
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
  void getWellSummaryView_whenHasWellsToExcludeTrue_andWellNoLongerExistsInPortal_andIsNotCached_thenWellIsNotInSummary() {

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

    var excludedWellInPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(200)
        .build();
    var excludedWellDtoInPortal = WellDtoTestUtil.builder()
        .withWellboreId(excludedWellInPortal.getWellboreId())
        .build();

    var excludedWellNotInPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(201)
        .build();
    var excludedWellDtoNotInPortal = WellDtoTestUtil.builder()
        .withWellboreId(excludedWellNotInPortal.getWellboreId())
        .build();

    // and a well to exclude has been provided
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(excludedWellInPortal, excludedWellNotInPortal));

    given(wellQueryService.getWellsByIds(
        List.of(excludedWellDtoInPortal.wellboreId(), excludedWellDtoNotInPortal.wellboreId()),
        WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE
    ))
        .willReturn(List.of(excludedWellDtoInPortal));

    when(finalisedNominatedSubareaWellsAccessService
        .getFinalisedNominatedSubareasWells(nominationDetail))
        .thenReturn(Set.of());

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
            List.of(new WellboreRegistrationNumber(excludedWellDtoInPortal.name()))
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
  void getWellSummaryView_whenWellNoLongerExistsInPortal_andIsCached_thenVerifyCachedSubareaUsed() {

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

    var excludedWellInPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(200)
        .build();
    var excludedWellDtoInPortal = WellDtoTestUtil.builder()
        .withWellboreId(excludedWellInPortal.getWellboreId())
        .build();

    var excludedWellNotInPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(201)
        .build();
    var excludedWellDtoNotInPortal = WellDtoTestUtil.builder()
        .withWellboreId(excludedWellNotInPortal.getWellboreId())
        .build();

    var cachedWell = new NominatedSubareaWellDto(
        new WellboreId(999),
        "cached name"
    );
    when(finalisedNominatedSubareaWellsAccessService
        .getFinalisedNominatedSubareasWells(nominationDetail))
        .thenReturn(Set.of(cachedWell));

    // and a well to exclude has been provided
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(excludedWellInPortal, excludedWellNotInPortal));

    given(wellQueryService.getWellsByIds(
        List.of(cachedWell.wellboreId(), excludedWellDtoInPortal.wellboreId(), excludedWellDtoNotInPortal.wellboreId()),
        WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE
    ))
        .willReturn(List.of(excludedWellDtoInPortal));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then the excluded wells properties are blank
    assertThat(resultingWellSummaryView.getSubareaWellsIncludedOnNomination())
        .extracting(
            WellSummaryItemView::name,
            WellSummaryItemView::isOnPortal
        )
        .containsExactly(
            Tuple.tuple(
                "cached name",
                false
            )
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

    var expectedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100), "subarea name");

    var expectedWell = WellDtoTestUtil.builder()
        .withWellboreId(expectedSubareaWell.wellboreId().id())
        .build();

    // and the subarea selected contains a wellbore
    given(finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(nominationDetail))
        .willReturn(Set.of(expectedSubareaWell));

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(true);

    var excludedWellId = 200;
    var excludedWell = ExcludedWellTestUtil.builder()
        .withWellboreId(excludedWellId)
        .build();

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(excludedWell));

    var excludedWellDto = WellDtoTestUtil.builder()
        .withWellboreId(200)
        .build();

    wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then we make one request to the energy portal api for
    // excluded and included wells
    then(wellQueryService)
        .should(onlyOnce())
        .getWellsByIds(
            List.of(expectedWell.wellboreId(), excludedWellDto.wellboreId()),
            WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE);
  }

  @Test
  void getWellSummaryView_whenWellsToExcludeAndWellsInSubarea_andMixedSource_thenVerifyOrder() {

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

    var expectedSubareaWellOnPortal = new NominatedSubareaWellDto(new WellboreId(100), "portal subarea");
    var expectedSubareaWellNotOnPortal = new NominatedSubareaWellDto(new WellboreId(101), "removed subarea");

    var expectedWell = WellDtoTestUtil.builder()
        .withWellboreId(expectedSubareaWellOnPortal.wellboreId().id())
        .withRegistrationNumber(expectedSubareaWellOnPortal.name())
        .build();

    // and the subarea selected contains a wellbore
    var finalisedNominatedSubareaWells = List.of(expectedSubareaWellOnPortal, expectedSubareaWellNotOnPortal);
    given(finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(nominationDetail))
        .willReturn(new LinkedHashSet<>(finalisedNominatedSubareaWells));

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.hasWellsToExclude(nominationDetail))
        .willReturn(true);

    var excludedWellOnPortalId = new WellboreId(200);
    var excludedWellOnPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(excludedWellOnPortalId.id())
        .build();

    var excludedWellNotOnPortalId = new WellboreId(201);
    var excludedWellNotOnPortal = ExcludedWellTestUtil.builder()
        .withWellboreId(excludedWellNotOnPortalId.id())
        .build();

    // and we have said Yes to needing to exclude wells
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(excludedWellOnPortal, excludedWellNotOnPortal));

    var excludedWellDto = WellDtoTestUtil.builder()
        .withWellboreId(excludedWellOnPortalId.id())
        .build();

    given(wellQueryService.getWellsByIds(
        List.of(
            expectedSubareaWellOnPortal.wellboreId(),
            expectedSubareaWellNotOnPortal.wellboreId(),
            excludedWellOnPortalId,
            excludedWellNotOnPortalId
        ),
        WellSummaryService.WELLS_RELATED_TO_NOMINATION_PURPOSE
    ))
        .willReturn(List.of(expectedWell, excludedWellDto));

    var resultingWellSummaryView = wellSummaryService.getWellSummaryView(
        nominationDetail,
        SummaryValidationBehaviour.VALIDATED
    );

    // then verify the nominated wells
    assertThat(resultingWellSummaryView.getSubareaWellsIncludedOnNomination())
        .extracting(
            WellSummaryItemView::name,
            WellSummaryItemView::isOnPortal
        )
        .containsExactly(
            Tuple.tuple(
                "removed subarea",
                false
            ),
            Tuple.tuple(
                "portal subarea",
                true
            )
        );

    // then verify the excluded wells
    assertThat(resultingWellSummaryView.getExcludedWellSummaryView().excludedWells())
        .containsExactly(
            new WellboreRegistrationNumber(excludedWellDto.name())
        );

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