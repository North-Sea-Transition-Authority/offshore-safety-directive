package uk.co.nstauthority.offshoresafetydirective.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public abstract class AbstractHandlerInterceptor implements HandlerInterceptor {

  public boolean hasAnnotations(HandlerMethod handlerMethod,
                                Set<Class<? extends Annotation>> annotationClasses) {
    return annotationClasses
        .stream()
        .anyMatch(annotationClass -> hasAnnotation(handlerMethod, annotationClass));
  }

  public boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return Optional.ofNullable(AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation)).isPresent()
        || Optional.ofNullable(AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation))
        .isPresent();
  }

  public Annotation getAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return Objects.requireNonNullElse(
        AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation),
        AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation)
    );
  }

  public static Optional<Parameter> getPathVariableByClass(HandlerMethod handlerMethod, Class<?> clazzOfPathVariable) {
    return Arrays.stream(handlerMethod.getMethod().getParameters())
        .filter(methodParameter -> methodParameter.getType().equals(clazzOfPathVariable)
            && methodParameter.isAnnotationPresent(PathVariable.class))
        .findFirst();
  }
}