package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FilePurpose;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/nominee-details/{nominationDetailId}/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.DRAFT, fetchType = NominationDetailFetchType.LATEST)
public class NomineeDetailAppendixFileController {

  public static final FilePurpose PURPOSE = new FilePurpose("APPENDIX_C_DOCUMENT");
  public static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.APPENDIX_C;

  private final NominationDetailService nominationDetailService;
  private final FileControllerHelperService fileControllerHelperService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public NomineeDetailAppendixFileController(NominationDetailService nominationDetailService,
                                             FileControllerHelperService fileControllerHelperService,
                                             FileUploadConfig fileUploadConfig) {
    this.nominationDetailService = nominationDetailService;
    this.fileControllerHelperService = fileControllerHelperService;
    this.fileUploadConfig = fileUploadConfig;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("nominationDetailId") NominationDetailId nominationDetailId,
                                 @Nullable @RequestParam("file") MultipartFile multipartFile) {

    var nominationDetail = getNominationDetail(nominationId, nominationDetailId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.processFileUpload(fileReference, PURPOSE.purpose(), VIRTUAL_FOLDER,
        Objects.requireNonNull(multipartFile), fileUploadConfig.getDefaultPermittedFileExtensions());
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("nominationDetailId") NominationDetailId nominationDetailId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getNominationDetail(nominationId, nominationDetailId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.deleteFile(fileReference, uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(
      @PathVariable("nominationId") NominationId nominationId,
      @PathVariable("nominationDetailId") NominationDetailId nominationDetailId,
      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId
  ) {

    var nominationDetail = getNominationDetail(nominationId, nominationDetailId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.downloadFile(fileReference, uploadedFileId);
  }

  private NominationDetail getNominationDetail(NominationId nominationId,
                                               NominationDetailId nominationDetailId) {
    return nominationDetailService.getNominationDetail(nominationDetailId)
        .filter(nominationDetail -> nominationDetail.getNomination().getId().equals(nominationId.id()))
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "Cannot find latest NominationDetail with ID [%d] for Nomination [%s]".formatted(
                nominationDetailId.id(),
                nominationId.id()
            )));
  }

}
