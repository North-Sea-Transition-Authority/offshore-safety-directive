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
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
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
    var subareaId1 = 1;
    var subareaId2 = 2;
    var blockSubareaDto1 = new LicenceBlockSubareaDto(subareaId1, "well1", "1");
    var blockSubareaDto2 = new LicenceBlockSubareaDto(subareaId2, "well2", "2");
    var formWithDuplicateSubarea = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder()
        .withSubareas(List.of(subareaId1, subareaId2, subareaId2))
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(List.of(subareaId1, subareaId2)))
        .thenReturn(List.of(blockSubareaDto1, blockSubareaDto2));

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
        tuple(blockSubareaDto1.id(), NOMINATION_DETAIL),
        tuple(blockSubareaDto2.id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void findAllByNominationDetail_verifyMethodCall() {
    nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaRepository, times(1)).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}