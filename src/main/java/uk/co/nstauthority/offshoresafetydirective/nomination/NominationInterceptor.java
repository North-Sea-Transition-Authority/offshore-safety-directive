package uk.co.nstauthority.offshoresafetydirective.nomination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanDownloadCaseEventFiles;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasRoleInApplicantOrganisationGroupTeam;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Component
public class NominationInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      HasNominationStatus.class,
      HasRoleInApplicantOrganisationGroupTeam.class,
      CanDownloadCaseEventFiles.class
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationInterceptor.class);

  private final NominationDetailService nominationDetailService;

  private final NominationRoleService nominationRoleService;

  private final UserDetailService userDetailService;

  private final TeamQueryService teamQueryService;

  @Autowired
  NominationInterceptor(NominationDetailService nominationDetailService,
                        NominationRoleService nominationRoleService,
                        UserDetailService userDetailService,
                        TeamQueryService teamQueryService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationRoleService = nominationRoleService;
    this.userDetailService = userDetailService;
    this.teamQueryService = teamQueryService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {
      var nominationId = NominationInterceptorUtil.extractNominationIdFromRequest(request, handlerMethod);

      Optional<HasNominationStatus> hasNominationStatusAnnotation = getNominationStatusAnnotation(handlerMethod);

      if (hasNominationStatusAnnotation.isPresent()) {
        var nominationDetail = getNominationDetail(hasNominationStatusAnnotation.get().fetchType(), nominationId);
        checkNominationStatus(hasNominationStatusAnnotation.get(), nominationDetail);
      }

      Optional<HasRoleInApplicantOrganisationGroupTeam> hasRoleInApplicantOrganisationGroupTeamAnnotation =
          getHasRoleInApplicantOrganisationGroupTeamAnnotation(handlerMethod);

      if (hasRoleInApplicantOrganisationGroupTeamAnnotation.isPresent()) {

        var nominationDetail = getNominationDetail(NominationDetailFetchType.LATEST, nominationId);

        checkRoleInApplicationOrganisationGroupTeam(
            hasRoleInApplicantOrganisationGroupTeamAnnotation.get(),
            nominationDetail
        );
      }

      Optional<CanDownloadCaseEventFiles> hasCanDownloadCaseEventFilesAnnotation =
          getCanDownloadCaseEventFilesAnnotation(handlerMethod);

      if (hasCanDownloadCaseEventFilesAnnotation.isPresent()) {
        checkUserCanDownloadCaseEventFiles();
      }
    }

    return true;
  }

  private Optional<HasNominationStatus> getNominationStatusAnnotation(HandlerMethod handlerMethod) {

    if (hasAnnotation(handlerMethod, HasNominationStatus.class)) {
      return Optional.of((HasNominationStatus) getAnnotation(handlerMethod, HasNominationStatus.class));
    }

    return Optional.empty();
  }

  private Optional<HasRoleInApplicantOrganisationGroupTeam> getHasRoleInApplicantOrganisationGroupTeamAnnotation(
      HandlerMethod handlerMethod
  ) {

    if (hasAnnotation(handlerMethod, HasRoleInApplicantOrganisationGroupTeam.class)) {
      var annotation = (HasRoleInApplicantOrganisationGroupTeam) getAnnotation(
          handlerMethod,
          HasRoleInApplicantOrganisationGroupTeam.class
      );

      return Optional.of(annotation);
    }

    return Optional.empty();
  }

  private Optional<CanDownloadCaseEventFiles> getCanDownloadCaseEventFilesAnnotation(
      HandlerMethod handlerMethod
  ) {

    if (hasAnnotation(handlerMethod, CanDownloadCaseEventFiles.class)) {
      var annotation = (CanDownloadCaseEventFiles) getAnnotation(
          handlerMethod,
          CanDownloadCaseEventFiles.class
      );

      return Optional.of(annotation);
    }

    return Optional.empty();
  }

  private NominationDetail getNominationDetail(NominationDetailFetchType fetchType, NominationId nominationId) {
    return (
        switch (fetchType) {
          case LATEST -> Optional.ofNullable(nominationDetailService.getLatestNominationDetail(nominationId));
          case LATEST_POST_SUBMISSION -> nominationDetailService.getLatestNominationDetailWithStatuses(
              nominationId,
              NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
          );
        }
    )
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "A non null nomination detail was expected to be found for nomination id %s".formatted(nominationId.id()))
        );
  }

  private void checkNominationStatus(HasNominationStatus annotation, NominationDetail nominationDetail) {

    var allowedStatuses = Arrays.asList(annotation.statuses());

    if (!allowedStatuses.contains(nominationDetail.getStatus())) {

      var allowedStatusNames = allowedStatuses
          .stream()
          .map(NominationStatus::name)
          .toList();

      var errorMessage = "Nomination detail with ID %s has status %s but requires %s"
          .formatted(
              nominationDetail.getId(),
              nominationDetail.getStatus().name(),
              StringUtils.join(allowedStatusNames)
          );

      LOGGER.warn(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
  }

  private void checkRoleInApplicationOrganisationGroupTeam(
      HasRoleInApplicantOrganisationGroupTeam annotation,
      NominationDetail nominationDetail
  ) {

    Set<Role> requiredRoles = Arrays.stream(annotation.roles()).collect(Collectors.toSet());

    var user = userDetailService.getUserDetail();

    if (!teamQueryService.areRolesValidForTeamType(requiredRoles, TeamType.ORGANISATION_GROUP)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not all roles %s are valid for team type %s".formatted(requiredRoles, TeamType.ORGANISATION_GROUP)
      );
    }

    var userHasRoleInApplicantTeam = nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        user.wuaId(),
        nominationDetail,
        Arrays.stream(annotation.roles()).collect(Collectors.toSet())
    );

    if (!userHasRoleInApplicantTeam) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "User with ID %s does not have any of roles %s in applicant group team for nomination detail %s"
              .formatted(user.wuaId(), requiredRoles, nominationDetail.getId())
      );
    }
  }

  private void checkUserCanDownloadCaseEventFiles() {

    var user = userDetailService.getUserDetail();

    var requiredRegulatorRoles = Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION);

    var hasRoleInRegulatorTeam = teamQueryService.userHasAtLeastOneStaticRole(
        user.wuaId(),
        TeamType.REGULATOR,
        requiredRegulatorRoles
    );

    if (!hasRoleInRegulatorTeam) {

      var requiredConsulteeRoles = Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT);

      var hasRoleInConsulteeTeam = teamQueryService.userHasAtLeastOneStaticRole(
          user.wuaId(),
          TeamType.CONSULTEE,
          requiredConsulteeRoles
      );

      if (!hasRoleInConsulteeTeam) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User with ID %s does not have any of roles %s in regulator team or any of roles %s in the consultee team"
                .formatted(user.wuaId(), requiredRegulatorRoles, requiredConsulteeRoles)
        );
      }
    }
  }
}
