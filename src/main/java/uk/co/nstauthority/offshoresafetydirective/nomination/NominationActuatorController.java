package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@RestControllerEndpoint(id = "nominations")
@Profile("!disable-epmq")
class NominationActuatorController {

  private final NominationSnsService nominationSnsService;

  @Autowired
  NominationActuatorController(NominationSnsService nominationSnsService) {
    this.nominationSnsService = nominationSnsService;
  }

  @PostMapping("publish-epmq-message/nomination/{nominationId}")
  ResponseEntity<Void> publishNominationSubmittedMessage(@PathVariable("nominationId") NominationId nominationId) {
    nominationSnsService.publishNominationSubmittedMessage(nominationId);
    return ResponseEntity.ok().build();
  }
}
