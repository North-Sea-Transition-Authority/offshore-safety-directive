package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = ErrorListHandlerInterceptorTest.ErrorListHandlerTestController.class)

class ErrorListHandlerInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getErrorItems_whenHasBindingResult_thenIncludeErrorList() throws Exception {

    var errorList = List.of(new ErrorItem(1, "ErrorMessage", "default message"));
    when(formErrorSummaryService.getErrorItems(any(BindingResult.class)))
        .thenReturn(errorList);

    mockMvc.perform(
        post(ReverseRouter.route(on(ErrorListHandlerTestController.class)
            .endpointWithBindingResult(new Form(), null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(view().name(ErrorListHandlerTestController.VIEW_NAME))
        .andExpect(model().attribute("errorList", errorList));
  }


  @Test
  void getErrorItems_whenNoBindingResult_thenNoErrorList() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(ErrorListHandlerTestController.class)
                .endpointWithoutBindingResult()))
                .with(user(USER))
                .with(csrf()))
        .andExpect(view().name(ErrorListHandlerTestController.VIEW_NAME))
        .andExpect(model().attributeDoesNotExist("errorList"));
  }

  @Controller
  @RequestMapping
  static class ErrorListHandlerTestController {

    static final String VIEW_NAME = "osd/error/notFound.ftl";

    @PostMapping("no-binding-result")
    public ModelAndView endpointWithoutBindingResult() {
      return new ModelAndView(VIEW_NAME);
    }

    @PostMapping("with-binding-result")
    public ModelAndView endpointWithBindingResult(@ModelAttribute("form") Form form, BindingResult bindingResult) {
      return new ModelAndView(VIEW_NAME);
    }

  }

  static class Form {
    private String field;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }
  }
}