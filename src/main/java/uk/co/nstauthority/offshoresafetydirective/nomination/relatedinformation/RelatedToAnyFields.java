package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.List;

public record RelatedToAnyFields(boolean related, List<String> fieldNames) {
}
