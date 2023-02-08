package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;

@ExtendWith(MockitoExtension.class)
class FinaliseNominatedSubareaWellsServiceTest {

  @Mock
  private NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService;

  @Mock
  private WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @Mock
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Mock
  private LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @InjectMocks
  private FinaliseNominatedSubareaWellsService finaliseNominatedSubareaWellsService;

  @Test
  void finaliseNominationWellbores_whenNoWellboreSelectionType_thenNoWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.empty());

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(never())
        .materialiseNominatedSubareaWells(eq(nominationDetail), anyCollection());

    then(nominatedBlockSubareaAccessService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaWellboreService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest(name = "{index} => WellSelectionType=''{0}''")
  @EnumSource(value = WellSelectionType.class, names = "LICENCE_BLOCK_SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void finaliseNominationWellbores_whenWellSelectTypeIsNotSubarea_thenNoWellboresSaved(WellSelectionType wellSelectionType) {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(wellSelectionType));

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(never())
        .materialiseNominatedSubareaWells(eq(nominationDetail), anyCollection());

    then(nominatedBlockSubareaAccessService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaWellboreService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndNoSubareasSelected_thenNoWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(Collections.emptyList());

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(never())
        .materialiseNominatedSubareaWells(eq(nominationDetail), anyCollection());

    then(licenceBlockSubareaWellboreService)
        .shouldHaveNoInteractions();

    then(excludedWellAccessService)
        .shouldHaveNoInteractions();
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndSubareasSelected_thenWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var expectedWellboreInSubarea = WellDtoTestUtil.builder().build();

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedNominatedSubarea.subareaId())))
        .willReturn(List.of(expectedWellboreInSubarea));

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(atMostOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .materialiseNominatedSubareaWells(
            nominationDetail,
            Set.of(
                new NominatedSubareaWellDto(expectedWellboreInSubarea.wellboreId())
            )
        );
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndWellboreExcluded_thenWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var firstExpectedWellboreInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    var secondExpectedWellboreInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(20)
        .build();

    // two wells come back from the subarea
    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedNominatedSubarea.subareaId())))
        .willReturn(List.of(firstExpectedWellboreInSubarea, secondExpectedWellboreInSubarea));

    // the first wellbore is in the exclusion list
    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(Set.of(firstExpectedWellboreInSubarea.wellboreId()));

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(atMostOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    // the first wellbore is not written to the table as it is excluded
    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .materialiseNominatedSubareaWells(
            nominationDetail,
            Set.of(
                new NominatedSubareaWellDto(secondExpectedWellboreInSubarea.wellboreId())
            )
        );
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndAllWellboresExcluded_thenNoWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var firstExpectedWellboreInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    var secondExpectedWellboreInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(20)
        .build();

    // two wells come back from the subarea
    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedNominatedSubarea.subareaId())))
        .willReturn(List.of(firstExpectedWellboreInSubarea, secondExpectedWellboreInSubarea));

    // both wellbores are in the exclusion list
    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(
            Set.of(
                firstExpectedWellboreInSubarea.wellboreId(),
                secondExpectedWellboreInSubarea.wellboreId()
            )
        );

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(atMostOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(never())
        .materialiseNominatedSubareaWells(eq(nominationDetail), anyCollection());
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndDuplicateWells_thenDistinctWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var expectedWellboreInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    // when the same well comes back from the related subareas method
    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(List.of(expectedNominatedSubarea.subareaId())))
        .willReturn(List.of(expectedWellboreInSubarea, expectedWellboreInSubarea));

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(atMostOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    // then only one instance of the wellbore is written to the final table
    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .materialiseNominatedSubareaWells(
            nominationDetail,
            Set.of(
                new NominatedSubareaWellDto(expectedWellboreInSubarea.wellboreId())
            )
        );
  }
}