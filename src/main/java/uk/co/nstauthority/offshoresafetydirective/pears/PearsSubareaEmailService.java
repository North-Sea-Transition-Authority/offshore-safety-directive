package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Service
class PearsSubareaEmailService {

  static final RequestPurpose LICENCE_QUERY_REQUEST_PURPOSE =
      new RequestPurpose("Get licence for PEARS transaction");

  static final RequestPurpose LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE =
      new RequestPurpose("Get removed subareas");

  private static final Logger LOGGER = LoggerFactory.getLogger(PearsSubareaEmailService.class);

  private final EmailService emailService;
  private final LicenceQueryService licenceQueryService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final EmailUrlGenerationService emailUrlGenerationService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final TeamQueryService teamQueryService;

  @Autowired
  PearsSubareaEmailService(EmailService emailService,
                           LicenceQueryService licenceQueryService,
                           LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                           EmailUrlGenerationService emailUrlGenerationService,
                           CustomerConfigurationProperties customerConfigurationProperties,
                           TeamQueryService teamQueryService) {
    this.emailService = emailService;
    this.licenceQueryService = licenceQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.emailUrlGenerationService = emailUrlGenerationService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.teamQueryService = teamQueryService;
  }

  public void sendForwardAreaApprovalTerminationNotifications(String transactionId,
                                                              String licenceId,
                                                              Collection<LicenceBlockSubareaId> subareaIds) {

    var removedSubareaDtos = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        subareaIds,
        LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE
    );

    var subareaNames = removedSubareaDtos.stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .map(LicenceBlockSubareaDto::displayName)
        .toList();

    var template = getTemplate(subareaNames);

    var licenceProjectionWithLicensees = LicenceQueryService.SINGLE_LICENCE_PROJECTION_ROOT
        .licensees().organisationGroups().organisationGroupId().root();

    var numericLicenceId = Integer.parseInt(licenceId);
    var licence = licenceQueryService.getLicenceById(
            new LicenceId(numericLicenceId),
            LICENCE_QUERY_REQUEST_PURPOSE,
            licenceProjectionWithLicensees
        )
        .orElseThrow(() -> new IllegalStateException("No licence found with ID [%d]".formatted(numericLicenceId)));

    var orgGroupPortalIds = licence.licensees()
        .stream()
        .flatMap(organisationUnit ->
            Optional.ofNullable(organisationUnit.getOrganisationGroups())
                .orElse(List.of())
                .stream()
        )
        .map(OrganisationGroup::getOrganisationGroupId)
        .map(Objects::toString)
        .toList();

    var teams = teamQueryService.getScopedTeams(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", orgGroupPortalIds);

    Set<EnergyPortalUserDto> usersToEmail = new HashSet<>();

    teams.forEach(team -> {
      Map<Role, Set<EnergyPortalUserDto>> usersInTeam =  teamQueryService
          .getUsersInScopedTeam(TeamType.ORGANISATION_GROUP, TeamScopeReference.from(team.getScopeId(), team.getScopeType()));

      Set<EnergyPortalUserDto> users = usersInTeam.entrySet()
          .stream()
          .filter(userRoles -> Set.of(Role.TEAM_MANAGER, Role.NOMINATION_SUBMITTER).contains(userRoles.getKey()))
          .flatMap(userRoles -> userRoles.getValue().stream())
          .collect(Collectors.toSet());

      usersToEmail.addAll(users);
    });

    Set<String> teamScopeIds = teams
        .stream()
        .map(Team::getScopeId)
        .collect(Collectors.toSet());

    var nonInformedOrganisationIds = orgGroupPortalIds
        .stream()
        .filter(organisationGroupId -> !teamScopeIds.contains(organisationGroupId))
        .collect(Collectors.toSet());

    if (CollectionUtils.isNotEmpty(nonInformedOrganisationIds)) {
      var organisationIds = Strings.join(nonInformedOrganisationIds, ',');
      LOGGER.info(
          "The following organisation groups will not be informed of the " +
              "ending forward area approvals: [{}] for transaction [{}]",
          organisationIds,
          transactionId
      );
    } else if (CollectionUtils.isEmpty(orgGroupPortalIds)) {
      var licenseeIds = Optional.ofNullable(licence.licensees()).orElse(Set.of())
          .stream()
          .map(OrganisationUnit::getOrganisationUnitId)
          .map(Objects::toString)
          .collect(Collectors.joining(","));
      LOGGER.info(
          "No organisation groups were available to inform on transaction [{}] for licensee org units [{}] on licence [{}]",
          transactionId,
          licenseeIds,
          licenceId
      );
    }

    var domainReference = DomainReference.from(transactionId, "PEARS_TRANSACTION");

    for (EnergyPortalUserDto user : usersToEmail) {

      var mergedTemplate = template
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, user.forename())
          .merge();

      var sentEmail = emailService.sendEmail(
          mergedTemplate,
          user::emailAddress,
          domainReference
      );

      LOGGER.info(
          "Sending FAA approval end email to user [{}] for transaction [{}] with email id [{}]",
          user.webUserAccountId(),
          transactionId,
          sentEmail.id()
      );
    }

    var approvalsEmail = EmailRecipient.directEmailAddress(customerConfigurationProperties.businessEmailAddress());
    var mergedTemplate = template
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, customerConfigurationProperties.mnemonic())
        .merge();

    var sentEmail = emailService.sendEmail(
        mergedTemplate,
        approvalsEmail,
        domainReference
    );

    LOGGER.info(
        "Sending FAA approval end email to [{}] for transaction [{}] with email id [{}]",
        customerConfigurationProperties.mnemonic(),
        transactionId,
        sentEmail.id()
    );
  }

  private MergedTemplate.MergedTemplateBuilder getTemplate(List<String> subareaNames) {
    return emailService.getTemplate(GovukNotifyTemplate.FORWARD_AREA_APPROVAL_ENDED)
        .withMailMergeField("ENDED_FAA_LIST", subareaNames)
        .withMailMergeField(
            "SERVICE_WORK_AREA_URL",
            emailUrlGenerationService.generateEmailUrl(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea())
            )
        );
  }

}
