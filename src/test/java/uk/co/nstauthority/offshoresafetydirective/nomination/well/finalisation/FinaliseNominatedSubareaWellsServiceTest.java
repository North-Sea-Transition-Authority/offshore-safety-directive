package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsService;

@ExtendWith(MockitoExtension.class)
class FinaliseNominatedSubareaWellsServiceTest {

  @Mock
  private NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService;

  @Mock
  private WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @Mock
  private NominatedSubareaWellsService nominatedSubareaWellsService;

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

    then(nominatedSubareaWellsService)
        .shouldHaveNoInteractions();
  }

  @ParameterizedTest(name = "{index} => WellSelectionType=''{0}''")
  @EnumSource(value = WellSelectionType.class, names = "LICENCE_BLOCK_SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void finaliseNominationWellbores_whenWellSelectTypeIsNotSubarea_thenNoWellboresSaved(
      WellSelectionType wellSelectionType
  ) {

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

    then(nominatedSubareaWellsService)
        .shouldHaveNoInteractions();
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndNoWellboresReturned_thenNoWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    given(nominatedSubareaWellsService.determineNominatedSubareaWellbores(nominationDetail))
        .willReturn(Collections.emptySet());

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(never())
        .materialiseNominatedSubareaWells(eq(nominationDetail), anyCollection());
  }

  @Test
  void finaliseNominationWellbores_whenWellSelectTypeIsSubareaWellsAndWellboresReturned_thenWellboresSaved() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .willReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var expectedNominatedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100), "subarea name");

    given(nominatedSubareaWellsService.determineNominatedSubareaWellbores(nominationDetail))
        .willReturn(Set.of(expectedNominatedSubareaWell));

    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(atMostOnce())
        .deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellPersistenceService)
        .should(onlyOnce())
        .materialiseNominatedSubareaWells(
            nominationDetail,
            Set.of(
                new NominatedSubareaWellDto(expectedNominatedSubareaWell.wellboreId(), "subarea name")
            )
        );
  }
}