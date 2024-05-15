package uk.co.nstauthority.offshoresafetydirective.exception;

public class IllegalUtilClassInstantiationException extends IllegalStateException {

  public IllegalUtilClassInstantiationException(Class<?> utilClass) {
    super("%s is a util class and should not be instantiated".formatted(utilClass.getName()));
  }
}
