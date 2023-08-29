package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.mockito.Mockito.doCallRealMethod;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlResponseParser;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
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
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.IncludeEnergyPortalConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;
import uk.co.nstauthority.offshoresafetydirective.validation.ValidationErrorOrderingService;

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
    UpdateRequestInterceptor.class,
    IsCurrentAppointmentInterceptor.class,
    HasNotBeenTerminatedInterceptor.class,
    PermissionService.class,
    WebSecurityConfiguration.class,
    IsMemberOfTeamTypeInterceptor.class
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
  protected NominationDetailService nominationDetailService;

  @MockBean
  protected SamlResponseParser samlResponseParser;

  @MockBean
  protected ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;

  @MockBean
  protected CaseEventQueryService caseEventQueryService;

  @MockBean
  protected AppointmentAccessService appointmentAccessService;

  @MockBean
  protected AppointmentTerminationService appointmentTerminationService;

  @MockBean
  protected RegulatorTeamService regulatorTeamService;

  @MockBean
  protected ConsulteeTeamService consulteeTeamService;

  @MockBean
  protected IndustryTeamService teamService;

  @BeforeEach
  void setupAbstractControllerTest() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @TestConfiguration
  public static class TestConfig {
    @Bean
    public ControllerHelperService controllerHelperService() {
      return new ControllerHelperService(validationErrorOrderingService());
    }

    @Bean
    public ValidationErrorOrderingService validationErrorOrderingService() {
      return new ValidationErrorOrderingService(messageSource());
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
    }
  }
}