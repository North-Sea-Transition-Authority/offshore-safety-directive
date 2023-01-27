package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.file.virus.VirusCheckFailedException;
import uk.co.nstauthority.offshoresafetydirective.file.virus.VirusCheckService;

@Service
public class FileUploadValidationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadValidationService.class);
  private final VirusCheckService virusCheckService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public FileUploadValidationService(VirusCheckService virusCheckService, FileUploadConfig fileUploadConfig) {
    this.virusCheckService = virusCheckService;
    this.fileUploadConfig = fileUploadConfig;
  }

  public Optional<UploadErrorType> validateFileUpload(MultipartFile multipartFile, long fileSize, String filename) {
    if (!isFileExtensionAllowed(filename)) {
      return Optional.of(UploadErrorType.EXTENSION_NOT_ALLOWED);
    }

    if (!isFileSizeAllowed(fileSize, filename)) {
      return Optional.of(UploadErrorType.MAX_FILE_SIZE_EXCEEDED);
    }

    LOGGER.debug("Starting virus scan for file: {}", filename);
    try {
      if (virusCheckService.hasVirus(multipartFile.getInputStream())) {
        LOGGER.warn("Virus found in uploaded file: {}", filename);
        return Optional.of(UploadErrorType.VIRUS_FOUND_IN_FILE);
      }
    } catch (Exception e) {
      throw new VirusCheckFailedException(multipartFile, e);
    }
    LOGGER.debug("Completed virus scan for file: {}", filename);

    return Optional.empty();
  }

  private boolean isFileExtensionAllowed(String filename) {
    var lowercaseFilename = filename.toLowerCase();
    var isFileExtensionAllowed = fileUploadConfig.getAllowedFileExtensions()
        .stream()
        .map(String::trim)
        .anyMatch(lowercaseFilename::endsWith);

    if (!isFileExtensionAllowed) {
      LOGGER.warn("Uploaded file {} does not have an allowed file extension", filename);
    }

    return isFileExtensionAllowed;
  }

  private boolean isFileSizeAllowed(long fileSize, String filename) {
    var maxAllowedSize = fileUploadConfig.getMaxFileUploadBytes();
    var isFileSizeAllowed = fileSize <= maxAllowedSize;

    if (!isFileSizeAllowed) {
      LOGGER.warn("Uploaded file %s with file size of %s exceeds file size limit %s".formatted(
          filename,
          FileUploadUtils.fileSizeFormatter(fileSize),
          FileUploadUtils.fileSizeFormatter(Long.valueOf(maxAllowedSize))
      ));
    }

    return isFileSizeAllowed;
  }
}
