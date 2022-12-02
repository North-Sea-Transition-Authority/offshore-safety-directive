package uk.co.nstauthority.offshoresafetydirective.mvc;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.PermissionManagementHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorPermissionManagementHandlerInterceptor;

@Configuration
class WebMvcConfiguration implements WebMvcConfigurer {

  private static final String ASSETS_PATH = "/assets/**";

  private final PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor;
  private final RegulatorPermissionManagementHandlerInterceptor regulatorPermissionManagementHandlerInterceptor;

  private final NominationInterceptor nominationInterceptor;

  private final HasPermissionInterceptor hasPermissionInterceptor;

  @Autowired
  WebMvcConfiguration(PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor,
                      RegulatorPermissionManagementHandlerInterceptor regulatorPermissionManagementHandlerInterceptor,
                      NominationInterceptor nominationInterceptor,
                      HasPermissionInterceptor hasPermissionInterceptor) {
    this.permissionManagementHandlerInterceptor = permissionManagementHandlerInterceptor;
    this.regulatorPermissionManagementHandlerInterceptor = regulatorPermissionManagementHandlerInterceptor;
    this.nominationInterceptor = nominationInterceptor;
    this.hasPermissionInterceptor = hasPermissionInterceptor;
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
    registry.addInterceptor(regulatorPermissionManagementHandlerInterceptor)
        .addPathPatterns("/permission-management/regulator/**");
    registry.addInterceptor(nominationInterceptor)
        .addPathPatterns("/nomination/**");
    registry.addInterceptor(hasPermissionInterceptor)
        .excludePathPatterns(ASSETS_PATH);
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }
}
