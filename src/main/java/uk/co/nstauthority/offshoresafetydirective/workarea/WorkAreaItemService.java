package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.Collection;
import java.util.Collections;
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

  List<WorkAreaItem> getWorkAreaItems(WorkAreaFilter filter) {
    // Return a union of all items to be displayed in the work area.
    if (filter == null || filter.nominationStatuses() == null) {
      return Collections.emptyList();
    }

    return Stream.of(
            nominationWorkAreaItemService.getNominationWorkAreaItems(filter)
        )
        .flatMap(Collection::stream)
        .sorted(Comparator.comparing(WorkAreaItem::type))
        .toList();
  }

}
