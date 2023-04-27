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

  private final NominationDetailService nominationDetailService;
  private final NominationSnsService nominationSnsService;

  @Autowired
  NominationActuatorController(
      NominationDetailService nominationDetailService,
      NominationSnsService nominationSnsService
  ) {
    this.nominationDetailService = nominationDetailService;
    this.nominationSnsService = nominationSnsService;
  }

  @PostMapping("publish-epmq-message/nomination/{nominationId}")
  ResponseEntity<Void> publishNominationSubmittedMessage(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    nominationSnsService.publishNominationSubmittedMessage(nominationDetail);
    return ResponseEntity.ok().build();
  }
}
