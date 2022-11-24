package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class WorkAreaItemService {

  private final NominationWorkAreaItemService nominationWorkAreaItemService;

  @Autowired
  WorkAreaItemService(
      NominationWorkAreaItemService nominationWorkAreaItemService) {
    this.nominationWorkAreaItemService = nominationWorkAreaItemService;
  }

  List<WorkAreaItem> getWorkAreaItems() {
    // Return a union of all items to be displayed in the work area.
    return Stream.of(
            nominationWorkAreaItemService.getNominationWorkAreaItems()
        )
        .flatMap(Collection::stream)
        .sorted(Comparator.comparing(WorkAreaItem::type))
        .toList();
  }

}
