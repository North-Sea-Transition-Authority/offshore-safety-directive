package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;

public record CaseEventFileView(UploadedFileView uploadedFileView, String downloadUrl) {
}
