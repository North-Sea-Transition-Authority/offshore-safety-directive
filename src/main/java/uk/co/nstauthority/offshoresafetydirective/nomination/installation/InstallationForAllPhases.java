package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;

public record InstallationForAllPhases(boolean forAllPhases, List<String> phases) {
}
