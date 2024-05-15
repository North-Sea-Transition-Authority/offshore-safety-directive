package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RequestMapping("/api")
@RestController
@AccessibleByServiceUsers
public class FieldRestController {

  static final Set<FieldStatus> NON_DELETION_FIELD_STATUSES = EnumSet.allOf(FieldStatus.class)
      .stream()
      .filter(fieldStatus -> !EnumSet.of(FieldStatus.STATUS9999).contains(fieldStatus))
      .collect(Collectors.toSet());

  static final RequestPurpose SEARCH_FIELDS_PURPOSE = new RequestPurpose("Fields search selector (search fields)");

  private final EnergyPortalFieldQueryService fieldQueryService;

  @Autowired
  public FieldRestController(EnergyPortalFieldQueryService fieldQueryService) {
    this.fieldQueryService = fieldQueryService;
  }

  @GetMapping("/active-fields")
  public RestSearchResult getActiveFields(@RequestParam(value = "term", required = false) String fieldName) {

    List<RestSearchItem> searchItemsResult = fieldQueryService.searchFields(
            fieldName,
            NON_DELETION_FIELD_STATUSES,
            SEARCH_FIELDS_PURPOSE
        )
        .stream()
        .sorted(Comparator.comparing(field -> field.name().toLowerCase()))
        .map(field -> new RestSearchItem(String.valueOf(field.fieldId().id()), field.name()))
        .toList();

    return new RestSearchResult(searchItemsResult);
  }

}
