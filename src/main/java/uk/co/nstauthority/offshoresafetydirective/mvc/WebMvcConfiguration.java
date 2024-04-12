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
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanViewNominationPostSubmissionInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAppointmentStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasAssetStatusInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNotBeenTerminatedInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointmentInterceptor;
import uk.co.nstauthority.offshoresafetydirective.authorisation.UpdateRequestInterceptor;
import uk.co.nstauthority.offshoresafetydirective.mvc.error.ErrorListHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationInterceptor;

@Configuration
class WebMvcConfiguration implements WebMvcConfigurer {

  private static final String ASSETS_PATH = "/assets/**";
  private static final String SYSTEM_OF_RECORD_PATH = "/system-of-record/**";

  private static final List<String> UNAUTHENTICATED_URL_PATHS = List.of(
      ASSETS_PATH, SYSTEM_OF_RECORD_PATH
  );


  private final NominationInterceptor nominationInterceptor;
  private final UpdateRequestInterceptor updateRequestInterceptor;
  private final IsCurrentAppointmentInterceptor isCurrentAppointmentInterceptor;
  private final HasNotBeenTerminatedInterceptor hasNotBeenTerminatedInterceptor;
  private final HasAppointmentStatusInterceptor hasAppointmentStatusInterceptor;
  private final HasAssetStatusInterceptor hasAssetStatusInterceptor;
  private final ErrorListHandlerInterceptor errorListHandlerInterceptor;
  private final CanViewNominationPostSubmissionInterceptor canViewNominationPostSubmissionInterceptor;

  @Autowired
  WebMvcConfiguration(NominationInterceptor nominationInterceptor,
                      UpdateRequestInterceptor updateRequestInterceptor,
                      IsCurrentAppointmentInterceptor isCurrentAppointmentInterceptor,
                      HasNotBeenTerminatedInterceptor hasNotBeenTerminatedInterceptor,
                      HasAppointmentStatusInterceptor hasAppointmentStatusInterceptor,
                      HasAssetStatusInterceptor hasAssetStatusInterceptor,
                      ErrorListHandlerInterceptor errorListHandlerInterceptor,
                      CanViewNominationPostSubmissionInterceptor canViewNominationPostSubmissionInterceptor) {
    this.nominationInterceptor = nominationInterceptor;
    this.updateRequestInterceptor = updateRequestInterceptor;
    this.isCurrentAppointmentInterceptor = isCurrentAppointmentInterceptor;
    this.hasNotBeenTerminatedInterceptor = hasNotBeenTerminatedInterceptor;
    this.hasAppointmentStatusInterceptor = hasAppointmentStatusInterceptor;
    this.hasAssetStatusInterceptor = hasAssetStatusInterceptor;
    this.errorListHandlerInterceptor = errorListHandlerInterceptor;
    this.canViewNominationPostSubmissionInterceptor = canViewNominationPostSubmissionInterceptor;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(ASSETS_PATH)
        .addResourceLocations("classpath:/public/assets/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
        .resourceChain(false)
        .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
  }

  // TODO OSDOP-811
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ResponseBufferSizeHandlerInterceptor())
        .excludePathPatterns(ASSETS_PATH);
//    registry.addInterceptor(permissionManagementHandlerInterceptor)
//        .addPathPatterns("/permission-management/**");
//    registry.addInterceptor(hasTeamPermissionInterceptor)
//        .addPathPatterns("/permission-management/**");
    registry.addInterceptor(nominationInterceptor)
        .addPathPatterns("/nomination/**", "/draft-nomination/**");
    registry.addInterceptor(updateRequestInterceptor)
        .addPathPatterns("/nomination/**");
    registry.addInterceptor(canViewNominationPostSubmissionInterceptor)
        .addPathPatterns("/nomination/**");
//    registry.addInterceptor(hasPermissionInterceptor)
//        .excludePathPatterns(UNAUTHENTICATED_URL_PATHS);
    registry.addInterceptor(isCurrentAppointmentInterceptor)
        .addPathPatterns("/appointment/**");
    registry.addInterceptor(hasNotBeenTerminatedInterceptor)
        .addPathPatterns("/appointment/**");
    registry.addInterceptor(hasAppointmentStatusInterceptor)
        .addPathPatterns("/appointment/**");
//    registry.addInterceptor(isMemberOfTeamTypeInterceptor)
//        .addPathPatterns("/appointment/**", "/nomination/**", "/termination/**");
    registry.addInterceptor(hasAssetStatusInterceptor)
        .addPathPatterns("/asset/**", "/appointment/**");
    registry.addInterceptor(errorListHandlerInterceptor)
        .excludePathPatterns(ASSETS_PATH, "/api/**");
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }
}
