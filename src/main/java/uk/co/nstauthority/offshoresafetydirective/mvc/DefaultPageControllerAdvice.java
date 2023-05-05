package uk.co.nstauthority.offshoresafetydirective.mvc;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DefaultPageControllerAdvice {

  private final DefaultModelAttributeService defaultModelAttributeService;

  @Autowired
  DefaultPageControllerAdvice(DefaultModelAttributeService defaultModelAttributeService) {
    this.defaultModelAttributeService = defaultModelAttributeService;
  }

  @ModelAttribute
  void addDefaultModelAttributes(Model model, HttpServletRequest request) {
    defaultModelAttributeService.addDefaultModelAttributes(model.asMap(), request);
  }

  @InitBinder
  void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }
}
