package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@ExtendWith(SpringExtension.class)
class WorkAreaServiceTest {

  private WorkAreaService workAreaService;

  @BeforeEach
  void setup() {
    workAreaService = new WorkAreaService();
  }

  @Test
  void getModelAndView_assertModelProperties() {
    var modelAndView = workAreaService.getModelAndView();

    assertThat(modelAndView.getModel()).containsExactly(
        entry("startNominationUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()))
    );

    assertEquals("osd/workarea/workArea", modelAndView.getViewName());
  }
}