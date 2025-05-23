package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.wellbore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@RestController
@RequestMapping("/api/appointments/parent-wellbores")
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.APPOINTMENT_MANAGER)
public class WellboreAppointmentRestController {

  private final WellboreAppointmentRestService wellboreAppointmentRestService;

  @Autowired
  WellboreAppointmentRestController(WellboreAppointmentRestService wellboreAppointmentRestService) {
    this.wellboreAppointmentRestService = wellboreAppointmentRestService;
  }

  @GetMapping
  public RestSearchResult searchWellboreAppointments(@RequestParam("term") String searchTerm) {
    var wellboreAppointments = wellboreAppointmentRestService.searchWellboreAppointments(searchTerm);
    return new RestSearchResult(wellboreAppointments);
  }
}
