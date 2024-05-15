package uk.co.nstauthority.offshoresafetydirective.interceptorutil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;

public class AssetInterceptorUtil {

  private AssetInterceptorUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Optional<AssetId> extractAssetIdFromRequest(HttpServletRequest httpServletRequest,
                                                            HandlerMethod handlerMethod) {

    var assetIdParameter = AbstractHandlerInterceptor.getPathVariableByClass(handlerMethod, AssetId.class);

    if (assetIdParameter.isEmpty()) {
      return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    var pathVariables = (Map<String, String>) httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    var assetId = AssetId.valueOf(pathVariables.get(assetIdParameter.get().getName()));

    return Optional.of(assetId);
  }
}
