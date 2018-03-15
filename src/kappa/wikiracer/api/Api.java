package kappa.wikiracer.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import kappa.wikiracer.dao.GameDao;
import kappa.wikiracer.dao.LinkDao;
import kappa.wikiracer.dao.UserDao;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.InvalidArticleException;
import kappa.wikiracer.exception.UserNotFoundException;
import kappa.wikiracer.util.UserVerification;
import kappa.wikiracer.wiki.ExistRequest;
import kappa.wikiracer.wiki.RandomRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    } catch (InvalidArticleException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
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

  private Boolean isAuthenticated(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return session != null && session.getAttribute("username") != null;
  }

  private Boolean hasLink(String parentTitle, String childTitle)
      throws SQLException, InvalidArticleException {
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

  private Boolean inGame(String gameId, String username) throws SQLException {
    return new GameDao(dbUrl,dbUsername,dbPassword).inGame(gameId, username);
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

  @RequestMapping(value = "/api/game/new/", method = RequestMethod.POST)
  public ResponseEntity<?> createGame(HttpServletRequest req, String start, String end) {
    if (!isAuthenticated(req)) return new ResponseEntity<>("Not logged in", HttpStatus.UNAUTHORIZED);
    start = StringUtils.trimToEmpty(start);
    end = StringUtils.trimToEmpty(end);
    if (start.isEmpty()) {
      start = RandomRequest.getRandom();
    } else {
      try {
        if (!ExistRequest.exists(start)) {
          return new ResponseEntity<>(start + " does not exist", HttpStatus.NOT_FOUND);
        }
      } catch (InvalidArticleException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
      }
    }
    if (end.isEmpty()) {
      end = RandomRequest.getRandom();
    } else {
      try {
        if (!ExistRequest.exists(end)) {
          return new ResponseEntity<>(end + " does not exist", HttpStatus.NOT_FOUND);
        }
      } catch (InvalidArticleException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
      }
    }

    Map<String, String> response = new HashMap<>();
    response.put("start", start);
    response.put("end", end);
    try {
      response.put("id", new GameDao(dbUrl,dbUsername,dbPassword).createGame(start,end));
      new GameDao(dbUrl,dbUsername,dbPassword).joinGame(response.get("id"),
          (String) req.getSession().getAttribute("username"));
    } catch (SQLException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(response, HttpStatus.OK);

  }

  @RequestMapping(value = "/api/game/{gameId}/join/", method = RequestMethod.POST)
  public ResponseEntity<String> joinGame(HttpServletRequest req, @PathVariable String gameId) {
    if (!isAuthenticated(req)) return new ResponseEntity<String>("Not logged in", HttpStatus.UNAUTHORIZED);
    try {
      return new ResponseEntity<String>(new GameDao(dbUrl,dbUsername,dbPassword).joinGame(gameId,
          (String) req.getSession().getAttribute("username")), HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/game/{gameId}/goto/{nextPage}/", method = RequestMethod.POST)
  public ResponseEntity<?> goToPage(HttpServletRequest req, @PathVariable String gameId, @PathVariable String nextPage) {
    if (!isAuthenticated(req))
      return new ResponseEntity<String>("Not logged in", HttpStatus.UNAUTHORIZED);
    String username = (String) req.getSession().getAttribute("username");
    try {
      if (!inGame(gameId, username))
        return new ResponseEntity<String>("Join game first", HttpStatus.UNAUTHORIZED);
      nextPage = StringUtils.trimToEmpty(nextPage);
      if (!ExistRequest.exists(nextPage))
        return new ResponseEntity<String>(nextPage + " does not exist", HttpStatus.NOT_FOUND);
      String currentPage = new GameDao(dbUrl, dbUsername, dbPassword).getCurrentPage(gameId, username);
      String finalPage = new GameDao(dbUrl, dbUsername, dbPassword).finalPage(gameId);
      if (currentPage.equals(finalPage)) return new ResponseEntity<String>("Game already finished", HttpStatus.BAD_REQUEST);
      if (!hasLink(currentPage, nextPage)) return new ResponseEntity<String>("No link to '" + nextPage + "' found in '" + currentPage + "'", HttpStatus.NOT_FOUND);
      Boolean finished = nextPage.equals(finalPage);
      new GameDao(dbUrl, dbUsername, dbPassword).changePage(gameId, username, nextPage, finished);
      Map<String, Object> response = new HashMap<>();
      response.put("finished", finished);
      response.put("current_page", nextPage);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InvalidArticleException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

    /*** API ENDS ***/

}
