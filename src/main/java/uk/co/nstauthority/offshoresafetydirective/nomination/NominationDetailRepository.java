package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NominationDetailRepository extends CrudRepository<NominationDetail, UUID> {

  Optional<NominationDetail> findFirstByNominationAndVersion(Nomination nomination, int version);

  Optional<NominationDetail> findFirstByNominationOrderByVersionDesc(Nomination nomination);

  Optional<NominationDetail> findFirstByNomination_IdOrderByVersionDesc(UUID nominationId);

  Optional<NominationDetail> findFirstByNomination_IdAndStatusInOrderByVersionDesc(
      UUID nominationId,
      Collection<NominationStatus> nominationStatuses
  );

  Optional<NominationDetail> findFirstByNomination_IdAndVersionAndStatusInOrderByVersionDesc(
      UUID nominationId,
      Integer version,
      Collection<NominationStatus> nominationStatuses
  );

  List<NominationDetail> findAllByNominationAndStatusIn(Nomination nomination, Collection<NominationStatus> statuses);

  List<NominationDetail> getNominationDetailsByStatusInAndNomination_ReferenceContainsIgnoreCase(
      Collection<NominationStatus> statuses,
      String reference
  );

  @Query(value = """
      SELECT new uk.co.nstauthority.offshoresafetydirective.nomination.NominationType(
        CASE WHEN wellSetup IS NOT NULL AND wellSetup.selectionType IS NOT NULL
          THEN true
          ELSE false
        END,
        CASE WHEN installationSetup IS NOT NULL AND installationSetup.includeInstallationsInNomination IS NOT NULL
          THEN installationSetup.includeInstallationsInNomination
          ELSE false
        END
      )
      FROM NominationDetail nominationDetail
      LEFT JOIN WellSelectionSetup wellSetup
        ON wellSetup.nominationDetail = nominationDetail
        AND wellSetup.selectionType != uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType.NO_WELLS
      LEFT JOIN InstallationInclusion installationSetup ON installationSetup.nominationDetail = nominationDetail
      WHERE nominationDetail = :nominationDetail
      """
  )
  NominationType getNominationType(@Param("nominationDetail") NominationDetail nominationDetail);

}
