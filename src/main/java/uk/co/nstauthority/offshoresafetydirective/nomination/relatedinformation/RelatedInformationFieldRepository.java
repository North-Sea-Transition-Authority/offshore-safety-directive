package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface RelatedInformationFieldRepository extends CrudRepository<RelatedInformationField, Integer> {

  List<RelatedInformationField> findAllByRelatedInformation(RelatedInformation relatedInformation);

  void deleteAllByRelatedInformation(RelatedInformation relatedInformation);

}
