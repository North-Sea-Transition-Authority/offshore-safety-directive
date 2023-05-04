package uk.co.nstauthority.offshoresafetydirective.file;

public record FileAssociationDto(
    UploadedFileId uploadedFileId,
    String referenceId
) {

  public static FileAssociationDto from(FileAssociation fileAssociation) {
    return new FileAssociationDto(
        new UploadedFileId(fileAssociation.getUploadedFile().getId()),
        fileAssociation.getReferenceId()
    );
  }

}
