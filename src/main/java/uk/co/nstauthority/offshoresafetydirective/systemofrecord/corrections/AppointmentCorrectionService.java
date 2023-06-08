package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;

@Service
class AppointmentCorrectionService {

  private final AppointmentUpdateService appointmentUpdateService;

  @Autowired
  AppointmentCorrectionService(AppointmentUpdateService appointmentUpdateService) {
    this.appointmentUpdateService = appointmentUpdateService;
  }

  AppointmentCorrectionForm getForm(AppointmentDto appointment) {
    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(Integer.valueOf(appointment.appointedOperatorId().id()));
    return form;
  }

  @Transactional
  public void updateCorrection(AppointmentDto appointmentDto, AppointmentCorrectionForm appointmentCorrectionForm) {
    var updateDto = new AppointmentDto(
        appointmentDto.appointmentId(),
        appointmentDto.portalAssetId(),
        new AppointedOperatorId(appointmentCorrectionForm.getAppointedOperatorId().toString()),
        appointmentDto.appointmentFromDate(),
        appointmentDto.appointmentToDate(),
        appointmentDto.appointmentCreatedDate(),
        appointmentDto.appointmentType(),
        appointmentDto.legacyNominationReference(),
        appointmentDto.nominationId()
    );
    appointmentUpdateService.updateAppointment(updateDto);
  }

}
