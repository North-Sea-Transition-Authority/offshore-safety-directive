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

    var pearsRelated = true;
    var pearsReferences = "pears/ref/1";
    var wonsRelated = true;
    var wonsReferences = "wons/ref/1";

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToLicenceApplications(pearsRelated)
        .withRelatedLicenceApplications(pearsReferences)
        .withRelatedToWellApplications(wonsRelated)
        .withRelatedWellApplications(wonsReferences)
        .build();
    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var result = relatedInformationAccessService.getRelatedInformationDto(nominationDetail);

    assertThat(result).get()
        .extracting(
            dto -> dto.relatedToPearsApplications().related(),
            dto -> dto.relatedToPearsApplications().applications(),
            dto -> dto.relatedToWonsApplications().related(),
            dto -> dto.relatedToWonsApplications().applications()
        )
        .containsExactly(
            pearsRelated,
            pearsReferences,
            wonsRelated,
            wonsReferences
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