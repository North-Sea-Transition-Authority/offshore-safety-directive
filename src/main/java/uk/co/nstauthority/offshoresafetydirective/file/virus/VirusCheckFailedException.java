package uk.co.nstauthority.offshoresafetydirective.file.virus;

import org.springframework.web.multipart.MultipartFile;

public class VirusCheckFailedException extends RuntimeException {
  public VirusCheckFailedException(MultipartFile multipartFile, Exception e) {
    super(String.format("Failed to virus scan file %s", multipartFile.getOriginalFilename()), e);
  }
}
