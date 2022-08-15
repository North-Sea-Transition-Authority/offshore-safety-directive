package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstallationQueryServiceTest {

  private static InstallationQueryService installationQueryService;

  @BeforeAll
  static void setup() {
    installationQueryService = new InstallationQueryService();
  }

  @Test
  void queryInstallationsByName_assertReturnedList() {
    var searchTerm = "Installation";
    var installations = installationQueryService.queryInstallationsByName(searchTerm);

    assertThat(installations).containsExactly(
        new InstallationDto(1, "Installation 1"),
        new InstallationDto(2, "Installation 2"),
        new InstallationDto(3, "Installation 3"),
        new InstallationDto(4, "Installation 4")
    );
  }

  @Test
  void getInstallationsByIdIn_assertReturnedList() {
    var idList = List.of(3, 1);
    var installations = installationQueryService.getInstallationsByIdIn(idList);

    assertThat(installations).containsExactly(
        new InstallationDto(1, "Installation 1"),
        new InstallationDto(3, "Installation 3")
    );
  }
}