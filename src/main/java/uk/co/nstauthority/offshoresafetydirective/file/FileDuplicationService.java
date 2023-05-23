package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
public class FileDuplicationService {

  private final UploadedFileRepository uploadedFileRepository;
  private final FileAssociationService fileAssociationService;
  private final FileAssociationRepository fileAssociationRepository;

  public FileDuplicationService(UploadedFileRepository uploadedFileRepository,
                                FileAssociationService fileAssociationService,
                                FileAssociationRepository fileAssociationRepository) {
    this.uploadedFileRepository = uploadedFileRepository;
    this.fileAssociationService = fileAssociationService;
    this.fileAssociationRepository = fileAssociationRepository;
  }

  @Transactional
  public void duplicateFiles(NominationDetail nominationDetailToDuplicate,
                             NominationDetail nominationDetailToCascadeTo) {
    var fileAssociations = fileAssociationService.getFileAssociationByFileAssociationReference(
        new NominationDetailFileReference(nominationDetailToDuplicate));

    var uploadFilesToDuplicate = fileAssociations.stream()
        .map(FileAssociation::getUploadedFile)
        .toList();

    var duplicatedUploadedFiles = duplicateUploadedFiles(uploadFilesToDuplicate);
    duplicateFileAssociations(nominationDetailToCascadeTo, fileAssociations, duplicatedUploadedFiles);
  }

  private List<UploadedFile> duplicateUploadedFiles(Collection<UploadedFile> uploadedFilesToDuplicate) {
    var duplicatedFiles = new ArrayList<UploadedFile>();

    uploadedFilesToDuplicate.forEach(file -> {
      var newFile = BeanUtils.instantiateClass(UploadedFile.class);
      DuplicationUtil.copyProperties(file, newFile, "id");
      duplicatedFiles.add(newFile);
    });
    uploadedFileRepository.saveAll(duplicatedFiles);
    return List.copyOf(duplicatedFiles);
  }

  private void duplicateFileAssociations(NominationDetail nominationDetailToCascadeTo,
                                         Collection<FileAssociation> fileAssociations,
                                         Collection<UploadedFile> duplicatedUploadedFiles) {
    var fileAssociationToUploadedFileMap = fileAssociations.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            fileAssociation ->
                getUploadedFileForFileAssociation(nominationDetailToCascadeTo, fileAssociation, duplicatedUploadedFiles)
        ));

    var duplicatedFileAssociations = new ArrayList<FileAssociation>();

    fileAssociationToUploadedFileMap.forEach((fileAssociation, uploadedFile) -> {
      var newAssociation = BeanUtils.instantiateClass(FileAssociation.class);
      var detailDto = NominationDetailDto.fromNominationDetail(nominationDetailToCascadeTo);
      DuplicationUtil.copyProperties(fileAssociation, newAssociation, "uuid");
      newAssociation.setUploadedFile(uploadedFile);
      newAssociation.setReferenceId(String.valueOf(detailDto.nominationDetailId().id()));
      duplicatedFileAssociations.add(newAssociation);
    });

    fileAssociationRepository.saveAll(duplicatedFileAssociations);
  }

  private UploadedFile getUploadedFileForFileAssociation(NominationDetail nominationDetail,
                                                         FileAssociation fileAssociation,
                                                         Collection<UploadedFile> uploadedFiles) {
    return uploadedFiles.stream().filter(uploadedFile ->
            uploadedFile.getFileKey().equals(fileAssociation.getUploadedFile().getFileKey()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No file key [%s] found for uploaded files of nomination [%d]".formatted(
                fileAssociation.getUploadedFile().getFileKey(),
                new NominationId(nominationDetail).id()
            )));
  }

}
