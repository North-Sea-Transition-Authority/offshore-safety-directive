package uk.co.nstauthority.offshoresafetydirective.energyportal.user;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class EnergyPortalAllowedDomainServiceTest {

  private static final String USER_EMAIL = "user@example.com";

  @Mock
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @InjectMocks
  private EnergyPortalAllowedDomainService energyPortalAllowedDomainService;

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_industry(String domain, boolean isAllowed) {
    var industryTeam = new Team(UUID.randomUUID());
    industryTeam.setTeamType(TeamType.ORGANISATION_GROUP);
    industryTeam.setName("industry team");
    industryTeam.setScopeType("SINGLE_ORGANISATION_PROJECTION_ROOT");
    industryTeam.setScopeId("1");

    var orgGroup = new PortalOrganisationGroupDto(
        "1",
        "Org1",
        Set.of(),
        List.of(new OrganisationGroupEmailDomain(domain))
    );

    when(portalOrganisationGroupQueryService.findOrganisationById(eq(1), any(RequestPurpose.class)))
        .thenReturn(Optional.of(orgGroup));

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, industryTeam))
        .isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_regulator(String domain, boolean isAllowed) {
    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("regulator team");

    var orgGroup = new PortalOrganisationGroupDto("123", "regulator", Set.of(), List.of(new OrganisationGroupEmailDomain(domain)));

    when(portalOrganisationGroupQueryService.getRegulatorOrganisationGroup())
        .thenReturn(Optional.of(orgGroup));

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, regTeam))
        .isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_consultee(String domain, boolean isAllowed) {
    var consulteeTeam = new Team(UUID.randomUUID());
    consulteeTeam.setTeamType(TeamType.CONSULTEE);

    var orgGroup = new PortalOrganisationGroupDto("123", "consultee", Set.of(), List.of(new OrganisationGroupEmailDomain(domain)));

    when(portalOrganisationGroupQueryService.getConsulteeOrganisationGroup())
        .thenReturn(Optional.of(orgGroup));

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, consulteeTeam))
        .isEqualTo(isAllowed);
  }

  private static Stream<Arguments> provideDomainIsAllowedCombinations() {
    return Stream.of(
        Arguments.of("example.com", true),
        Arguments.of("domain.com", false)
    );
  }
}