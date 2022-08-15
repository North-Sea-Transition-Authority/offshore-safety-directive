package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class InstallationQueryService {

  //TODO remove this dummy values OSDOP-92
  private final List<InstallationDto> dummyInstallations = List.of(
      new InstallationDto(1, "Installation 1"),
      new InstallationDto(2, "Installation 2"),
      new InstallationDto(3, "Installation 3"),
      new InstallationDto(4, "Installation 4")
  );

  List<InstallationDto> queryInstallationsByName(String wellName) {
    return dummyInstallations.stream()
        .filter(installationDto ->
            installationDto.name()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(wellName.toLowerCase(), ""))
        )
        .sorted(Comparator.comparing(InstallationDto::name))
        .toList();
  }

  public List<InstallationDto> getInstallationsByIdIn(List<Integer> idList) {
    return dummyInstallations.stream()
        .filter(installationDto -> idList.contains(installationDto.id()))
        .sorted(Comparator.comparing(InstallationDto::name))
        .toList();
  }
}
