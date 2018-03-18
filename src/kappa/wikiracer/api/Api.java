package kappa.wikiracer.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Array;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import kappa.wikiracer.dao.GameDao;
import kappa.wikiracer.dao.LinkDao;
import kappa.wikiracer.dao.RulesDao;
import kappa.wikiracer.dao.UserDao;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.InvalidArticleException;
import kappa.wikiracer.exception.UserNotFoundException;
import kappa.wikiracer.util.UserVerification;
import kappa.wikiracer.wiki.CategoryRequest;
import kappa.wikiracer.wiki.ExistRequest;
import kappa.wikiracer.wiki.RandomRequest;
import kappa.wikiracer.wiki.ResolveRedirectRequest;
import kappa.wikiracer.wiki.SendRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

  private final String CATEGORIES = "categories";
  private final String ARTICLES = "articles";

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
    } catch (SQLException | UnsupportedEncodingException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InvalidArticleException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  /*** Testing API Ends ***/

  private LoadingCache<String, Set<String>> linkCache;
  private LoadingCache<String, Integer> storedPagesCache;
  private LoadingCache<String, String> finalPageCache;
  private LoadingCache<String, Set<String>> bannedCategoriesCache;
  private LoadingCache<String, Set<String>> bannedArticlesCache;
  // Pair<gameId, username>
  private LoadingCache<Pair<String, String>, Boolean> inGameCache;
  private LoadingCache<String, Boolean> existsCache;
  private LoadingCache<String, String> redirectCache;

  @PostConstruct
  public void initCaches() {
    linkCache = Caffeine.newBuilder().maximumWeight(10000).weigher((String key, Set links) -> links.size()).refreshAfterWrite(5, TimeUnit.MINUTES).build(LinkRequest::sendRequest);
    storedPagesCache = Caffeine.newBuilder().maximumSize(10000).build(key -> new LinkDao(dbUrl, dbUsername, dbPassword).addPage(key));
    finalPageCache = Caffeine.newBuilder().maximumSize(1000).build(key -> new GameDao(dbUrl, dbUsername, dbPassword).finalPage(key));
    bannedCategoriesCache = Caffeine.newBuilder().maximumWeight(3000).weigher((String key, Set categories) -> categories.size()).build(key -> new RulesDao(dbUrl, dbUsername, dbPassword).getCategories(key));
    bannedArticlesCache = Caffeine.newBuilder().maximumWeight(1500).weigher((String key, Set articles) -> articles.size()).build(key -> new RulesDao(dbUrl, dbUsername, dbPassword).getArticles(key));
    inGameCache = Caffeine.newBuilder().maximumSize(5000).build(key -> new GameDao(dbUrl, dbUsername, dbPassword).inGame(key.getKey(), key.getValue()));
    existsCache = Caffeine.newBuilder().maximumSize(10000).refreshAfterWrite(1, TimeUnit.HOURS).build(
        ExistRequest::exists);
    redirectCache = Caffeine.newBuilder().maximumSize(10000).refreshAfterWrite(1, TimeUnit.HOURS).build(ResolveRedirectRequest::resolveRedirect);
  }

  private void setSession(HttpServletRequest req, HttpServletResponse res, String username) {
    req.getSession().setAttribute("username", username);
    req.getSession().setMaxInactiveInterval(60*60*24);
    Calendar expireTime = Calendar.getInstance();
    expireTime.add(Calendar.MONTH, 1);
    SimpleDateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    cookieDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    String expireString = cookieDateFormat.format(expireTime.getTime());
    res.setHeader("Set-Cookie", "JSESSIONID=" + req.getSession().getId() + "; HttpOnly; SameSite=strict; Secure; Path=/; Expires=" + expireString);
    res.addHeader("Set-Cookie", "username=" + username+ "; SameSite=strict; Secure; Path=/; Expires=" + expireString);

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
      throws SQLException, InvalidArticleException, UnsupportedEncodingException {
//    Set<String> links = new LinkDao(dbUrl, dbUsername, dbPassword).getLinks(parentTitle);
//    if (links.contains(childTitle)) {
//      return true;
//    } else {
      Set<String> updatedLinks = linkCache.get(parentTitle);
      if (updatedLinks.contains(URLDecoder.decode(childTitle, "UTF-8"))) {
        storedPagesCache.get(parentTitle);
        storedPagesCache.get(childTitle);
//        updatedLinks.removeAll(links);
//        new LinkDao(dbUrl, dbUsername, dbPassword).addLinks(parentTitle, updatedLinks);
        return true;
      } else {
        return false;
      }
//    }
  }

  private Boolean inGame(String gameId, String username) throws SQLException {
    return inGameCache.get(new Pair<>(gameId, username));
  }

  private Boolean isBanned(String gameId, String nextPage)
      throws SQLException, InvalidArticleException {
    return CategoryRequest.inCategory(nextPage, bannedCategoriesCache.get(gameId)) || bannedArticlesCache
        .get(gameId).contains(nextPage);
  }

  private String fixWikiTitles(String title) {
    title = title.replaceAll("&", "%26");
    return title.replaceAll("_", " ");
  }
  
  private ResponseEntity<String> redirectToHome() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", "/");
    return new ResponseEntity<String>(headers, HttpStatus.FOUND);
  }

  /*** API BEGINS ***/

  @RequestMapping(value = "/login/", method = RequestMethod.POST)
  public ResponseEntity<String> login(HttpServletRequest req, HttpServletResponse res, String username, String password) {
    try {
      String hash = new UserDao(dbUrl, dbUsername, dbPassword).getUserPasswordHash(username);
      if (UserVerification.checkPassword(password, hash)) {
        invalidateSession(req);
        setSession(req, res, username);
        return new ResponseEntity<String>(JSONObject.quote("User signed in"), HttpStatus.OK);
      } else {
        return new ResponseEntity<String>(JSONObject.quote("Invalid username or password"), HttpStatus.UNAUTHORIZED);
      }
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<String>(JSONObject.quote("Invalid username or password"), HttpStatus.UNAUTHORIZED);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/signup/", method = RequestMethod.POST)
  public ResponseEntity<String> signup(HttpServletRequest req, HttpServletResponse res, String username, String password) {
    username = StringUtils.trimToEmpty(username);
    password = StringUtils.trimToEmpty(password);
    if (!UserVerification.usernameIsValid(username)) {
      return new ResponseEntity<String>(JSONObject.quote("Username invalid"), HttpStatus.BAD_REQUEST);
    }
    try {
      new UserDao(dbUrl, dbUsername, dbPassword)
          .createUser(username, UserVerification.createHash(password));
      invalidateSession(req);
      setSession(req, res, username);
      return new ResponseEntity<String>(JSONObject.quote("User signed up"), HttpStatus.OK);
    } catch (SQLException ex) {
      if (ex.getMessage().equals("Username already in use")) {
        return new ResponseEntity<String>(JSONObject.quote("Username in use"), HttpStatus.CONFLICT);
      }
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/logoff/", method = RequestMethod.GET)
  public ResponseEntity<String> logoff(HttpServletRequest req, HttpServletResponse res) {
    invalidateSession(req);
    Cookie[] cookies = req.getCookies();
      if (cookies != null) {
      for (Cookie cookie : cookies) {
        cookie.setMaxAge(0);
        cookie.setValue(null);
        cookie.setPath("/");
        res.addCookie(cookie);
      }
    }
    return new ResponseEntity<>(JSONObject.quote("Logged off"), HttpStatus.OK);
  }

  @RequestMapping(value = "/api/game/new/", method = RequestMethod.POST)
  public ResponseEntity<?> createGame(HttpServletRequest req, String start, String end, String rules, String gameMode) {
    if (!isAuthenticated(req)) return new ResponseEntity<>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    start = StringUtils.trimToEmpty(start);
    end = StringUtils.trimToEmpty(end);
    JSONObject parsedRules = new JSONObject(rules);
    JSONArray bannedCategories = parsedRules.getJSONArray(CATEGORIES);
    JSONArray bannedArticles = parsedRules.getJSONArray(ARTICLES);
    start = fixWikiTitles(start);
    end = fixWikiTitles(end);
    if (start.isEmpty()) {
      start = RandomRequest.getRandom(end);
    } else {
      if (SendRequest.invalidArticle(start)) {
        return new ResponseEntity<>(JSONObject.quote("Articles has invalid characters"), HttpStatus.BAD_REQUEST);
      }

      if (!existsCache.get(start)) {
        return new ResponseEntity<>(JSONObject.quote(start + " does not exist"),
            HttpStatus.NOT_FOUND);
      }
    }
    if (end.isEmpty()) {
      end = RandomRequest.getRandom(start);
    } else {
      if (SendRequest.invalidArticle(end)) {
        return new ResponseEntity<>(JSONObject.quote("Articles has invalid characters"), HttpStatus.BAD_REQUEST);
      }

      if (!existsCache.get(end)) {
        return new ResponseEntity<>(JSONObject.quote(end + " does not exist"), HttpStatus.NOT_FOUND);
      }
    }

    start = redirectCache.get(start);
    end = redirectCache.get(end);

    Map<String, String> response = new HashMap<>();


    if (start.equals(end)) return new ResponseEntity<String>(JSONObject.quote("Cannot start and end in same article"), HttpStatus.BAD_REQUEST);

    response.put("start", start);
    response.put("end", end);
    try {
      response.put("id", new GameDao(dbUrl,dbUsername,dbPassword).createGame(start,end,gameMode));
      new GameDao(dbUrl,dbUsername,dbPassword).joinGame(response.get("id"),
          (String) req.getSession().getAttribute("username"));
      new RulesDao(dbUrl, dbUsername, dbPassword).banCategories(response.get("id"), bannedCategories);
      new RulesDao(dbUrl, dbUsername, dbPassword).banArticles(response.get("id"), bannedArticles);
    } catch (SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(response, HttpStatus.OK);

  }

  @RequestMapping(value = "/api/game/join/", method = RequestMethod.POST)
  public ResponseEntity<?> joinGame(HttpServletRequest req, String gameId) {
    if (!isAuthenticated(req)) return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    try {
      Map<String, String> response = new HashMap<>();
      response.put("start", new GameDao(dbUrl,dbUsername,dbPassword).joinGame(gameId, (String) req.getSession().getAttribute("username")));
      response.put("id", gameId);
      response.put("end", finalPageCache.get(gameId));
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/game/{gameId}/goto/", method = RequestMethod.POST)
  public ResponseEntity<?> goToPage(HttpServletRequest req, @PathVariable String gameId, String nextPage) {
    if (!isAuthenticated(req))
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    String username = (String) req.getSession().getAttribute("username");
    try {
      if (!inGame(gameId, username))
        return new ResponseEntity<String>(JSONObject.quote("Join game first"), HttpStatus.UNAUTHORIZED);
      nextPage = StringUtils.trimToEmpty(nextPage);
      nextPage = fixWikiTitles(nextPage);
      if (!existsCache.get(nextPage))
        return new ResponseEntity<String>(JSONObject.quote(nextPage + " does not exist"),
            HttpStatus.NOT_FOUND);
      String currentPage = new GameDao(dbUrl, dbUsername, dbPassword)
          .getCurrentPage(gameId, username);
      String finalPage = finalPageCache.get(gameId);
      if (currentPage.equals(finalPage))
        return new ResponseEntity<String>(JSONObject.quote("Game already finished"),
            HttpStatus.BAD_REQUEST);
      if (!hasLink(currentPage, nextPage))
        return new ResponseEntity<String>(
            JSONObject.quote("No link to '" + nextPage + "' found in '" + currentPage + "'"),
            HttpStatus.NOT_FOUND);
      String oldPage = nextPage;
      nextPage = redirectCache.get(nextPage);
      if (!nextPage.equals(oldPage)) {
        storedPagesCache.get(nextPage);
      }
      Boolean finished = nextPage.equals(finalPage);
      if (!finished && isBanned(gameId, nextPage)) {
        return new ResponseEntity<String>(JSONObject.quote("Attempt page is banned by rules"),
            HttpStatus.UNAUTHORIZED);
      }
      Map<String, Object> response = new HashMap<>(new GameDao(dbUrl, dbUsername, dbPassword)
          .changePage(gameId, username, nextPage, finished));
      response.put("finished", finished);
      response.put("current_page", nextPage);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (SQLException | UnsupportedEncodingException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InvalidArticleException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/getGameList", method = RequestMethod.GET)
  public ResponseEntity<?> getGameList(HttpServletRequest req, HttpServletResponse res, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
    List<String> response = new ArrayList<String>();
    try {
      response = new GameDao(dbUrl, dbUsername, dbPassword).getGameList(offset, limit);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @RequestMapping(value = "/api/getGameStats/{gameId}/", method = RequestMethod.GET)
  public ResponseEntity<?> getGameStats(HttpServletRequest req, HttpServletResponse res, @PathVariable String gameId) {
    List<List<String>> response = new ArrayList<>();
    try {
      response = new GameDao(dbUrl, dbUsername, dbPassword).getGameStats(gameId);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /*** API ENDS ***/

}
