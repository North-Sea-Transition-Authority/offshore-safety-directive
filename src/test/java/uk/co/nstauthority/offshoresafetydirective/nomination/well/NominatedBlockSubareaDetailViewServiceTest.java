package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Mock
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  @Test
  void getNominatedBlockSubareaDetailView_whenEntityExists_assertFields() {

    var nominatedBlockSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .withDecommissioningPhase(true)
        .build();

    var nominatedBlockSubarea = new NominatedBlockSubarea();
    nominatedBlockSubarea.setBlockSubareaId("1");

    var expectedLicenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(nominatedBlockSubarea.getBlockSubareaId())
        .build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedBlockSubarea));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(new LicenceBlockSubareaId(nominatedBlockSubarea.getBlockSubareaId())),
        NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE)
    )
        .thenReturn(List.of(expectedLicenceBlockSubareaDto));

    var nominatedBlockSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertTrue(nominatedBlockSubareaDetailView.isPresent());
    assertThat(nominatedBlockSubareaDetailView.get())
        .extracting(
            NominatedBlockSubareaDetailView::getLicenceBlockSubareas,
            NominatedBlockSubareaDetailView::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaDetailView::getForAllWellPhases,
            NominatedBlockSubareaDetailView::getWellPhases
        )
        .containsExactly(
            List.of(expectedLicenceBlockSubareaDto),
            nominatedBlockSubareaDetail.getValidForFutureWellsInSubarea(),
            nominatedBlockSubareaDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING)
        );

    assertThat(nominatedBlockSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(LicenceBlockSubareaDto::subareaId)
        .containsExactly(expectedLicenceBlockSubareaDto.subareaId());
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenEntityDoesNotExist_assertEmptyOptional() {
    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var nominatedBlockSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertTrue(nominatedBlockSubareaDetailView.isEmpty());
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenMultipleSubareas_whenSameBlockAndSubareaName_thenSortedByLicenceComponents() {

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .withSubareaId("first")
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .withSubareaId("second")
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withSubareaId("third")
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .withSubareaId("fourth")
        .build();

    var unsortedSubareaList = List.of(
        fourthSubareaByLicence,
        thirdSubareaByLicence,
        secondSubareaByLicence,
        firstSubareaByLicence
    );

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            firstSubareaByLicence.subareaId(),
            secondSubareaByLicence.subareaId(),
            thirdSubareaByLicence.subareaId(),
            fourthSubareaByLicence.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(unsortedSubareaList);

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByLicence.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByLicence.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(thirdSubareaByLicence.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(fourthSubareaByLicence.subareaId().id())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(LicenceBlockSubareaDto::subareaId)
        .containsExactly(
            firstSubareaByLicence.subareaId(),
            secondSubareaByLicence.subareaId(),
            thirdSubareaByLicence.subareaId(),
            fourthSubareaByLicence.subareaId()
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenMultipleSubareas_whenSameLicenceAndSubareaName_thenSortedByLicenceBlockComponents() {

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .withSubareaId("first")
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .withSubareaId("second")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .withSubareaId("third")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .withSubareaId("fourth")
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("10")
        .withSubareaId("fifth")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("2")
        .withSubareaId("sixth")
        .build();

    var unsortedSubareaList = List.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            firstSubareaByBlock.subareaId(),
            secondSubareaByBlock.subareaId(),
            thirdSubareaByBlock.subareaId(),
            fourthSubareaByBlock.subareaId(),
            fifthSubareaByBlock.subareaId(),
            sixthSubareaByBlock.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(unsortedSubareaList);

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByBlock.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByBlock.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(thirdSubareaByBlock.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(fourthSubareaByBlock.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(fifthSubareaByBlock.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(sixthSubareaByBlock.subareaId().id())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(LicenceBlockSubareaDto::subareaId)
        .containsExactly(
            firstSubareaByBlock.subareaId(),
            secondSubareaByBlock.subareaId(),
            thirdSubareaByBlock.subareaId(),
            fourthSubareaByBlock.subareaId(),
            fifthSubareaByBlock.subareaId(),
            sixthSubareaByBlock.subareaId()
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenSubareasAreNotOnPortal_thenAssertPropertiesAndOrder() {
    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("first")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("b name")
        .withSubareaId("second")
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            secondSubareaByName.subareaId(),
            firstSubareaByName.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(List.of());

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByName.subareaId().id())
                .withName(secondSubareaByName.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByName.subareaId().id())
                .withName(firstSubareaByName.subareaName().value())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(
            LicenceBlockSubareaDto::subareaId,
            dto -> dto.subareaName().value(),
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            Tuple.tuple(
                firstSubareaByName.subareaId(),
                firstSubareaByName.subareaName().value(),
                false
            ),
            Tuple.tuple(
                secondSubareaByName.subareaId(),
                secondSubareaByName.subareaName().value(),
                false
            )
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_verifyOrderOfMixedSourceSubareas() {
    var firstSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("first")
        .build();

    var secondSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("b name")
        .withSubareaId("second")
        .build();

    var firstSubareaByNameOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("third")
        .build();

    var secondSubareaByNameOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("b name")
        .withSubareaId("fourth")
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            secondSubareaByNameOnPortal.subareaId(),
            firstSubareaByNameOnPortal.subareaId(),
            secondSubareaByNameNotOnPortal.subareaId(),
            firstSubareaByNameNotOnPortal.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(List.of(secondSubareaByNameOnPortal, firstSubareaByNameOnPortal));

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByNameOnPortal.subareaId().id())
                .withName(secondSubareaByNameOnPortal.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByNameOnPortal.subareaId().id())
                .withName(firstSubareaByNameOnPortal.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByNameNotOnPortal.subareaId().id())
                .withName(secondSubareaByNameNotOnPortal.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByNameNotOnPortal.subareaId().id())
                .withName(firstSubareaByNameNotOnPortal.subareaName().value())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(
            LicenceBlockSubareaDto::subareaId,
            dto -> dto.subareaName().value(),
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            Tuple.tuple(
                firstSubareaByNameNotOnPortal.subareaId(),
                firstSubareaByNameNotOnPortal.subareaName().value(),
                false
            ),
            Tuple.tuple(
                secondSubareaByNameNotOnPortal.subareaId(),
                secondSubareaByNameNotOnPortal.subareaName().value(),
                false
            ),
            Tuple.tuple(
                firstSubareaByNameOnPortal.subareaId(),
                firstSubareaByNameOnPortal.subareaName().value(),
                true
            ),
            Tuple.tuple(
                secondSubareaByNameOnPortal.subareaId(),
                secondSubareaByNameOnPortal.subareaName().value(),
                true
            )
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenMultipleSubareas_whenSameLicenceAndBlock_thenSortedBySubareaName() {

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("first")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("second")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .withSubareaId("third")
        .build();

    var unsortedSubareaList = List.of(
        thirdSubareaByName,
        secondSubareaByName,
        firstSubareaByName
    );

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            firstSubareaByName.subareaId(),
            secondSubareaByName.subareaId(),
            thirdSubareaByName.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(unsortedSubareaList);

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByName.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByName.subareaId().id())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(thirdSubareaByName.subareaId().id())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(LicenceBlockSubareaDto::subareaId)
        .containsExactly(
            firstSubareaByName.subareaId(),
            secondSubareaByName.subareaId(),
            thirdSubareaByName.subareaId()
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenSubareasNotOnPortal_verifyOrderedByName_andOrderIsCaseInsensitive() {
    var firstSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("first")
        .build();

    var secondSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("second")
        .build();

    var thirdSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .withSubareaId("third")
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            secondSubareaByNameNotOnPortal.subareaId(),
            thirdSubareaByNameNotOnPortal.subareaId(),
            firstSubareaByNameNotOnPortal.subareaId()
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(List.of());

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(secondSubareaByNameNotOnPortal.subareaId().id())
                .withName(secondSubareaByNameNotOnPortal.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(thirdSubareaByNameNotOnPortal.subareaId().id())
                .withName(thirdSubareaByNameNotOnPortal.subareaName().value())
                .build(),
            NominatedBlockSubareaTestUtil.builder()
                .withBlockSubareaId(firstSubareaByNameNotOnPortal.subareaId().id())
                .withName(firstSubareaByNameNotOnPortal.subareaName().value())
                .build()
        ));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(
            LicenceBlockSubareaDto::subareaId,
            dto -> dto.subareaName().value(),
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            Tuple.tuple(
                firstSubareaByNameNotOnPortal.subareaId(),
                firstSubareaByNameNotOnPortal.subareaName().value(),
                false
            ),
            Tuple.tuple(
                secondSubareaByNameNotOnPortal.subareaId(),
                secondSubareaByNameNotOnPortal.subareaName().value(),
                false
            ),
            Tuple.tuple(
                thirdSubareaByNameNotOnPortal.subareaId(),
                thirdSubareaByNameNotOnPortal.subareaName().value(),
                false
            )
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenSubareasNotOnPortal_verifyCachedNameFallback() {
    var expectedPortalName = "portal name 1";
    var firstSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName(expectedPortalName)
        .withSubareaId("first")
        .isExtant(true)
        .build();

    var secondSubareaByNameNotOnPortal = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("portal name 2")
        .withSubareaId("second")
        .isExtant(false)
        .build();

    var firstNominatedBlockSubarea = NominatedBlockSubareaTestUtil.builder()
        .withId(UUID.randomUUID())
        .withBlockSubareaId(firstSubareaByNameNotOnPortal.subareaId().id())
        .withName("cached name 1")
        .build();

    var expectedCachedName = "cached name 2";
    var secondNominatedBlockSubarea = NominatedBlockSubareaTestUtil.builder()
        .withId(UUID.randomUUID())
        .withBlockSubareaId(secondSubareaByNameNotOnPortal.subareaId().id())
        .withName(expectedCachedName)
        .build();

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(secondNominatedBlockSubarea, firstNominatedBlockSubarea));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
            new LicenceBlockSubareaId(secondNominatedBlockSubarea.getBlockSubareaId()),
            new LicenceBlockSubareaId(firstNominatedBlockSubarea.getBlockSubareaId())
        ), NominatedBlockSubareaDetailViewService.NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
    )).thenReturn(List.of(secondSubareaByNameNotOnPortal, firstSubareaByNameNotOnPortal));

    var nominatedBlockSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder().build();
    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    var resultingSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(
        NOMINATION_DETAIL);

    assertThat(resultingSubareaDetailView).isPresent();
    assertThat(resultingSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(
            LicenceBlockSubareaDto::subareaId,
            dto -> dto.subareaName().value(),
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            Tuple.tuple(
                secondSubareaByNameNotOnPortal.subareaId(),
                expectedCachedName,
                false
            ),
            Tuple.tuple(
                firstSubareaByNameNotOnPortal.subareaId(),
                expectedPortalName,
                true
            )
        );
  }
}