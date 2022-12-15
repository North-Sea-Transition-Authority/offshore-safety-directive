package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeaderDto;

@Repository
interface NominationDetailCaseProcessingRepository extends CrudRepository<NominationDetail, Integer> {

  @Query("""
      SELECT new uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeaderDto(
        nd.nomination.reference
      , ap.portalOrganisationId
      , nom.nominatedOrganisationId
      , wss.selectionType
      , ii.includeInstallationsInNomination
      , nd.status
      ) 
      FROM NominationDetail nd
      JOIN ApplicantDetail ap ON ap.nominationDetail.id = nd.id
      JOIN NomineeDetail nom ON nom.nominationDetail.id = nd.id
      JOIN WellSelectionSetup wss ON wss.nominationDetail.id = nd.id
      JOIN InstallationInclusion ii ON ii.nominationDetail.id = nd.id
      WHERE nd = :nominationDetail
        """)
  Optional<NominationCaseProcessingHeaderDto> findCaseProcessingHeaderDto(NominationDetail nominationDetail);
}
