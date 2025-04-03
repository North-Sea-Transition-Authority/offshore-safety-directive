package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.mockito.Mockito.doCallRealMethod;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlResponseParser;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailArgumentResolver;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.SamlProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.WebSecurityConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.IncludeEnergyPortalConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
import uk.co.nstauthority.offshoresafetydirective.jooq.JooqStatisticsListener;
import uk.co.nstauthority.offshoresafetydirective.jpa.HibernateQueryCounter;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.metrics.QueryCounter;
import uk.co.nstauthority.offshoresafetydirective.mvc.error.ErrorListHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanViewNominationPostSubmissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationRoleService;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.StartNominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.UpdateRequestInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasAppointmentStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasAssetStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.HasNotBeenTerminatedInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation.IsCurrentAppointmentInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;
import uk.co.nstauthority.offshoresafetydirective.teams.management.access.TeamManagementHandlerInterceptor;

@ActiveProfiles({"test", "development"})
@AutoConfigureMockMvc
@IncludeServiceBrandingConfigurationProperties
@IncludeEnergyPortalConfigurationProperties
@WithDefaultPageControllerAdvice
@WebMvcTest
@Import({
    AbstractControllerTest.TestConfig.class,
    WebMvcConfiguration.class,
    NominationInterceptor.class,
    IsCurrentAppointmentInterceptor.class,
    HasNotBeenTerminatedInterceptor.class,
    HasAppointmentStatusInterceptor.class,
    WebSecurityConfiguration.class,
    HasAssetStatusInterceptor.class,
    RequestLogFilter.class,
    PostAuthenticationRequestMdcFilter.class,
    MetricsProvider.class,
    ErrorListHandlerInterceptor.class,
    UpdateRequestInterceptor.class,
    CanViewNominationPostSubmissionInterceptor.class,
    TeamManagementHandlerInterceptor.class,
    ServiceUserDetailArgumentResolver.class,
    StartNominationInterceptor.class
})
@EnableConfigurationProperties(value = {SamlProperties.class, AnalyticsProperties.class})
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @MockitoBean
  protected UserDetailService userDetailService;

  @MockitoBean
  protected SamlResponseParser samlResponseParser;

  @MockitoBean
  protected ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;

  @MockitoBean
  protected CaseEventQueryService caseEventQueryService;

  @MockitoBean
  protected AppointmentAccessService appointmentAccessService;

  @MockitoBean
  protected AssetAccessService assetAccessService;

  @MockitoBean
  protected AppointmentTerminationService appointmentTerminationService;

  @MockitoBean
  protected JooqStatisticsListener jooqStatisticsListener;

  @MockitoBean
  protected MeterRegistry registry;

  @MockitoBean
  protected FormErrorSummaryService formErrorSummaryService;

  @MockitoBean
  protected PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockitoBean
  protected PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @MockitoBean
  protected NominationDetailService nominationDetailService;

  @MockitoBean
  protected ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @MockitoBean
  protected PortalAssetRetrievalService portalAssetRetrievalService;

  @MockitoBean
  protected AppointmentCorrectionService appointmentCorrectionService;

  @MockitoBean
  protected LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @MockitoBean
  protected WellQueryService wellQueryService;

  @MockitoBean
  protected TeamManagementService teamManagementService;

  @MockitoBean
  protected TeamQueryService teamQueryService;

  @MockitoBean
  protected NominationRoleService nominationRoleService;

  @BeforeEach
  void setupAbstractControllerTest() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public QueryCounter queryCounter() {
      return new QueryCounter();
    }

    @Bean
    public HibernateQueryCounter hibernateQueryInterceptor(QueryCounter queryCounter) {
      return new HibernateQueryCounter(queryCounter);
    }

    @Bean
    public AppointmentModelAndViewService appointmentModelAndViewService(PortalAssetRetrievalService portalAssetRetrievalService,
                                                                         AppointmentCorrectionService appointmentCorrectionService,
                                                                         NominationDetailService nominationDetailService,
                                                                         AppointmentAccessService appointmentAccessService,
                                                                         LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                                                         PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                                                         WellQueryService wellQueryService) {
      return new AppointmentModelAndViewService(
          portalAssetRetrievalService,
          appointmentCorrectionService,
          nominationDetailService,
          appointmentAccessService,
          licenceBlockSubareaQueryService,
          portalOrganisationUnitQueryService,
          wellQueryService
      );
    }
  }
}
