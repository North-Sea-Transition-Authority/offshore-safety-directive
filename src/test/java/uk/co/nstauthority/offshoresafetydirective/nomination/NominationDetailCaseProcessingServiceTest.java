package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeaderDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@ExtendWith(MockitoExtension.class)
class NominationDetailCaseProcessingServiceTest {

  @Mock
  private NominationDetailCaseProcessingRepository nominationDetailCaseProcessingRepository;

  @InjectMocks
  private NominationDetailCaseProcessingService nominationDetailCaseProcessingService;

  @Test
  void findCaseProcessingHeaderDto_whenExists_thenReturnsDto() {
    var dto = new NominationCaseProcessingHeaderDto(
        "nomination/ref",
        100,
        200,
        WellSelectionType.NO_WELLS,
        true,
        NominationStatus.DRAFT
    );
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(nominationDetailCaseProcessingRepository.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.of(dto));

    var result = nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail);
    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void findCaseProcessingHeaderDto_whenDoesNotExist_thenReturnsEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(nominationDetailCaseProcessingRepository.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.empty());

    var result = nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail);
    assertThat(result).isEmpty();
  }
}