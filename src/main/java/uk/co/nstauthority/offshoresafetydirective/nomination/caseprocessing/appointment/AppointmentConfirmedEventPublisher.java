package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Component
class AppointmentConfirmedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  AppointmentConfirmedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publish(NominationDetail nominationDetail) {
    applicationEventPublisher.publishEvent(
        new AppointmentConfirmedEvent(new NominationId(nominationDetail.getNomination().getId()))
    );
  }

}
