package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UploadedFileRepository extends CrudRepository<UploadedFile, UUID> {

  List<UploadedFile> findAllByIdIn(List<UUID> id);
}