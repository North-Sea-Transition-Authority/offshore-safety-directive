package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@WebEndpoint(id = "nomination-submitted-message")
@Profile("!disable-epmq")
class NominationActuatorController {

  private final NominationSnsService nominationSnsService;

  @Autowired
  NominationActuatorController(NominationSnsService nominationSnsService) {
    this.nominationSnsService = nominationSnsService;
  }

  @WriteOperation
  ResponseEntity<Void> publishMessage(@Selector UUID nominationId) {
    nominationSnsService.publishNominationSubmittedMessage(new NominationId(nominationId));
    return ResponseEntity.ok().build();
  }
}
