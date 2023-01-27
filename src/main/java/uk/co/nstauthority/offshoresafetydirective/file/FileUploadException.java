package uk.co.nstauthority.offshoresafetydirective.file;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadException extends RuntimeException {
  public FileUploadException(MultipartFile multipartFile, Exception e) {
    super(String.format("Could not upload file %s", multipartFile.getOriginalFilename()), e);
  }
}