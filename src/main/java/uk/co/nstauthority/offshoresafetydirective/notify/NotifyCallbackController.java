package uk.co.nstauthority.offshoresafetydirective.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.notify.library.model.NotifyCallback;
import uk.co.fivium.notify.library.service.FiviumNotifyCallbackAccessDeniedException;
import uk.co.fivium.notify.library.service.FiviumNotifyCallbackService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;

@Unauthenticated
@RestController
public class NotifyCallbackController {

  private final FiviumNotifyCallbackService fiviumNotifyCallbackService;

  @Autowired
  public NotifyCallbackController(FiviumNotifyCallbackService fiviumNotifyCallbackService) {
    this.fiviumNotifyCallbackService = fiviumNotifyCallbackService;
  }

  @PostMapping("/notify/callback")
  public ResponseEntity<Object> notifyCallback(@RequestBody NotifyCallback callbackRequest,
                                               @RequestHeader("Authorization") String bearerToken) {
    try {
      fiviumNotifyCallbackService.handleCallback(callbackRequest, bearerToken);
    } catch (FiviumNotifyCallbackAccessDeniedException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access request for NotifyCallback cannot be authorised");
    }
    return ResponseEntity.ok().build();
  }
}
