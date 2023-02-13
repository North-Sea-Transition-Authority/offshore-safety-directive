package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationAccessServiceTest {

  @Mock
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @InjectMocks
  private RelatedInformationAccessService relatedInformationAccessService;

  @Test
  void getRelatedInformationDto_whenPresent_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var relatedInformation = RelatedInformationTestUtil.builder().build();
    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var expectedDto = RelatedInformationDtoTestUtil.builder()
        .withRelatedToPearsApplications(new RelatedToPearsApplications(
           relatedInformation.getRelatedToLicenceApplications(),
           relatedInformation.getRelatedLicenceApplications()
        ))
        .build();

    var result = relatedInformationAccessService.getRelatedInformationDto(nominationDetail);

    assertThat(result).get()
        .extracting(RelatedInformationDto::relatedToPearsApplications)
        .extracting(
            RelatedToPearsApplications::related,
            RelatedToPearsApplications::applications
        )
        .containsExactly(
            expectedDto.relatedToPearsApplications().related(),
            expectedDto.relatedToPearsApplications().applications()
        );
  }

  @Test
  void getRelatedInformationDto_whenNotPresent_assertEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.empty());

    var result = relatedInformationAccessService.getRelatedInformationDto(nominationDetail);

    assertThat(result).isEmpty();
  }
}