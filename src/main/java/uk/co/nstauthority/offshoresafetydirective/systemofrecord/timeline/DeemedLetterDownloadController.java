package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadUtils;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;

@Controller
@RequestMapping("/documents/deemed-appointment-note")
@AccessibleByServiceUsers
class DeemedLetterDownloadController {

  public static final String FILE_NAME = "General note on deemed appointments.pdf";
  public static final Path FILE_PATH = Path.of("public", "docs", "DeemedLetter.pdf");

  public static UploadedFileView getAsUploadedFileView() {
    var resource = new ClassPathResource(FILE_PATH.toString(), DeemedLetterDownloadController.class.getClassLoader());
    try {
      return new UploadedFileView(
          "note-concerning-deemed-appointments",
          FILE_NAME,
          FileUploadUtils.fileSizeFormatter((long) resource.getInputStream().available()),
          null,
          null
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping
  public ResponseEntity<Resource> download() {
    var resource = new ClassPathResource(FILE_PATH.toString(), this.getClass().getClassLoader());
    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(resource.getInputStream().available())
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"%s\"".formatted(FILE_NAME)
          )
          .body(resource);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve deemed letter", e);
    }
  }

}
