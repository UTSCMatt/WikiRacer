package kappa.wikiracer.util;

import org.mindrot.jbcrypt.BCrypt;

public class UserVerification {
  private static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]{3,}$";

  public static boolean usernameIsValid(String username) {
    return username.matches(USERNAME_REGEX);
  }

  public static String createHash(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(12));
  }

  public static boolean checkPassword(String password, String hash) {
    return BCrypt.checkpw(password, hash);
  }
}
