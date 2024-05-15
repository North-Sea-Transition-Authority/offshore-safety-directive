package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.offshoresafetydirective.pears.PearsSubareaEmailService.LICENCE_BLOCK_SUBAREA_QUERY_REQUEST_PURPOSE;
import static uk.co.nstauthority.offshoresafetydirective.pears.PearsSubareaEmailService.LICENCE_QUERY_REQUEST_PURPOSE;

import java.util.List;
import java.util.Map;
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
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
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
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
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
  private TeamQueryService teamQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private EmailUrlGenerationService emailUrlGenerationService;

  private PearsSubareaEmailService pearsSubareaEmailService;

  @BeforeEach
  void setUp() {
    pearsSubareaEmailService = new PearsSubareaEmailService(
        emailService,
        licenceQueryService,
        licenceBlockSubareaQueryService,
        emailUrlGenerationService,
        CUSTOMER_CONFIGURATION_PROPERTIES,
        teamQueryService
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

    var team = new Team();

    when(teamQueryService.getScopedTeams(
        TeamType.ORGANISATION_GROUP,
        "ORGANISATION_GROUP",
        List.of(orgGroupId.toString())
    ))
        .thenReturn(Set.of(team));

    var userToEmail = EnergyPortalUserDtoTestUtil.Builder().build();

    when(teamQueryService.getUsersInScopedTeam(
        eq(TeamType.ORGANISATION_GROUP),
        refEq(TeamScopeReference.from(team.getScopeId(), team.getScopeType()))
    ))
        .thenReturn(Map.of(Role.NOMINATION_SUBMITTER, Set.of(userToEmail)));

    var generatedUrl = "/";
    when(emailUrlGenerationService.generateEmailUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .thenReturn(generatedUrl);

    when(emailService.sendEmail(any(), any(), any()))
        .thenReturn(new EmailNotification(UUID.randomUUID().toString()));

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
            DomainReference::getDomainId,
            DomainReference::getDomainType
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
            userToEmail.emailAddress(),
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
            Tuple.tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, userToEmail.forename()),
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

    when(teamQueryService.getScopedTeams(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", List.of()))
        .thenReturn(Set.of());

    var generatedUrl = "/";
    when(emailUrlGenerationService.generateEmailUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .thenReturn(generatedUrl);

    when(emailService.sendEmail(any(), any(), any()))
        .thenReturn(new EmailNotification(UUID.randomUUID().toString()));

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

    verifyNoMoreInteractions(emailService);
  }
}