package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class WellQueryService {

  //TODO remove this dummy values OSDOP-91
  private final List<WellDto> dummyWells = List.of(
      new WellDto(1, "16/01-3", "0004"),
      new WellDto(2, "24/99-12", "0002"),
      new WellDto(3, "23/04-96", "0001"),
      new WellDto(4, "06/03-96", "0003")
  );

  List<WellDto> queryWellByName(String wellName) {
    return dummyWells.stream()
        .filter(wellDto ->
            wellDto.name()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(wellName.toLowerCase(), ""))
        )
        .sorted(Comparator.comparing(WellDto::sortKey))
        .toList();
  }

  public List<WellDto> getWellsByIdIn(List<Integer> idList) {
    return dummyWells.stream()
        .filter(wellDto -> idList.contains(wellDto.id()))
        .sorted(Comparator.comparing(WellDto::sortKey))
        .toList();
  }
}
