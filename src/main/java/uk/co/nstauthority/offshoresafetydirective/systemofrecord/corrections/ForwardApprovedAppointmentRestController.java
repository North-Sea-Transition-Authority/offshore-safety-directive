package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@RestController
@RequestMapping("/api/appointments/forward-approvals")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.APPOINTMENT_MANAGER)
public class ForwardApprovedAppointmentRestController {

  private final ForwardApprovedAppointmentRestService forwardApprovedAppointmentRestService;

  ForwardApprovedAppointmentRestController(ForwardApprovedAppointmentRestService forwardApprovedAppointmentRestService) {
    this.forwardApprovedAppointmentRestService = forwardApprovedAppointmentRestService;
  }

  @GetMapping
  public RestSearchResult searchSubareaAppointments(@RequestParam("term") String searchTerm) {
    var results = forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    return new RestSearchResult(results);
  }
}
