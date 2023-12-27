package uk.co.nstauthority.offshoresafetydirective.contact;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = ContactInformationController.class)
@EnableConfigurationProperties(value = {
    CustomerConfigurationProperties.class,
    TechnicalSupportConfigurationProperties.class
})
class ContactInformationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Autowired
  private CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  private TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties;

  @SecurityTest
  void getContactInformationPage_assertModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage()))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/contact/contactInformation"))
        .andExpect(model().attribute("pageName", ContactInformationController.PAGE_NAME))
        .andExpect(model().attribute("businessSupport", customerConfigurationProperties))
        .andExpect(model().attribute("technicalSupport", technicalSupportConfigurationProperties));
  }

  @SecurityTest
  void getContactInformationPage_withNoUser_assertStatusOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())))
        .andExpect(status().isOk());
  }
}