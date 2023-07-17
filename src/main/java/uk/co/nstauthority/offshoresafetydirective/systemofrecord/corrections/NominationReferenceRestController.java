package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Comparator;
import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("/api/nomination")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class NominationReferenceRestController {

  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationReferenceRestController(NominationDetailService nominationDetailService) {
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public RestSearchResult searchPostSubmissionNominations(@RequestParam("term") String searchTerm) {
    var results = nominationDetailService.getNominationsByReferenceLikeWithStatuses(
            searchTerm,
            EnumSet.of(NominationStatus.APPOINTED)
        )
        .stream()
        .sorted(Comparator.comparing(NominationDto::nominationReference))
        .map(nominationDto -> new RestSearchItem(
            nominationDto.nominationId().toString(),
            nominationDto.nominationReference()
        ))
        .toList();
    return new RestSearchResult(results);
  }

}
