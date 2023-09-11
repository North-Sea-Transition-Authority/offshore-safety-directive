package uk.co.nstauthority.offshoresafetydirective.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@Component
public class IsMemberOfTeamTypeInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(IsMemberOfTeamType.class);

  private final UserDetailService userDetailService;
  private final RegulatorTeamService regulatorTeamService;
  private final ConsulteeTeamService consulteeTeamService;
  private final IndustryTeamService industryTeamService;

  @Autowired
  public IsMemberOfTeamTypeInterceptor(UserDetailService userDetailService, RegulatorTeamService regulatorTeamService,
                                       ConsulteeTeamService consulteeTeamService, IndustryTeamService industryTeamService) {
    this.userDetailService = userDetailService;
    this.regulatorTeamService = regulatorTeamService;
    this.consulteeTeamService = consulteeTeamService;
    this.industryTeamService = industryTeamService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var isMemberOfTeamTypeAnnotation = (IsMemberOfTeamType) getAnnotation(handlerMethod, IsMemberOfTeamType.class);
      var teamTypes = isMemberOfTeamTypeAnnotation.value();

      var isMemberOfTeam = Arrays.stream(teamTypes)
          .anyMatch(this::isMemberOfTeamType);

      if (!isMemberOfTeam) {
        var teamTypeDisplayNames = Arrays.stream(teamTypes)
            .map(TeamType::getDisplayText)
            .collect(Collectors.joining(","));

        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User is not a member of any teams of type [%s]"
                .formatted(teamTypeDisplayNames)
        );
      }
    }
    return true;
  }

  private boolean isMemberOfTeamType(TeamType teamType) {
    var user = userDetailService.getUserDetail();

    return switch (teamType) {
      case REGULATOR ->  regulatorTeamService.isMemberOfRegulatorTeam(user);
      case CONSULTEE -> consulteeTeamService.isMemberOfConsulteeTeam(user);
      case INDUSTRY -> industryTeamService.isMemberOfIndustryTeam(user);
    };
  }
}
