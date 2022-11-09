package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RequestMapping("/api/fields")
@RestController
public class FieldRestController {
  private final FieldRestService fieldRestService;

  @Autowired
  FieldRestController(FieldRestService fieldRestService) {
    this.fieldRestService = fieldRestService;
  }

  @GetMapping("/active")
  public RestSearchResult getActiveFields(@RequestParam(value = "term", required = false) String searchTerm) {
    return fieldRestService.searchForFields(searchTerm);
  }

}
