package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointment;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/termination")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
@IsCurrentAppointment
public class AppointmentTerminationController {

  private final AppointmentTerminationService appointmentTerminationService;

  @Autowired
  public AppointmentTerminationController(AppointmentTerminationService appointmentTerminationService) {
    this.appointmentTerminationService = appointmentTerminationService;
  }

  @GetMapping
  public ModelAndView renderTermination(@PathVariable AppointmentId appointmentId) {
    var appointment = appointmentTerminationService.getAppointment(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));
    return getModelAndView(appointment);
  }

  private ModelAndView getModelAndView(Appointment appointment) {
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var assetDto = appointmentDto.assetDto();
    var assetName = appointmentTerminationService.getAssetName(assetDto);

    return new ModelAndView("osd/systemofrecord/termination/terminateAppointment")
        .addObject("assetName", assetName.value())
        .addObject("appointedOperator", appointmentTerminationService.getAppointedOperator(appointmentDto.appointedOperatorId()))
        .addObject("responsibleFromDate", DateUtil.formatLongDate(appointmentDto.appointmentFromDate().value()))
        .addObject("phases", appointmentTerminationService.getAppointmentPhases(appointment, assetDto))
        .addObject("createdBy", appointmentTerminationService.getCreatedByDisplayString(appointmentDto));
  }
}
