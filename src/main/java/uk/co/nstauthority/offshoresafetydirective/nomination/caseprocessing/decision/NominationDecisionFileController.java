package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import java.util.EnumSet;
import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/decision/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationDecisionFileController {

  public static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf");
  public static final String PURPOSE = "DECISION_DOCUMENT";
  public static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.NOMINATION_DECISION;

  private final NominationDetailService nominationDetailService;
  private final FileEndpointService fileEndpointService;

  @Autowired
  public NominationDecisionFileController(NominationDetailService nominationDetailService,
                                          FileEndpointService fileEndpointService) {
    this.nominationDetailService = nominationDetailService;
    this.fileEndpointService = fileEndpointService;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @RequestParam("file") MultipartFile multipartFile) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return fileEndpointService.processFileUpload(
        new NominationDetailFileReference(nominationDetail),
        PURPOSE,
        VIRTUAL_FOLDER,
        multipartFile,
        ALLOWED_EXTENSIONS
    );
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return fileEndpointService.deleteFile(new NominationDetailFileReference(nominationDetail), uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return fileEndpointService.handleDownload(new NominationDetailFileReference(nominationDetail), uploadedFileId);
  }

  private NominationDetail getLatestSubmittedNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            EnumSet.of(NominationStatus.SUBMITTED)
        )
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %s and status: %s",
              nominationId.id(), NominationStatus.SUBMITTED.name()
          ));
        });
  }

}
