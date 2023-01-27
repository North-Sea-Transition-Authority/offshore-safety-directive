package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;

@Service
public class NominationDetailFileService {

  private final NominationDetailFileRepository nominationDetailFileRepository;
  private final FileUploadService fileUploadService;

  @Autowired
  public NominationDetailFileService(NominationDetailFileRepository nominationDetailFileRepository,
                                     FileUploadService fileUploadService) {
    this.nominationDetailFileRepository = nominationDetailFileRepository;
    this.fileUploadService = fileUploadService;
  }

  @Transactional
  public void createNominationDetailFile(NominationDetail nominationDetail, UploadedFile uploadedFile) {
    var nominationDetailFile = new NominationDetailFile();
    nominationDetailFile.setNominationDetail(nominationDetail);
    nominationDetailFile.setUploadedFile(uploadedFile);
    nominationDetailFile.setFileStatus(FileStatus.DRAFT);
    nominationDetailFileRepository.save(nominationDetailFile);
  }

  public List<NominationDetailFile> getNominationDetailFileForNomination(Nomination nomination,
                                                                         UploadedFile uploadedFile) {
    return nominationDetailFileRepository.findAllByUploadedFileAndNominationDetail_Nomination(
        uploadedFile,
        nomination
    );
  }

  public Optional<NominationDetailFile> getNominationDetailFileForNominationDetail(NominationDetail nominationDetail,
                                                                                   UploadedFile uploadedFile) {
    return nominationDetailFileRepository.findByUploadedFileAndNominationDetail(
        uploadedFile,
        nominationDetail
    );
  }

  @Transactional
  public void deleteNominationDetailFile(NominationDetail nominationDetail, UploadedFile uploadedFile) {
    nominationDetailFileRepository.deleteByUploadedFileAndNominationDetail(uploadedFile,
        nominationDetail);
  }

  @Transactional
  public void submitAndCleanFiles(NominationDetail nominationDetail, List<FileUploadForm> fileUploadForms,
                                  VirtualFolder virtualFolder) {

    if (fileUploadForms.isEmpty()) {
      return;
    }

    var fileIds = fileUploadForms.stream()
        .map(fileUploadForm -> new UploadedFileId(fileUploadForm.getUploadedFileId()))
        .toList();

    this.updateFileStatuses(fileIds, nominationDetail, FileStatus.SUBMITTED);

    var detailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    this.cleanNominationFiles(nominationDetail.getNomination(), detailDto.version(), virtualFolder);
  }

  private void cleanNominationFiles(Nomination nomination, int nominationVersion, VirtualFolder virtualFolder) {
    var nominationDetailFilesToRemove = nominationDetailFileRepository
        .findAllNominationDetailFilesByNominationAndStatusAndVirtualFolder(
            nomination, nominationVersion, FileStatus.DRAFT, virtualFolder
        );

    if (nominationDetailFilesToRemove.isEmpty()) {
      return;
    }

    var uploadedFileUuids = nominationDetailFilesToRemove.stream()
        .map(nominationDetailFile -> nominationDetailFile.getUploadedFile().getId())
        .toList();

    var removableUploadedFiles = nominationDetailFileRepository.getOnlySingleReferencedNominationDetailFilesFromCollection(
        uploadedFileUuids);

    nominationDetailFileRepository.deleteAll(nominationDetailFilesToRemove);

    // TODO: OSDOP-355 - Allow bulk delete operation of files
    removableUploadedFiles.stream()
        .map(NominationDetailFile::getUploadedFile)
        .forEach(fileUploadService::deleteFile);
  }

  private void updateFileStatuses(Collection<UploadedFileId> fileUuids, NominationDetail nominationDetail,
                                  FileStatus status) {
    var ids = fileUuids.stream()
        .map(UploadedFileId::uuid)
        .toList();

    var filesToUpdate = nominationDetailFileRepository.findAllByNominationDetailAndUploadedFile_IdIn(
        nominationDetail,
        ids
    );

    filesToUpdate.forEach(nominationDetailFile -> nominationDetailFile.setFileStatus(status));
    nominationDetailFileRepository.saveAll(filesToUpdate);
  }

}
