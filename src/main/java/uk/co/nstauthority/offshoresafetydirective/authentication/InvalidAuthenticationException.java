package uk.co.nstauthority.offshoresafetydirective.authentication;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
