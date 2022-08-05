package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LicenceBlockSubareaQueryService {

  //TODO remove this dummy values OSDOP-125
  private final List<LicenceBlockSubareaDto> dummyLicenceBlocks = List.of(
      new LicenceBlockSubareaDto(1, "P1234 1/1 West", "0004"),
      new LicenceBlockSubareaDto(2, "A2245 2/4 North", "0002"),
      new LicenceBlockSubareaDto(3, "A1234 1/1 South", "0001"),
      new LicenceBlockSubareaDto(4, "C4242 04/2 East", "0003")
  );

  List<LicenceBlockSubareaDto> queryLicenceBlockSubareaByName(String wellName) {
    return dummyLicenceBlocks.stream()
        .filter(blockSubareaDto ->
            blockSubareaDto.name()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(wellName.toLowerCase(), ""))
        )
        .sorted(Comparator.comparing(LicenceBlockSubareaDto::sortKey))
        .toList();
  }

  public List<LicenceBlockSubareaDto> getLicenceBlockSubareasByIdIn(List<Integer> idList) {
    return dummyLicenceBlocks.stream()
        .filter(wellDto -> idList.contains(wellDto.id()))
        .sorted(Comparator.comparing(LicenceBlockSubareaDto::sortKey))
        .toList();
  }
}
