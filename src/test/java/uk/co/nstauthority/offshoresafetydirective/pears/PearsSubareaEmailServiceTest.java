package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.offshoresafetydirective.pears.PearsSubareaEmailService.LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE;
import static uk.co.nstauthority.offshoresafetydirective.pears.PearsSubareaEmailService.LICENCE_QUERY_REQUEST_PURPOSE;
import static uk.co.nstauthority.offshoresafetydirective.pears.PearsSubareaEmailService.USER_QUERY_REQUEST_PURPOSE;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.EpaOrganisationGroupTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.EpaOrganisationUnitTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class PearsSubareaEmailServiceTest {

  private static final String TRANSACTION_ID = "transaction-id";
  private static final Integer LICENCE_ID = 1245;
  private static final CustomerConfigurationProperties CUSTOMER_CONFIGURATION_PROPERTIES =
      CustomerConfigurationPropertiesTestUtil.builder().build();

  @Mock
  private EmailService emailService;

  @Mock
  private LicenceQueryService licenceQueryService;

  @Mock
  private TeamScopeService teamScopeService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private EmailUrlGenerationService emailUrlGenerationService;

  private PearsSubareaEmailService pearsSubareaEmailService;

  @BeforeEach
  void setUp() {
    pearsSubareaEmailService = new PearsSubareaEmailService(
        emailService,
        licenceQueryService,
        teamScopeService,
        teamMemberService,
        licenceBlockSubareaQueryService,
        energyPortalUserService,
        emailUrlGenerationService,
        CUSTOMER_CONFIGURATION_PROPERTIES
    );
  }

  @Test
  void sendForwardAreaApprovalTerminationNotifications_verifyCalls() {
    var firstTerminatedSubareaId = new LicenceBlockSubareaId("subarea-%s".formatted(UUID.randomUUID()));
    var secondTerminatedSubareaId = new LicenceBlockSubareaId("subarea-%s".formatted(UUID.randomUUID()));

    var firstTerminatedSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstTerminatedSubareaId.id())
        .withLicenceNumber(11)
        .build();
    var secondTerminatedSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(secondTerminatedSubareaId.id())
        .withLicenceNumber(100)
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(firstTerminatedSubareaId, secondTerminatedSubareaId),
        LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(secondTerminatedSubareaDto, firstTerminatedSubareaDto));

    var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));
    when(emailService.getTemplate(GovukNotifyTemplate.FORWARD_AREA_APPROVAL_ENDED))
        .thenReturn(template);

    Integer orgGroupId = 111;
    var organisationGroup = EpaOrganisationGroupTestUtil.builder()
        .withId(orgGroupId)
        .build();
    var organisationUnit = EpaOrganisationUnitTestUtil.builder()
        .withOrganisationGroup(organisationGroup)
        .build();
    var licenceDto = LicenceDtoTestUtil.builder()
        .withLicensees(Set.of(organisationUnit))
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(LICENCE_ID),
        LICENCE_QUERY_REQUEST_PURPOSE,
        LicenceQueryService.SINGLE_LICENCE_PROJECTION_ROOT
            .licensees().organisationGroups().organisationGroupId().root()
    ))
        .thenReturn(Optional.of(licenceDto));

    var team = TeamTestUtil.Builder().build();
    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .build();
    when(teamScopeService.getTeamScope(List.of(orgGroupId.toString()), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(teamScope));

    var teamMemberToEmail = TeamMemberTestUtil.Builder().build();
    when(teamMemberService.getTeamMembersOfTeamsWithAnyRoleOf(
        Set.of(team),
        Set.of(
            IndustryTeamRole.ACCESS_MANAGER.name(),
            IndustryTeamRole.NOMINATION_SUBMITTER.name()
        )))
        .thenReturn(Set.of(teamMemberToEmail));

    var userDto = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaIds(
        Set.of(teamMemberToEmail.wuaId()),
        USER_QUERY_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(userDto));

    var generatedUrl = "/";
    when(emailUrlGenerationService.generateEmailUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .thenReturn(generatedUrl);

    pearsSubareaEmailService.sendForwardAreaApprovalTerminationNotifications(
        TRANSACTION_ID,
        LICENCE_ID.toString(),
        List.of(firstTerminatedSubareaId, secondTerminatedSubareaId)
    );

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService, times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(domainReferenceCaptor.getAllValues())
        .extracting(
            DomainReference::getId,
            DomainReference::getType
        )
        .containsExactly(
            Tuple.tuple(
                TRANSACTION_ID,
                "PEARS_TRANSACTION"
            ),
            Tuple.tuple(
                TRANSACTION_ID,
                "PEARS_TRANSACTION"
            )
        );

    assertThat(emailRecipientCaptor.getAllValues())
        .extracting(EmailRecipient::getEmailAddress)
        .containsExactly(
            userDto.emailAddress(),
            CUSTOMER_CONFIGURATION_PROPERTIES.businessEmailAddress()
        );

    var mailMergeFields = templateCaptor.getAllValues()
        .stream()
        .map(MergedTemplate::getMailMergeFields)
        .toList();

    assertThat(mailMergeFields).hasSize(2);

    assertThat(mailMergeFields.get(0))
        .extracting(MailMergeField::name, MailMergeField::value)
        .contains(
            Tuple.tuple("ENDED_FAA_LIST", List.of(
                firstTerminatedSubareaDto.displayName(),
                secondTerminatedSubareaDto.displayName()
            )),
            Tuple.tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, userDto.forename()),
            Tuple.tuple("SERVICE_WORK_AREA_URL", generatedUrl)
        );

    assertThat(mailMergeFields.get(1))
        .extracting(MailMergeField::name, MailMergeField::value)
        .contains(
            Tuple.tuple("ENDED_FAA_LIST", List.of(
                firstTerminatedSubareaDto.displayName(),
                secondTerminatedSubareaDto.displayName()
            )),
            Tuple.tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CUSTOMER_CONFIGURATION_PROPERTIES.mnemonic()),
            Tuple.tuple("SERVICE_WORK_AREA_URL", generatedUrl)
        );
  }

  @Test
  void sendForwardAreaApprovalTerminationNotifications_whenNoOrganisationGroupForUnit_verifyNoEmailsSent() {
    var firstTerminatedSubareaId = new LicenceBlockSubareaId("subarea-%s".formatted(UUID.randomUUID()));

    var terminatedSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(firstTerminatedSubareaId.id())
        .withLicenceNumber(11)
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(firstTerminatedSubareaId),
        LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(terminatedSubareaDto));

    var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));
    when(emailService.getTemplate(GovukNotifyTemplate.FORWARD_AREA_APPROVAL_ENDED))
        .thenReturn(template);

    var organisationUnit = EpaOrganisationUnitTestUtil.builder()
        .withOrganisationGroups(null)
        .build();
    var licenceDto = LicenceDtoTestUtil.builder()
        .withLicensees(Set.of(organisationUnit))
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(LICENCE_ID),
        LICENCE_QUERY_REQUEST_PURPOSE,
        LicenceQueryService.SINGLE_LICENCE_PROJECTION_ROOT
            .licensees().organisationGroups().organisationGroupId().root()
    ))
        .thenReturn(Optional.of(licenceDto));

    when(teamScopeService.getTeamScope(List.of(), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of());

    when(teamMemberService.getTeamMembersOfTeamsWithAnyRoleOf(
        Set.of(),
        Set.of(
            IndustryTeamRole.ACCESS_MANAGER.name(),
            IndustryTeamRole.NOMINATION_SUBMITTER.name()
        )))
        .thenReturn(Set.of());

    when(energyPortalUserService.findByWuaIds(
        Set.of(),
        USER_QUERY_REQUEST_PURPOSE
    ))
        .thenReturn(List.of());

    var generatedUrl = "/";
    when(emailUrlGenerationService.generateEmailUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .thenReturn(generatedUrl);

    pearsSubareaEmailService.sendForwardAreaApprovalTerminationNotifications(
        TRANSACTION_ID,
        LICENCE_ID.toString(),
        List.of(firstTerminatedSubareaId)
    );

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(CUSTOMER_CONFIGURATION_PROPERTIES.businessEmailAddress());

    verifyNoMoreInteractions(energyPortalUserService, teamMemberService, teamScopeService, emailService);
  }

  @Test
  void getOrganisationIdsThatWillNotBeInformed_whenAllOrgsInformed_thenEmptyList() {
    var organisationId = "org-id";
    var teamId = UUID.randomUUID();
    var team = TeamTestUtil.Builder()
        .withId(teamId)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TeamId.valueOf(teamId))
        .build();
    var teamScope = TeamScopeTestUtil.builder()
        .withPortalId(organisationId)
        .withTeam(team)
        .build();
    var result = pearsSubareaEmailService.getOrganisationIdsThatWillNotBeInformed(
        List.of(organisationId),
        List.of(teamScope),
        List.of(teamMember)
    );

    assertThat(result).isEmpty();
  }

  @Test
  void getOrganisationIdsThatWillNotBeInformed_whenSomeOrgsInformed_thenAssertOrganisation() {
    var informedOrganisationId = "org-id-%s".formatted(UUID.randomUUID());
    var uninformedOrganisationId = "org-id-%s".formatted(UUID.randomUUID());
    var teamId = UUID.randomUUID();
    var team = TeamTestUtil.Builder()
        .withId(teamId)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TeamId.valueOf(teamId))
        .build();
    var teamScope = TeamScopeTestUtil.builder()
        .withPortalId(informedOrganisationId)
        .withTeam(team)
        .build();
    var result = pearsSubareaEmailService.getOrganisationIdsThatWillNotBeInformed(
        List.of(informedOrganisationId, uninformedOrganisationId),
        List.of(teamScope),
        List.of(teamMember)
    );

    assertThat(result).containsExactly(uninformedOrganisationId);
  }

  @Test
  void getOrganisationIdsThatWillNotBeInformed_whenNoTeamMemberForOrg_thenAssertOrganisation() {
    var organisationId = "org-id-%s".formatted(UUID.randomUUID());
    var teamId = UUID.randomUUID();
    var team = TeamTestUtil.Builder()
        .withId(teamId)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TeamId.valueOf(UUID.randomUUID()))
        .build();
    var teamScope = TeamScopeTestUtil.builder()
        .withPortalId(organisationId)
        .withTeam(team)
        .build();
    var result = pearsSubareaEmailService.getOrganisationIdsThatWillNotBeInformed(
        List.of(organisationId),
        List.of(teamScope),
        List.of(teamMember)
    );

    assertThat(result).containsExactly(organisationId);
  }
}