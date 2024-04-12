package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;

@Controller
@RequestMapping("/termination/{terminationId}")
// TODO OSDOP-811 @IsMemberOfTeamType(TeamType.REGULATOR)
public class AppointmentTerminationFileController {

  private final AppointmentTerminationService appointmentTerminationService;
  private final FileService fileService;

  @Autowired
  public AppointmentTerminationFileController(AppointmentTerminationService appointmentTerminationService,
                                              FileService fileService) {
    this.appointmentTerminationService = appointmentTerminationService;
    this.fileService = fileService;
  }

  @ResponseBody
  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable UUID terminationId,
                                                      @PathVariable UUID fileId) {

    var termination = appointmentTerminationService.getTermination(terminationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No termination with ID [%s] found for appointment [%s]"
        ));

    return fileService.find(fileId)
        .filter(uploadedFile -> canAccessFile(uploadedFile, termination))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  private boolean canAccessFile(UploadedFile uploadedFile, AppointmentTermination termination) {
    return Objects.equals(uploadedFile.getUsageId(), termination.getId().toString())
        && Objects.equals(uploadedFile.getUsageType(), FileUsageType.TERMINATION.getUsageType())
        && Objects.equals(uploadedFile.getDocumentType(), FileDocumentType.TERMINATION.name());
  }
}
