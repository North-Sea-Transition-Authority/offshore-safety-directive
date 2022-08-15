package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/licence-block-subarea")
public class LicenceBlockSubareaRestController {

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public LicenceBlockSubareaRestController(LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @GetMapping
  public RestSearchResult searchWells(@RequestParam("term") String searchTerm) {
    List<RestSearchItem> searchItemsResult = licenceBlockSubareaQueryService.queryLicenceBlockSubareaByName(searchTerm)
        .stream()
        .map(licenceBlockSubareaDto ->
            new RestSearchItem(String.valueOf(licenceBlockSubareaDto.id()), licenceBlockSubareaDto.name())
        )
        .toList();
    return new RestSearchResult(searchItemsResult);
  }
}
