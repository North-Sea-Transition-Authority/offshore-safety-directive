package uk.co.nstauthority.offshoresafetydirective.authentication;

class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
