package kappa.wikiracer.util;

import org.mindrot.jbcrypt.BCrypt;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class UserVerification {

  private static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]{3,}$";

  public static boolean usernameIsValid(String username) {
    PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
    String sanitized = policy.sanitize(username);
    return username.matches(USERNAME_REGEX) && sanitized.equals(username);
  }

  public static String createHash(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(12));
  }

  public static boolean checkPassword(String password, String hash) {
    return BCrypt.checkpw(password, hash);
  }
}
