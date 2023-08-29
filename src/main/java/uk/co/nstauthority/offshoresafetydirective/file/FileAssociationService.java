package uk.co.nstauthority.offshoresafetydirective.file;

import java.time.Clock;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileAssociationService {

  private final FileAssociationRepository fileAssociationRepository;
  private final FileUploadService fileUploadService;
  private final Clock clock;

  @Autowired
  public FileAssociationService(FileAssociationRepository fileAssociationRepository,
                                FileUploadService fileUploadService, Clock clock) {
    this.fileAssociationRepository = fileAssociationRepository;
    this.fileUploadService = fileUploadService;
    this.clock = clock;
  }

  @Transactional
  public void createDraftAssociation(UploadedFile uploadedFile, FileAssociationReference fileReference,
                                     String purpose) {
    var draftDetail = new FileAssociation();
    draftDetail.setUploadedFile(uploadedFile);
    draftDetail.setFileStatus(FileStatus.DRAFT);
    draftDetail.setReferenceType(fileReference.getFileReferenceType());
    draftDetail.setReferenceId(fileReference.getReferenceId());
    draftDetail.setPurpose(purpose);
    draftDetail.setUploadedInstant(clock.instant());
    fileAssociationRepository.save(draftDetail);
  }

  public Optional<FileAssociation> findFileAssociation(FileAssociationReference fileReference,
                                                       UploadedFileId uploadedFileId) {
    return fileAssociationRepository.findByReferenceTypeAndReferenceIdAndUploadedFile_Id(
        fileReference.getFileReferenceType(),
        fileReference.getReferenceId(),
        uploadedFileId.uuid()
    );
  }

  public List<FileAssociationDto> getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds(
      FileAssociationType referenceType,
      Collection<String> referenceIds
  ) {
    return fileAssociationRepository.findAllByReferenceTypeAndReferenceIdIn(referenceType, referenceIds)
        .stream()
        .sorted(Comparator.comparing(
            fileAssociation -> fileAssociation.getUploadedFile().getFilename(),
            String::compareToIgnoreCase
        ))
        .map(FileAssociationDto::from)
        .toList();
  }

  public List<FileAssociationDto> getSubmittedUploadedFileAssociations(
      FileAssociationType referenceType,
      Collection<String> referenceIds
  ) {
    return fileAssociationRepository
        .findAllByReferenceTypeAndFileStatusAndReferenceIdIn(referenceType, FileStatus.SUBMITTED, referenceIds)
        .stream()
        .map(FileAssociationDto::from)
        .toList();
  }

  public List<FileAssociation> getFileAssociationByFileAssociationReference(
      FileAssociationReference fileAssociationReference) {
    return fileAssociationRepository.findAllByReferenceTypeAndReferenceIdIn(
        fileAssociationReference.getFileReferenceType(),
        List.of(fileAssociationReference.getReferenceId())
    );
  }

  public List<FileAssociation> getAllByFileReferenceAndUploadedFileIds(
      FileAssociationReference fileAssociationReference,
      Collection<UploadedFileId> uploadedFileIds) {
    var fileIds = uploadedFileIds.stream()
        .map(UploadedFileId::uuid)
        .toList();

    return fileAssociationRepository.findAllByReferenceTypeAndReferenceIdAndUploadedFile_IdIn(
        fileAssociationReference.getFileReferenceType(),
        fileAssociationReference.getReferenceId(),
        fileIds
    );
  }

  @Transactional
  public void updateFileReferences(Collection<FileAssociation> fileAssociations,
                                   FileAssociationReference newReference) {
    fileAssociations.forEach(fileAssociation -> {
      fileAssociation.setReferenceId(newReference.getReferenceId());
      fileAssociation.setReferenceType(newReference.getFileReferenceType());
    });
    fileAssociationRepository.saveAll(fileAssociations);
  }

  void deleteFileAssociation(FileAssociation fileAssociation) {
    fileAssociationRepository.delete(fileAssociation);
  }

  @Transactional
  public void submitFiles(List<FileUploadForm> fileUploadForms) {
    if (fileUploadForms.isEmpty()) {
      return;
    }

    var fileIds = fileUploadForms.stream()
        .map(fileUploadForm -> new UploadedFileId(fileUploadForm.getUploadedFileId()))
        .toList();

    fileUploadService.updateFileUploadDescriptions(fileUploadForms);
    this.updateFileStatuses(fileIds, FileStatus.SUBMITTED);
  }

  public Map<FilePurpose, List<UploadedFileView>> getSubmittedUploadedFileViewsForReferenceAndPurposes(
      FileAssociationReference fileAssociationReference,
      Collection<String> purposes
  ) {
    var submittedFileAssociations = fileAssociationRepository.findAllByReferenceTypeAndReferenceIdInAndPurposeIn(
            fileAssociationReference.getFileReferenceType(),
            List.of(fileAssociationReference.getReferenceId()),
            purposes
        )
        .stream()
        .filter(fileAssociation -> fileAssociation.getFileStatus().equals(FileStatus.SUBMITTED))
        .toList();

    var fileIds = submittedFileAssociations.stream()
        .map(fileAssociation -> new UploadedFileId(fileAssociation.getUploadedFile().getId()))
        .toList();
    Map<UploadedFileId, UploadedFileView> fileIdAndViewMap = fileUploadService.getUploadedFileViewList(fileIds)
        .stream()
        .collect(Collectors.toMap(
            fileView -> UploadedFileId.valueOf(fileView.getFileId()),
            fileView -> fileView
        ));

    return submittedFileAssociations.stream()
        .collect(Collectors.groupingBy(
            detail -> new FilePurpose(detail.getPurpose()),
            Collectors.mapping(fileAssociation -> fileIdAndViewMap.get(
                new UploadedFileId(fileAssociation.getUploadedFile().getId())), Collectors.toList())
        ));
  }

  private void updateFileStatuses(Collection<UploadedFileId> uploadedFileIds,
                                  FileStatus newStatus) {
    var fileUuids = uploadedFileIds.stream()
        .map(UploadedFileId::uuid)
        .toList();

    var filesToUpdate = fileAssociationRepository.findAllByUploadedFile_IdIn(fileUuids);

    filesToUpdate.forEach(fileAssociation -> fileAssociation.setFileStatus(newStatus));

    fileAssociationRepository.saveAll(filesToUpdate);
  }

}
