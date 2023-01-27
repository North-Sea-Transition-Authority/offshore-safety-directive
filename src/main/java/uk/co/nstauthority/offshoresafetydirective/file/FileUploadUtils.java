package uk.co.nstauthority.offshoresafetydirective.file;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class FileUploadUtils {

  private static final long BYTE = 1L;
  private static final long KB = BYTE << 10;
  private static final long MB = KB << 10;
  private static final long GB = MB << 10;


  private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.#");

  private FileUploadUtils() {
  }

  public static String fileSizeFormatter(Long size) {
    if (size < 0) {
      throw new IllegalArgumentException("Invalid file size: " + size);
    }
    if (size >= GB) {
      return formatSize(size, GB, "GB");
    }
    if (size >= MB) {
      return formatSize(size, MB, "MB");
    }
    if (size >= KB) {
      return formatSize(size, KB, "KB");
    }
    return formatSize(size, BYTE, "B");
  }

  public static ResponseEntity<InputStreamResource> getFileResourceResponseEntity(UploadedFile uploadedFile,
                                                                                  InputStreamResource inputStreamResource) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(uploadedFile.getFileSizeBytes())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"%s\"".formatted(uploadedFile.getFilename())
        )
        .body(inputStreamResource);
  }

  public static List<UUID> getUploadIdList(List<FileUploadForm> fileUploadForms) {
    return fileUploadForms.stream()
        .map(FileUploadForm::getUploadedFileId)
        .toList();
  }

  private static String formatSize(long size, long divider, String unitName) {
    return DEC_FORMAT.format((double) size / divider) + " " + unitName;
  }
}
