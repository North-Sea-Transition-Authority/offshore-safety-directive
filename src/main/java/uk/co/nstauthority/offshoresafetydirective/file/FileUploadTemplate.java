package uk.co.nstauthority.offshoresafetydirective.file;

public record FileUploadTemplate(
    String downloadUrl,
    String uploadUrl,
    String deleteUrl,
    String maxAllowedSize,
    String allowedExtensions
) {
}