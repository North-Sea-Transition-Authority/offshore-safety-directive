package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/termination")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
@IsCurrentAppointment
public class AppointmentTerminationController {

  @GetMapping
  public ModelAndView renderTermination(@PathVariable AppointmentId appointmentId) {
    return new ModelAndView();
  }
}
