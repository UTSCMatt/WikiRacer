package kappa.wikiracer.api;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import kappa.wikiracer.dao.UserDao;
import kappa.wikiracer.exception.UserNotFoundException;
import kappa.wikiracer.util.UserVerification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kappa.wikiracer.dao.TestDao;
import kappa.wikiracer.wiki.LinkRequest;

@RestController
public class Api {
  
  @Value("${spring.datasource.username}")
  private String dbUsername;
  
  @Value("${spring.datasource.url}")
  private String dbUrl;
  
  @Value("${spring.datasource.password}")
  private String dbPassword;

  /*** Testing API Begins ***/

  @RequestMapping(value = "/api/test/", method = RequestMethod.GET)
  public String test(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      return (String) session.getAttribute("username");
    } else {
      return null;
    }
  }

  @RequestMapping(value = "/api/test/wiki", method = RequestMethod.GET)
  public String wiki(
      @RequestParam(value = "title", defaultValue = "Albert Einstein") String title) {
    return LinkRequest.sendRequest(title);
  }

  /*** Testing API Ends ***/

  private void setSession(HttpServletRequest req, String username) {
    req.getSession().setAttribute("username", username);
  }

  private void invalidateSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }

  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public ResponseEntity<String> login(HttpServletRequest req, String username, String password) {
    try {
      String hash = new UserDao(dbUrl, dbUsername, dbPassword).getUserPasswordHash(username);
      if (UserVerification.checkPassword(password, hash)) {
        invalidateSession(req);
        setSession(req, username);
        return new ResponseEntity<String>("Success", HttpStatus.OK);
      } else {
        return new ResponseEntity<String>("Invalid username or password", HttpStatus.UNAUTHORIZED);
      }
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<String>("Invalid username or password", HttpStatus.UNAUTHORIZED);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  public ResponseEntity<String> signup(HttpServletRequest req, String username, String password) {
    username = StringUtils.trimToEmpty(username);
    password = StringUtils.trimToEmpty(password);
    if (!UserVerification.usernameIsValid(username)) {
      return new ResponseEntity<String>("Username invalid", HttpStatus.BAD_REQUEST);
    }
    try {
      new UserDao(dbUrl, dbUsername, dbPassword)
          .createUser(username, UserVerification.createHash(password));
      invalidateSession(req);
      setSession(req, username);
      return new ResponseEntity<String>("Success", HttpStatus.OK);
    } catch (SQLException ex) {
      if (ex.getMessage().equals("Username already in use")) {
        return new ResponseEntity<String>("Username in use", HttpStatus.CONFLICT);
      }
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
