package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface FileAssociationRepository extends CrudRepository<FileAssociation, UUID> {

  List<FileAssociation> findAllByUploadedFile_IdIn(Collection<UUID> fileIds);

  List<FileAssociation> findAllByReferenceTypeAndReferenceIdIn(FileAssociationType fileAssociationType,
                                                               Collection<String> referenceIds);

  List<FileAssociation> findAllByReferenceTypeAndFileStatusAndReferenceIdIn(FileAssociationType fileAssociationType,
                                                                            FileStatus fileStatus,
                                                                            Collection<String> referenceIds);

  List<FileAssociation> findAllByReferenceTypeAndReferenceIdInAndPurposeIn(FileAssociationType fileAssociationType,
                                                                           Collection<String> referenceIds,
                                                                           Collection<String> purposes);

  List<FileAssociation> findAllByReferenceTypeAndReferenceIdAndUploadedFile_IdIn(FileAssociationType referenceType,
                                                                                 String referenceId,
                                                                                 Collection<UUID> fileIds);

  Optional<FileAssociation> findByReferenceTypeAndReferenceIdAndUploadedFile_Id(FileAssociationType fileAssociationType,
                                                                                String referenceId,
                                                                                UUID uploadedFileId);

}
