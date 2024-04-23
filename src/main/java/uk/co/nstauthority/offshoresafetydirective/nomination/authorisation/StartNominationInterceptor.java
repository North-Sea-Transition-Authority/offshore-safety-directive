package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;

@Component
public class StartNominationInterceptor extends AbstractHandlerInterceptor {

  private final NominationRoleService nominationRoleService;

  private final UserDetailService userDetailService;

  @Autowired
  public StartNominationInterceptor(NominationRoleService nominationRoleService, UserDetailService userDetailService) {
    this.nominationRoleService = nominationRoleService;
    this.userDetailService = userDetailService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod) {

      Optional<CanStartNomination> canStartNomination = getCanStartNominationAnnotation(handlerMethod);

      if (canStartNomination.isPresent()) {
        var user = userDetailService.getUserDetail();
        return checkCanStartNomination(user.wuaId());
      }
    }

    return true;
  }

  private Optional<CanStartNomination> getCanStartNominationAnnotation(HandlerMethod handlerMethod) {
    if (hasAnnotation(handlerMethod, CanStartNomination.class)) {
      return Optional.of((CanStartNomination) getAnnotation(
          handlerMethod,
          CanStartNomination.class)
      );
    }
    return Optional.empty();
  }

  private boolean checkCanStartNomination(long wuaId) {
    if (nominationRoleService.userCanStartNomination(wuaId)) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "User with ID %s is not able to start a nomination".formatted(wuaId));
    }
  }
}
