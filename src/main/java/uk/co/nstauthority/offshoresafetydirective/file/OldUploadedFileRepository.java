package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface OldUploadedFileRepository extends CrudRepository<OldUploadedFile, UUID> {

  List<OldUploadedFile> findAllByIdIn(Collection<UUID> id);
}