package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@Component
public class IsMemberOfTeamTypeInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      IsMemberOfTeamType.class
  );

  private final UserDetailService userDetailService;
  private final RegulatorTeamService regulatorTeamService;
  private final ConsulteeTeamService consulteeTeamService;
  private final IndustryTeamService teamService;

  @Autowired
  public IsMemberOfTeamTypeInterceptor(UserDetailService userDetailService, RegulatorTeamService regulatorTeamService,
                                       ConsulteeTeamService consulteeTeamService, IndustryTeamService teamService) {
    this.userDetailService = userDetailService;
    this.regulatorTeamService = regulatorTeamService;
    this.consulteeTeamService = consulteeTeamService;
    this.teamService = teamService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var isMemberOfTeamTypeAnnotation = (IsMemberOfTeamType) getAnnotation(handlerMethod, IsMemberOfTeamType.class);
      var teamType = isMemberOfTeamTypeAnnotation.value();
      var user = userDetailService.getUserDetail();

      var isMemberOfTeamType =  switch (teamType) {
        case REGULATOR ->  regulatorTeamService.isMemberOfRegulatorTeam(user);
        case CONSULTEE -> consulteeTeamService.isMemberOfConsulteeTeam(user);
        case INDUSTRY -> teamService.isMemberOfIndustryTeam(user);
      };

      if (!isMemberOfTeamType) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User is not a member of team [%s]".formatted(teamType.getDisplayText())
        );
      }
    }
    return true;
  }
}
