package kappa.wikiracer.exception;

public class UserNotFoundException extends Exception {

  public UserNotFoundException(String ex) {
    super(ex);
  }

  public UserNotFoundException() {
    super();
  }
}
