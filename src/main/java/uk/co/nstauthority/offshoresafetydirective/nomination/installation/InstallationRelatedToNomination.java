package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;

public record InstallationRelatedToNomination(boolean related, List<String> relatedInstallations) {
}
