package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominationDetailFileRepository extends CrudRepository<NominationDetailFile, UUID> {

  List<NominationDetailFile> findAllByUploadedFileAndNominationDetail_Nomination(
      UploadedFile uploadedFile,
      Nomination nomination
  );

  Optional<NominationDetailFile> findByUploadedFileAndNominationDetail(
      UploadedFile uploadedFile,
      NominationDetail nominationDetail
  );

  void deleteByUploadedFileAndNominationDetail(
      UploadedFile uploadedFile,
      NominationDetail nominationDetail
  );

  @Query("""
      FROM NominationDetailFile ndf
      WHERE ndf.nominationDetail.nomination = :nomination
      AND ndf.nominationDetail.version = :nominationVersion
      AND ndf.fileStatus = :fileStatus
      AND ndf.uploadedFile.virtualFolder = :virtualFolder
      """)
  List<NominationDetailFile> findAllNominationDetailFilesByNominationAndStatusAndVirtualFolder(
      Nomination nomination,
      int nominationVersion,
      FileStatus fileStatus,
      VirtualFolder virtualFolder
  );

  // TODO: OSDOP-360 - Review if GROUP BY is the best method / explicit properties are needed
  @Query("""
          FROM NominationDetailFile ndf
          WHERE ndf.uploadedFile.id IN (:fileIds)
          GROUP BY 
            ndf.uploadedFile.id,
            ndf.uuid, ndf.nominationDetail, ndf.uploadedFile, ndf.fileStatus
          HAVING COUNT(ndf.uploadedFile.id) = 1
      """)
  List<NominationDetailFile> getOnlySingleReferencedNominationDetailFilesFromCollection(Collection<UUID> fileIds);

  List<NominationDetailFile> findAllByNominationDetailAndUploadedFile_IdIn(NominationDetail nominationDetail,
                                                                           Collection<UUID> uploadedFileUuids);

}
