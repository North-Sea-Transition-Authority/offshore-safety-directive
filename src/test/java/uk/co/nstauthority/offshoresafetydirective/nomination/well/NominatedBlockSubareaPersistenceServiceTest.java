package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedBlockSubareaRepository nominatedBlockSubareaRepository;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @Test
  void saveNominatedLicenceBlockSubareas_whenFormHasDuplicateSubareas_verifyNoDuplicateSubareasSaved() {

    var firstSubareaId = new LicenceBlockSubareaId("1");
    var secondSubareaId = new LicenceBlockSubareaId("2");

    var firstSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstSubareaId.id())
        .build();

    var secondSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(secondSubareaId.id())
        .build();

    var formWithDuplicateSubarea = NominatedBlockSubareaFormTestUtil.builder()
        .withSubareas(List.of(firstSubareaId.id(), secondSubareaId.id(), secondSubareaId.id()))
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(List.of(firstSubareaId, secondSubareaId)))
        .thenReturn(List.of(firstSubareaDto, secondSubareaDto));

    nominatedBlockSubareaPersistenceService.saveNominatedLicenceBlockSubareas(NOMINATION_DETAIL, formWithDuplicateSubarea);

    verify(nominatedBlockSubareaRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedBlockSubarea>> wellArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(nominatedBlockSubareaRepository, times(1)).saveAll(wellArgumentCaptor.capture());

    var savedWells = wellArgumentCaptor.getValue();
    assertThat(savedWells).extracting(
        NominatedBlockSubarea::getBlockSubareaId,
        NominatedBlockSubarea::getNominationDetail
    ).containsExactly(
        tuple(firstSubareaId.id(), NOMINATION_DETAIL),
        tuple(secondSubareaId.id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void findAllByNominationDetail_verifyMethodCall() {
    nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaRepository, times(1)).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}