package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationFileService;

@Service
public class NominationFileEndpointService {

  private final NominationDetailService nominationDetailService;
  private final NominationFileService nominationFileService;

  @Autowired
  public NominationFileEndpointService(NominationDetailService nominationDetailService,
                                       NominationFileService nominationFileService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationFileService = nominationFileService;
  }

  public Optional<FileUploadResult> handleUpload(NominationId nominationId, MultipartFile multipartFile,
                                                 VirtualFolder virtualFolder, Set<NominationStatus> statuses,
                                                 Collection<String> allowedExtensions) {

    return nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses)
        .map(nominationDetail -> nominationFileService.processFileUpload(
            nominationDetail,
            virtualFolder,
            multipartFile,
            allowedExtensions
        ));
  }

  public Optional<FileDeleteResult> handleDelete(NominationId nominationId, UploadedFileId uploadedFileId,
                                                 Set<NominationStatus> statuses) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses)
        .map(nominationDetail -> nominationFileService.deleteFile(nominationDetail, uploadedFileId));
  }

  public Optional<ResponseEntity<InputStreamResource>> handleDownload(NominationId nominationId,
                                                                      UploadedFileId uploadedFileId,
                                                                      Set<NominationStatus> statuses) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses)
        .map(nominationDetail -> nominationFileService.handleDownload(nominationDetail, uploadedFileId));
  }

}
