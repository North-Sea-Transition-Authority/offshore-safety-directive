package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.DefaultModelAttributeService;

@ExtendWith(MockitoExtension.class)
class DefaultExceptionResolverTest {

  @Mock
  private ErrorModelService errorModelService;

  @Mock
  private DefaultModelAttributeService defaultModelAttributeService;

  @InjectMocks
  private DefaultExceptionResolver defaultExceptionResolver;

  @Test
  void getModelAndView_whenClientAbortException_thenReturnNull() {

    var clientAbortException = new ClientAbortException("client-abort");

    defaultExceptionResolver.getModelAndView("view-name", clientAbortException);

    then(defaultModelAttributeService)
        .should(never())
        .addDefaultModelAttributes(anyMap());

    then(errorModelService)
        .should(never())
        .addErrorModelProperties(any(ModelAndView.class), any(Throwable.class), any());
  }

  @Test
  void getModelAndView_whenNotClientAbortException_verifyModelInteractions() {

    var notClientAbortException = new NullPointerException();

    defaultExceptionResolver.getModelAndView("view-name", notClientAbortException);

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap());

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), eq(notClientAbortException), eq(HttpStatus.INTERNAL_SERVER_ERROR));
  }
}