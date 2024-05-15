package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class RelatedInformationAccessService {

  private final RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Autowired
  public RelatedInformationAccessService(RelatedInformationPersistenceService relatedInformationPersistenceService) {
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
  }

  public Optional<RelatedInformationDto> getRelatedInformationDto(NominationDetail nominationDetail) {
    return relatedInformationPersistenceService.getRelatedInformation(nominationDetail)
        .map(this::createRelatedInformationDto);
  }

  private RelatedInformationDto createRelatedInformationDto(RelatedInformation relatedInformation) {
    var relatedPears = new RelatedToPearsApplications(
        relatedInformation.getRelatedToLicenceApplications(),
        relatedInformation.getRelatedLicenceApplications()
    );
    var relatedWons = new RelatedToWonsApplications(
        relatedInformation.getRelatedToWellApplications(),
        relatedInformation.getRelatedWellApplications()
    );
    return new RelatedInformationDto(relatedPears, relatedWons);
  }

}
