package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Collection;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;

public record AppendixDocuments(Collection<FileSummaryView> documents) {
}
