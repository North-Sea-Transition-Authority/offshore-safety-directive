package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.mockito.Mockito.doCallRealMethod;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlResponseParser;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanViewNominationPostSubmissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAppointmentStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAssetStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNotBeenTerminatedInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointmentInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeamTypeInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.UpdateRequestInterceptor;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.SamlProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.WebSecurityConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.IncludeEnergyPortalConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
import uk.co.nstauthority.offshoresafetydirective.jooq.JooqStatisticsListener;
import uk.co.nstauthority.offshoresafetydirective.jpa.HibernateQueryCounterImpl;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.mvc.error.ErrorListHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@ActiveProfiles({"test", "development"})
@AutoConfigureMockMvc
@IncludeServiceBrandingConfigurationProperties
@IncludeEnergyPortalConfigurationProperties
@WithDefaultPageControllerAdvice
@WebMvcTest
@Import({
    AbstractControllerTest.TestConfig.class,
    WebMvcConfiguration.class,
    PermissionManagementHandlerInterceptor.class,
    HasTeamPermissionInterceptor.class,
    NominationInterceptor.class,
    HasPermissionInterceptor.class,
    IsCurrentAppointmentInterceptor.class,
    HasNotBeenTerminatedInterceptor.class,
    HasAppointmentStatusInterceptor.class,
    PermissionService.class,
    WebSecurityConfiguration.class,
    IsMemberOfTeamTypeInterceptor.class,
    HasAssetStatusInterceptor.class,
    RequestLogFilter.class,
    PostAuthenticationRequestMdcFilter.class,
    MetricsProvider.class,
    ErrorListHandlerInterceptor.class,
    UpdateRequestInterceptor.class,
    CanViewNominationPostSubmissionInterceptor.class
})
@EnableConfigurationProperties(SamlProperties.class)
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected PermissionService permissionService;

  @MockBean
  protected TeamMemberService teamMemberService;

  @MockBean
  protected UserDetailService userDetailService;

  @MockBean
  protected SamlResponseParser samlResponseParser;

  @MockBean
  protected ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;

  @MockBean
  protected CaseEventQueryService caseEventQueryService;

  @MockBean
  protected AppointmentAccessService appointmentAccessService;

  @MockBean
  protected AssetAccessService assetAccessService;

  @MockBean
  protected AppointmentTerminationService appointmentTerminationService;

  @MockBean
  protected RegulatorTeamService regulatorTeamService;

  @MockBean
  protected ConsulteeTeamService consulteeTeamService;

  @MockBean
  protected IndustryTeamService industryTeamService;

  @MockBean
  protected JooqStatisticsListener jooqStatisticsListener;

  @MockBean
  protected MeterRegistry registry;

  @MockBean
  protected FormErrorSummaryService formErrorSummaryService;

  @MockBean
  protected PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  protected PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @MockBean
  protected TeamScopeService teamScopeService;

  @MockBean
  protected NominationDetailService nominationDetailService;

  @MockBean
  protected ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @MockBean
  protected PortalAssetRetrievalService portalAssetRetrievalService;

  @MockBean
  protected AppointmentCorrectionService appointmentCorrectionService;

  @MockBean
  protected LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @BeforeEach
  void setupAbstractControllerTest() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public HibernateQueryCounterImpl hibernateQueryInterceptor() {
      return new HibernateQueryCounterImpl();
    }

    @Bean
    public AppointmentModelAndViewService appointmentModelAndViewService(PortalAssetRetrievalService portalAssetRetrievalService,
                                                                          AppointmentCorrectionService appointmentCorrectionService,
                                                                          NominationDetailService nominationDetailService,
                                                                          AppointmentAccessService appointmentAccessService,
                                                                          LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                                                          PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
      return new AppointmentModelAndViewService(
          portalAssetRetrievalService,
          appointmentCorrectionService,
          nominationDetailService,
          appointmentAccessService,
          licenceBlockSubareaQueryService,
          portalOrganisationUnitQueryService);
    }
  }
}