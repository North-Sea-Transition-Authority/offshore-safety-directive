package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

@ExtendWith(SpringExtension.class)
class ReverseRouterTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {

    Map<String, Object> uriTemplateVariablesMap = Map.of(
        "parentId", "request_parent_id",
        "childId", "request_child_id"
    );

    var mockHttpServletRequest = new MockHttpServletRequest();
    var servletRequestAttributes = new ServletRequestAttributes(mockHttpServletRequest);

    servletRequestAttributes.setAttribute(
        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
        uriTemplateVariablesMap,
        RequestAttributes.SCOPE_REQUEST
    );

    RequestContextHolder.setRequestAttributes(servletRequestAttributes);
  }

  @Test
  void route_methodCallVariant() {
    var route = ReverseRouter.route(on(TestController.class).testMethod("method_child_id"));
    assertThat(route).isEqualTo("/parent/request_parent_id/child/method_child_id");
  }

  @Test
  void route_whenUriVariablesVariant_thenVerifyRoute() {
    // variable from request should be overridden by variable in map
    var route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"),
        Map.of("parentId", "map_parent_id")
    );
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");
  }

  @Test
  void route_whenExpandUriVariablesFromRequestFalse_thenVariableFromRequestOverriddenMyMap() {
    // variable from request should be overridden by variable in map even when request substitution is disabled
    var route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"),
        Map.of("parentId", "map_parent_id"),
        false
    );
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");
  }

  @Test
  void route_whenMethodParamNotOverriddenByVariableInMap_thenVerifyRoute() {
    // variable from method parameter should NOT be overridden by variable in map
    var route = ReverseRouter.route(
        on(TestController.class).testMethod("method_child_id"),
        Map.of(
            "parentId", "map_parent_id",
            "childId", "map_child_id"
        )
    );
    assertThat(route).isEqualTo("/parent/map_parent_id/child/method_child_id");
  }

  @Test
  void route_whenNoVariablesAllowedFromRequest_thenException() {
    // should throw exception if we don't allow variables from the request
    assertThatThrownBy(
        () -> ReverseRouter.route(on(TestController.class).testMethod("method_child_id"), Map.of(), false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Map has no value");
  }

  @Test
  void redirect_whenMethodCallVariant_thenVerifyRoute() {
    // redirect should behave as route does, with "redirect:/" prefix on results
    var redirectModelAndView = ReverseRouter.redirect(on(TestController.class).testMethod("method_child_id"));
    assertThat(redirectModelAndView.getViewName()).isEqualTo("redirect:/parent/request_parent_id/child/method_child_id");
  }

  @Test
  void redirect_whenUriVariablesVariant_thenVerifyRoute() {
    // redirect should behave as route does, with "redirect:/" prefix on results
    var redirectModelAndView = ReverseRouter.redirect(
        on(TestController.class).testMethod("method_child_id"),
        Map.of("parentId", "map_parent_id")
    );
    assertThat(redirectModelAndView.getViewName()).isEqualTo("redirect:/parent/map_parent_id/child/method_child_id");
  }

  @Test
  void redirect_whenExpandUriVariablesFromRequest_thenVerifyRoute() {
    // redirect should behave as route does, with "redirect:/" prefix on results
    var redirectModelAndView = ReverseRouter.redirect(
        on(TestController.class).testMethod("method_child_id"),
        Map.of("parentId", "map_parent_id"),
        false
    );
    assertThat(redirectModelAndView.getViewName()).isEqualTo("redirect:/parent/map_parent_id/child/method_child_id");
  }

  @Test
  void emptyBindingResult_verifyResponse() {
    var emptyBindingResult = (BeanPropertyBindingResult) ReverseRouter.emptyBindingResult();
    assertThat(emptyBindingResult.getTarget()).isNull();
    assertThat(emptyBindingResult.getObjectName()).isEqualTo("empty");
  }

  // Dummy application to stop the @WebMvcTest loading more than it needs
  @SpringBootApplication
  static class TestApplication {
  }

  @RequestMapping("/parent/{parentId}")
  static class TestController {

    @GetMapping("/child/{childId}")
    ModelAndView testMethod(@PathVariable String childId) {
      return new ModelAndView(childId);
    }
  }
}