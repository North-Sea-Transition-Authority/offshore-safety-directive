package uk.co.nstauthority.offshoresafetydirective.mvc;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNotBeenTerminatedInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasTeamPermissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointmentInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeamTypeInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.UpdateRequestInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;

@Configuration
class WebMvcConfiguration implements WebMvcConfigurer {

  private static final String ASSETS_PATH = "/assets/**";
  private static final String SYSTEM_OF_RECORD_PATH = "/system-of-record/**";

  private static final List<String> UNAUTHENTICATED_URL_PATHS = List.of(
      ASSETS_PATH, SYSTEM_OF_RECORD_PATH
  );

  private final PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor;

  private final NominationInterceptor nominationInterceptor;

  private final HasPermissionInterceptor hasPermissionInterceptor;
  private final HasTeamPermissionInterceptor hasTeamPermissionInterceptor;
  private final UpdateRequestInterceptor updateRequestInterceptor;
  private final IsCurrentAppointmentInterceptor isCurrentAppointmentInterceptor;
  private final HasNotBeenTerminatedInterceptor hasNotBeenTerminatedInterceptor;
  private final IsMemberOfTeamTypeInterceptor isMemberOfTeamTypeInterceptor;

  @Autowired
  WebMvcConfiguration(PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor,
                      NominationInterceptor nominationInterceptor,
                      HasPermissionInterceptor hasPermissionInterceptor,
                      HasTeamPermissionInterceptor hasTeamPermissionInterceptor,
                      UpdateRequestInterceptor updateRequestInterceptor,
                      IsCurrentAppointmentInterceptor isCurrentAppointmentInterceptor,
                      HasNotBeenTerminatedInterceptor hasNotBeenTerminatedInterceptor,
                      IsMemberOfTeamTypeInterceptor isMemberOfTeamTypeInterceptor) {
    this.permissionManagementHandlerInterceptor = permissionManagementHandlerInterceptor;
    this.nominationInterceptor = nominationInterceptor;
    this.hasPermissionInterceptor = hasPermissionInterceptor;
    this.hasTeamPermissionInterceptor = hasTeamPermissionInterceptor;
    this.updateRequestInterceptor = updateRequestInterceptor;
    this.isCurrentAppointmentInterceptor = isCurrentAppointmentInterceptor;
    this.hasNotBeenTerminatedInterceptor = hasNotBeenTerminatedInterceptor;
    this.isMemberOfTeamTypeInterceptor = isMemberOfTeamTypeInterceptor;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(ASSETS_PATH)
        .addResourceLocations("classpath:/public/assets/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
        .resourceChain(false)
        .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ResponseBufferSizeHandlerInterceptor())
        .excludePathPatterns(ASSETS_PATH);
    registry.addInterceptor(permissionManagementHandlerInterceptor)
        .addPathPatterns("/permission-management/**");
    registry.addInterceptor(hasTeamPermissionInterceptor)
        .addPathPatterns("/permission-management/**");
    registry.addInterceptor(nominationInterceptor)
        .addPathPatterns("/nomination/**");
    registry.addInterceptor(updateRequestInterceptor)
        .addPathPatterns("/nomination/**");
    registry.addInterceptor(hasPermissionInterceptor)
        .excludePathPatterns(UNAUTHENTICATED_URL_PATHS);
    registry.addInterceptor(isCurrentAppointmentInterceptor)
        .addPathPatterns("/appointment/**");
    registry.addInterceptor(hasNotBeenTerminatedInterceptor)
        .addPathPatterns("/appointment/**");
    registry.addInterceptor(isMemberOfTeamTypeInterceptor)
        .addPathPatterns("/appointment/**", "/nomination/**");
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }
}
