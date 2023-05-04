package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UploadedFileDetailRepository extends CrudRepository<UploadedFileDetail, UUID> {

  List<UploadedFileDetail> findAllByUploadedFile_IdIn(Collection<UUID> fileIds);

  List<UploadedFileDetail> findAllByReferenceTypeAndReferenceIdIn(FileReferenceType fileReferenceType,
                                                                  Collection<String> referenceIds);

  List<UploadedFileDetail> findAllByReferenceTypeAndReferenceIdAndUploadedFile_IdIn(FileReferenceType referenceType,
                                                                                    String referenceId,
                                                                                    Collection<UUID> fileIds);

  Optional<UploadedFileDetail> findByReferenceTypeAndReferenceIdAndUploadedFile_Id(FileReferenceType fileReferenceType,
                                                                                   String referenceId,
                                                                                   UUID uploadedFileId);

}
