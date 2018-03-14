package kappa.wikiracer.api;

import java.sql.SQLException;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import kappa.wikiracer.dao.LinkDao;
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

  @RequestMapping(value = "/api/test/", method = RequestMethod.POST)
  public ResponseEntity<String> test(String parent, String child) {
    try {
      return new ResponseEntity<String>(hasLink(parent, child).toString(), HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /*** Testing API Ends ***/

  private void setSession(HttpServletRequest req, HttpServletResponse res, String username) {
    req.getSession().setAttribute("username", username);
    res.setHeader("Set-Cookie", "JSESSIONID=" + req.getSession().getId() + "; HttpOnly; SameSite=strict");
  }

  private void invalidateSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }

  private Boolean hasLink(String parentTitle, String childTitle) throws SQLException {
    Set<String> links = new LinkDao(dbUrl, dbUsername, dbPassword).getLinks(parentTitle);
    if (links.contains(childTitle)) {
      return true;
    } else {
      Set<String> updatedLinks = LinkRequest.sendRequest(parentTitle);
      if (updatedLinks.contains(childTitle)) {
        updatedLinks.removeAll(links);
        new LinkDao(dbUrl, dbUsername, dbPassword).addLinks(parentTitle, updatedLinks);
        return true;
      } else {
        return false;
      }
    }
  }

  /*** API BEGINS ***/

  @RequestMapping(value = "/login/", method = RequestMethod.POST)
  public ResponseEntity<String> login(HttpServletRequest req, HttpServletResponse res, String username, String password) {
    try {
      String hash = new UserDao(dbUrl, dbUsername, dbPassword).getUserPasswordHash(username);
      if (UserVerification.checkPassword(password, hash)) {
        invalidateSession(req);
        setSession(req, res, username);
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

  @RequestMapping(value = "/signup/", method = RequestMethod.POST)
  public ResponseEntity<String> signup(HttpServletRequest req, HttpServletResponse res, String username, String password) {
    username = StringUtils.trimToEmpty(username);
    password = StringUtils.trimToEmpty(password);
    if (!UserVerification.usernameIsValid(username)) {
      return new ResponseEntity<String>("Username invalid", HttpStatus.BAD_REQUEST);
    }
    try {
      new UserDao(dbUrl, dbUsername, dbPassword)
          .createUser(username, UserVerification.createHash(password));
      invalidateSession(req);
      setSession(req, res, username);
      return new ResponseEntity<String>("Success", HttpStatus.OK);
    } catch (SQLException ex) {
      if (ex.getMessage().equals("Username already in use")) {
        return new ResponseEntity<String>("Username in use", HttpStatus.CONFLICT);
      }
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/logoff/", method = RequestMethod.GET)
  public ResponseEntity<String> logoff(HttpServletRequest req, HttpServletResponse res) {
    invalidateSession(req);
    for (Cookie cookie : req.getCookies()) {
      cookie.setMaxAge(0);
      cookie.setValue(null);
      cookie.setPath("/");
      res.addCookie(cookie);
    }
    return new ResponseEntity<String>("Logged off", HttpStatus.OK);
  }

  /*** API ENDS ***/

}
