package kappa.wikiracer.api;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import kappa.wikiracer.dao.GameDao;
import kappa.wikiracer.dao.LinkDao;
import kappa.wikiracer.dao.RulesDao;
import kappa.wikiracer.dao.StatsDao;
import kappa.wikiracer.dao.UserDao;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.InvalidArticleException;
import kappa.wikiracer.exception.InvalidFileTypeException;
import kappa.wikiracer.exception.UserNotFoundException;
import kappa.wikiracer.util.UserVerification;
import kappa.wikiracer.util.amazon.S3Client;
import kappa.wikiracer.wiki.CategoryRequest;
import kappa.wikiracer.wiki.ExistRequest;
import kappa.wikiracer.wiki.LinkRequest;
import kappa.wikiracer.wiki.RandomRequest;
import kappa.wikiracer.wiki.ResolveRedirectRequest;
import kappa.wikiracer.wiki.SendRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class Api {

  public static final String GAME_CODE_KEY = "gameCode";
  public static final String START_PAGE_KEY = "startPage";
  public static final String END_PAGE_KEY = "endPage";
  public static final String GAME_MODE_KEY = "gameMode";
  public static final String USERNAME_KEY = "username";
  public static final String TIME_SPEND_KEY = "timeSpend";
  public static final String NUM_CLICKS_KEY = "numClicks";
  private static final String CATEGORIES = "categories";
  private static final String ARTICLES = "articles";
  private final S3Client s3Client;
  private final SimpMessagingTemplate simpMessagingTemplate;
  @Value("${spring.datasource.username}")
  private String dbUsername;
  @Value("${spring.datasource.url}")
  private String dbUrl;
  @Value("${spring.datasource.password}")
  private String dbPassword;
  private LoadingCache<String, Set<String>> linkCache;
  private LoadingCache<String, Integer> storedPagesCache;
  private LoadingCache<String, String> finalPageCache;
  private LoadingCache<String, Set<String>> bannedCategoriesCache;
  private LoadingCache<String, Set<String>> bannedArticlesCache;
  // Pair<gameId, username>
  private LoadingCache<Pair<String, String>, Boolean> inGameCache;
  private LoadingCache<Pair<String, String>, String> pathCache;
  private LoadingCache<String, Boolean> existsCache;
  private LoadingCache<String, String> redirectCache;
  private LoadingCache<String, Boolean> isSyncCache;
  private LoadingCache<String, Boolean> userExistsCache;
  private LoadingCache<String, String> profilePictureUrlCache;
  private LoadingCache<String, byte[]> pictureCache;
  private LoadingCache<Integer, List<String>> topPagesCache;
  private SyncGamesManager syncGamesManager;

  @Autowired
  public Api(SimpMessagingTemplate simpMessagingTemplate, S3Client s3Client) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.s3Client = s3Client;
  }


  /**
   * Initialize all the caches.
   */
  @PostConstruct
  public void initCaches() {
    linkCache = Caffeine.newBuilder().maximumWeight(10000)
        .weigher((String key, Set links) -> links.size()).refreshAfterWrite(5, TimeUnit.MINUTES)
        .build(LinkRequest::sendRequest);
    storedPagesCache = Caffeine.newBuilder().maximumSize(10000)
        .build(key -> new LinkDao(dbUrl, dbUsername, dbPassword).addPage(key));
    finalPageCache = Caffeine.newBuilder().maximumSize(1000)
        .build(key -> new GameDao(dbUrl, dbUsername, dbPassword).finalPage(key));
    bannedCategoriesCache = Caffeine.newBuilder().maximumWeight(3000)
        .weigher((String key, Set categories) -> categories.size())
        .build(key -> new RulesDao(dbUrl, dbUsername, dbPassword).getCategories(key));
    bannedArticlesCache = Caffeine.newBuilder().maximumWeight(1500)
        .weigher((String key, Set articles) -> articles.size())
        .build(key -> new RulesDao(dbUrl, dbUsername, dbPassword).getArticles(key));
    inGameCache = Caffeine.newBuilder().maximumSize(5000).build(
        key -> new GameDao(dbUrl, dbUsername, dbPassword).inGame(key.getKey(), key.getValue()));
    existsCache = Caffeine.newBuilder().maximumSize(10000).refreshAfterWrite(1, TimeUnit.HOURS)
        .build(
            ExistRequest::exists);
    redirectCache = Caffeine.newBuilder().maximumSize(10000).refreshAfterWrite(1, TimeUnit.HOURS)
        .build(ResolveRedirectRequest::resolveRedirect);
    isSyncCache = Caffeine.newBuilder().maximumSize(1000)
        .build(key -> new GameDao(dbUrl, dbUsername, dbPassword).isSync(key));
    userExistsCache = Caffeine.newBuilder().maximumSize(1000)
        .build(key -> new UserDao(dbUrl, dbUsername, dbPassword).userExists(key));
    profilePictureUrlCache = Caffeine.newBuilder().maximumSize(5000)
        .build(key -> new UserDao(dbUrl, dbUsername, dbPassword).getImage(key));
    pictureCache = Caffeine.newBuilder().maximumWeight(10000000)
        .weigher((String key, byte[] file) -> file.length).build(
            s3Client::getImage);
    topPagesCache = Caffeine.newBuilder().maximumWeight(100)
        .weigher((Integer key, List<String> pages) -> pages.size())
        .build(key -> new StatsDao(dbUrl, dbUsername, dbPassword).topPages(key));
    pathCache = Caffeine.newBuilder().maximumWeight(10000)
        .weigher((Pair<String, String> key, String path) -> path.length()).build(
            key -> new StatsDao(dbUrl, dbUsername, dbPassword)
                .userGamePath(key.getKey(), key.getValue()));
  }

  @PostConstruct
  public void initManagers() {
    syncGamesManager = new SyncGamesManager(simpMessagingTemplate);
  }

  private void setSession(HttpServletRequest req, HttpServletResponse res, String username) {
    req.getSession().setAttribute("username", username);
    req.getSession().setMaxInactiveInterval(60 * 60 * 24);
    Calendar expireTime = Calendar.getInstance();
    expireTime.add(Calendar.MONTH, 1);
    SimpleDateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
        Locale.US);
    cookieDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    String expireString = cookieDateFormat.format(expireTime.getTime());
    res.setHeader("Set-Cookie", "JSESSIONID=" + req.getSession().getId()
        + "; HttpOnly; SameSite=strict; Secure; Path=/; Expires=" + expireString);
    res.addHeader("Set-Cookie",
        "username=" + username + "; SameSite=strict; Secure; Path=/; Expires=" + expireString);

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
    // Set<String> links = new LinkDao(dbUrl, dbUsername, dbPassword).getLinks(parentTitle);
    // if (links.contains(childTitle)) {
    // return true;
    // } else {
    Set<String> updatedLinks = linkCache.get(parentTitle);
    if (updatedLinks.contains(URLDecoder.decode(childTitle, "UTF-8"))) {
      storedPagesCache.get(parentTitle);
      storedPagesCache.get(childTitle);
      // updatedLinks.removeAll(links);
      // new LinkDao(dbUrl, dbUsername, dbPassword).addLinks(parentTitle, updatedLinks);
      return true;
    } else {
      return false;
    }
    // }
  }

  private Boolean inGame(String gameId, String username) throws SQLException {
    return inGameCache.get(new Pair<>(gameId, username));
  }

  private Boolean isBanned(String gameId, String nextPage)
      throws SQLException, InvalidArticleException {
    return CategoryRequest.inCategory(nextPage, bannedCategoriesCache.get(gameId))
        || bannedArticlesCache
        .get(gameId).contains(nextPage);
  }

  private String fixWikiTitles(String title) {
    title = title = title.replaceAll("&", "%26");
    title = title = title.replaceAll("\\+", "%2B");
    return title.replaceAll("_", " ");
  }

  private ResponseEntity<String> redirectToHome() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", "/");
    return new ResponseEntity<String>(headers, HttpStatus.FOUND);
  }

  /* API BEGINS */

  /**
   * API to login to the application.
   *
   * @param username the username which is case sensitive
   * @param password the password of the user
   * @return response from the server and adds session cookies
   */
  @RequestMapping(value = "/login/", method = RequestMethod.POST)
  public ResponseEntity<String> login(HttpServletRequest req, HttpServletResponse res,
      String username, String password) {
    try {
      String hash = new UserDao(dbUrl, dbUsername, dbPassword).getUserPasswordHash(username);
      if (UserVerification.checkPassword(password, hash)) {
        invalidateSession(req);
        setSession(req, res, username);
        return new ResponseEntity<String>(JSONObject.quote("User signed in"), HttpStatus.OK);
      } else {
        return new ResponseEntity<String>(JSONObject.quote("Invalid username or password"),
            HttpStatus.UNAUTHORIZED);
      }
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<String>(JSONObject.quote("Invalid username or password"),
          HttpStatus.UNAUTHORIZED);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Signup to the application.
   *
   * @param username the username which is case sensitive and must not have been used
   * @param password the password of the user
   * @return response from the server and adds logs in for them
   */
  @RequestMapping(value = "/signup/", method = RequestMethod.POST)
  public ResponseEntity<String> signup(HttpServletRequest req, HttpServletResponse res,
      String username, String password) {
    username = StringUtils.trimToEmpty(username);
    password = StringUtils.trimToEmpty(password);
    if (!UserVerification.usernameIsValid(username)) {
      return new ResponseEntity<String>(JSONObject.quote("Username invalid"),
          HttpStatus.BAD_REQUEST);
    }
    try {
      new UserDao(dbUrl, dbUsername, dbPassword)
          .createUser(username, UserVerification.createHash(password));
      invalidateSession(req);
      setSession(req, res, username);
      userExistsCache.invalidate(username);
      return new ResponseEntity<String>(JSONObject.quote("User signed up"), HttpStatus.OK);
    } catch (SQLException ex) {
      if (ex.getMessage().equals("Username already in use")) {
        return new ResponseEntity<String>(JSONObject.quote("Username in use"), HttpStatus.CONFLICT);
      }
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Logoff from the application.
   *
   * @return successful logoff and deletes cookies
   */
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

  @RequestMapping(value = "/api/profile/image/", method = RequestMethod.POST)
  public ResponseEntity<?> uploadProfileImage(HttpServletRequest req, MultipartFile file) {
    if (file == null) {
      return new ResponseEntity<>(JSONObject.quote("File not provided"), HttpStatus.BAD_REQUEST);
    }
    if (!isAuthenticated(req)) {
      return new ResponseEntity<>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String username = (String) req.getSession().getAttribute("username");
    try {
      String oldUrl = profilePictureUrlCache.get(username);
      String fileName = s3Client.uploadImage(file);
      new UserDao(dbUrl, dbUsername, dbPassword).changeImage(username, fileName);
      if (!oldUrl.isEmpty()) {
        s3Client.deleteImage(oldUrl);
        pictureCache.invalidate(oldUrl);
      }
      profilePictureUrlCache.invalidate(username);
      return new ResponseEntity<>(JSONObject.quote("Success"), HttpStatus.OK);
    } catch (IOException | SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InvalidFileTypeException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/profile/{user}/image/", method = RequestMethod.GET)
  public ResponseEntity<?> getProfileImage(@PathVariable String user) {
    try {
      HttpHeaders res = new HttpHeaders();
      String url = profilePictureUrlCache.get(user);
      if (url.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.OK);
      }
      res.add("content-type", "image/jpeg");
      return new ResponseEntity<>(pictureCache.get(url), res, HttpStatus.OK);
    } catch (AmazonS3Exception ex) {
      return new ResponseEntity<>(JSONObject.quote("Image not found"), HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "/api/profile/image/", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteProfileImage(HttpServletRequest req) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String username = (String) req.getSession().getAttribute("username");
    try {
      String url = profilePictureUrlCache.get(username);
      if (url.isEmpty()) {
        return new ResponseEntity<>(JSONObject.quote("Image not found"), HttpStatus.NOT_FOUND);
      }
      s3Client.deleteImage(url);
      new UserDao(dbUrl, dbUsername, dbPassword).deleteImage(username);
      profilePictureUrlCache.invalidate(username);
      pictureCache.invalidate(url);
      return new ResponseEntity<>(JSONObject.quote("Success"), HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (AmazonS3Exception ex) {
      return new ResponseEntity<>(JSONObject.quote("Image not found"), HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Create a new game.
   *
   * @param start the starting article, random if undefined
   * @param end the ending article random if undefined
   * @param rules JSON string containing rules for the game
   * @param gameMode the type of game to create
   * @return response of creating the game
   */
  @RequestMapping(value = "/api/game/new/", method = RequestMethod.POST)
  public ResponseEntity<?> createGame(HttpServletRequest req, String start, String end,
      String rules, String gameMode, Boolean isSync) {
    Boolean incrementStart = false;
    Boolean incrementEnd = false;
    if (!isAuthenticated(req)) {
      return new ResponseEntity<>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    start = StringUtils.trimToEmpty(start);
    end = StringUtils.trimToEmpty(end);
    JSONObject parsedRules = new JSONObject(rules);
    JSONArray bannedCategories = parsedRules.getJSONArray(CATEGORIES);
    JSONArray bannedArticles = parsedRules.getJSONArray(ARTICLES);

    Set<String> bannedCategoriesSet = new HashSet<>();
    for (int i = 0; i < bannedCategories.length(); i++) {
      String ban = StringUtils.trimToEmpty(bannedCategories.getString(i));
      if (!ban.isEmpty()) {
        bannedCategoriesSet.add(bannedCategories.getString(i));
      }
    }

    Set<String> bannedArticlesSet = new HashSet<>();
    for (int i = 0; i < bannedArticles.length(); i++) {
      String ban = StringUtils.trimToEmpty(bannedArticles.getString(i));
      if (!ban.isEmpty()) {
        bannedArticlesSet.add(bannedArticles.getString(i));
      }
    }

    start = fixWikiTitles(start);
    end = fixWikiTitles(end);
    if (start.isEmpty()) {
      start = RandomRequest.getRandom(end);
    } else {
      if (SendRequest.invalidArticle(start)) {
        return new ResponseEntity<>(JSONObject.quote("Articles has invalid characters"),
            HttpStatus.BAD_REQUEST);
      }

      if (!existsCache.get(start)) {
        return new ResponseEntity<>(JSONObject.quote(start + " does not exist"),
            HttpStatus.NOT_FOUND);
      }
      incrementStart = true;
    }
    if (end.isEmpty()) {
      end = RandomRequest.getRandom(start);
    } else {
      if (SendRequest.invalidArticle(end)) {
        return new ResponseEntity<>(JSONObject.quote("Articles has invalid characters"),
            HttpStatus.BAD_REQUEST);
      }

      if (!existsCache.get(end)) {
        return new ResponseEntity<>(JSONObject.quote(end + " does not exist"),
            HttpStatus.NOT_FOUND);
      }
      incrementEnd = true;
    }

    start = redirectCache.get(start);
    end = redirectCache.get(end);

    Map<String, Object> response = new HashMap<>();

    if (start.equals(end)) {
      return new ResponseEntity<String>(JSONObject.quote("Cannot start and end in same article"),
          HttpStatus.BAD_REQUEST);
    }

    response.put("start", start);
    response.put("end", end);
    try {
      response
          .put("id",
              new GameDao(dbUrl, dbUsername, dbPassword).createGame(start, end, gameMode, isSync));
      String gameId = (String) response.get("id");
      new GameDao(dbUrl, dbUsername, dbPassword).joinGame(gameId,
          (String) req.getSession().getAttribute("username"));
      if (isSync) {
        syncGamesManager
            .createGame(gameId, (String) req.getSession().getAttribute("username"), start,
                gameMode);
      }
      new RulesDao(dbUrl, dbUsername, dbPassword)
          .banCategories(gameId, bannedCategoriesSet);
      new RulesDao(dbUrl, dbUsername, dbPassword).banArticles(gameId, bannedArticlesSet);
      if (incrementStart) {
        new StatsDao(dbUrl, dbUsername, dbPassword).incrementWikiPageUse(start);
        topPagesCache.invalidateAll();
      }
      if (incrementEnd) {
        new StatsDao(dbUrl, dbUsername, dbPassword).incrementWikiPageUse(end);
        topPagesCache.invalidateAll();
      }
      new StatsDao(dbUrl, dbUsername, dbPassword)
          .addToPath(gameId, (String) req.getSession().getAttribute("username"), start);
    } catch (SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    response.put("isSync", isSync);
    return new ResponseEntity<>(response, HttpStatus.OK);

  }

  /**
   * Join a given game.
   *
   * @param gameId the game's id
   * @return response of trying to join the game
   */
  @RequestMapping(value = "/api/game/join/", method = RequestMethod.POST)
  public ResponseEntity<?> joinGame(HttpServletRequest req, String gameId) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    try {
      final Boolean isSync = isSyncCache.get(gameId);
      if (isSync) {
        syncGamesManager.joinGame(gameId, (String) req.getSession().getAttribute("username"));
      }
      Map<String, Object> response = new HashMap<>();
      response.put("start", new GameDao(dbUrl, dbUsername, dbPassword)
          .joinGame(gameId, (String) req.getSession().getAttribute("username")));
      response.put("id", gameId);
      response.put("end", finalPageCache.get(gameId));
      response.put("isSync", isSync);
      new StatsDao(dbUrl, dbUsername, dbPassword)
          .addToPath(gameId, (String) req.getSession().getAttribute("username"),
              (String) response.get("start"));
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/game/{gameId}/leave/", method = RequestMethod.GET)
  public ResponseEntity<?> leaveGame(HttpServletRequest req, @PathVariable String gameId) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    try {
      syncGamesManager.leaveGame(gameId, (String) req.getSession().getAttribute("username"));
      new GameDao(dbUrl, dbUsername, dbPassword)
          .leaveGame(gameId, (String) req.getSession().getAttribute("username"));
      return new ResponseEntity<String>(JSONObject.quote("success"), HttpStatus.OK);
    } catch (GameException | UserNotFoundException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Go to a new page for a given game.
   *
   * @param gameId the id of the game
   * @param nextPage the page to go to
   * @return response of trying to go to the page for the given game
   */
  @RequestMapping(value = "/api/game/{gameId}/goto/", method = RequestMethod.POST)
  public ResponseEntity<?> goToPage(HttpServletRequest req, @PathVariable String gameId,
      String nextPage) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String username = (String) req.getSession().getAttribute("username");
    try {
      if (!inGame(gameId, username)) {
        return new ResponseEntity<String>(JSONObject.quote("Join game first"),
            HttpStatus.UNAUTHORIZED);
      }
      nextPage = StringUtils.trimToEmpty(nextPage);
      nextPage = fixWikiTitles(nextPage);
      if (!existsCache.get(nextPage)) {
        return new ResponseEntity<String>(JSONObject.quote(nextPage + " does not exist"),
            HttpStatus.NOT_FOUND);
      }
      String currentPage = new GameDao(dbUrl, dbUsername, dbPassword)
          .getCurrentPage(gameId, username);
      String finalPage = finalPageCache.get(gameId);
      if (currentPage.equals(finalPage)) {
        return new ResponseEntity<String>(JSONObject.quote("Game already finished"),
            HttpStatus.BAD_REQUEST);
      }
      String oldPage = nextPage;
      nextPage = redirectCache.get(nextPage);
      if (!hasLink(currentPage, oldPage)) {
        return new ResponseEntity<String>(
            JSONObject.quote("No link to '" + nextPage + "' found in '" + currentPage + "'"),
            HttpStatus.NOT_FOUND);
      }
      if (!nextPage.equals(oldPage)) {
        storedPagesCache.get(nextPage);
      }
      Boolean finished = nextPage.equals(finalPage);
      if (!finished && isBanned(gameId, nextPage)) {
        return new ResponseEntity<String>(JSONObject.quote("Attempt page is banned by rules"),
            HttpStatus.UNAUTHORIZED);
      }
      nextPage = fixWikiTitles(nextPage);
      Map<String, Object> response = new HashMap<>(new GameDao(dbUrl, dbUsername, dbPassword)
          .changePage(gameId, username, nextPage, finished));
      response.put("finished", finished);
      response.put("current_page", nextPage);
      Boolean isSync = isSyncCache.get(gameId);
      response.put("isSync", isSync);
      if (isSync) {
        syncGamesManager.goToPage(gameId, username, response);
      }
      new StatsDao(dbUrl, dbUsername, dbPassword).addToPath(gameId, username, nextPage);
      pathCache.invalidate(new Pair<>(gameId, username));
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (SQLException | UnsupportedEncodingException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (InvalidArticleException | GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
  }

  @RequestMapping(value = "/api/game/realtime/{gameId}/players/", method = RequestMethod.GET)
  public ResponseEntity<?> gameLobbyPlayers(HttpServletRequest req, @PathVariable String gameId) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String username = (String) req.getSession().getAttribute("username");
    try {
      return new ResponseEntity<>(syncGamesManager.getPlayers(username, gameId), HttpStatus.OK);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
  }

  @RequestMapping(value = "/api/game/realtime/{gameId}/start/", method = RequestMethod.PATCH)
  public ResponseEntity<?> startGame(HttpServletRequest req, @PathVariable String gameId) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String username = (String) req.getSession().getAttribute("username");
    try {
      syncGamesManager.startGame(gameId, username);
      new GameDao(dbUrl, dbUsername, dbPassword).startSyncGame(gameId);
      return new ResponseEntity<>(JSONObject.quote("success"), HttpStatus.OK);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (SQLException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get a list of games.
   *
   * @param search optional parameter to search for games that begin with start
   * @param offset how many games to skip, default 0
   * @param limit how many games to return, default 10, max 50
   * @return response a list of games
   */
  @RequestMapping(value = "/api/gameList", method = RequestMethod.GET)
  public ResponseEntity<?> getGameList(HttpServletRequest req, HttpServletResponse res,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "10") int limit) {
    limit = Math.min(limit, 50);
    search = StringUtils.trimToEmpty(search);
    List<Map<String, Object>> response = new ArrayList<>();
    try {
      response = new GameDao(dbUrl, dbUsername, dbPassword).getGameList(search, offset, limit);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Get the stats of a given game.
   *
   * @param gameId the game's id
   * @return the stats of the game
   */
  @RequestMapping(value = "/api/game/{gameId}/stats/", method = RequestMethod.GET)
  public ResponseEntity<?> getGameStats(HttpServletRequest req, HttpServletResponse res,
      @PathVariable String gameId) {
    List<Map> response = new ArrayList<>();
    try {
      response = new GameDao(dbUrl, dbUsername, dbPassword).getGameStats(gameId);
    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (GameException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  /**
   * Get a list of games a user have played.
   *
   * @param username name of the user to display
   * @param showNonFinished display non-finished games, default false
   * @param offset how many games to skip, default 0
   * @param limit how many games to return, default 10, max 50
   * @return response a list of games
   */
  @RequestMapping(value = "/api/user/{username}/game", method = RequestMethod.GET)
  public ResponseEntity<?> userGames(HttpServletRequest req, HttpServletResponse res,
      @PathVariable String username,
      @RequestParam(value = "showNonFinished", defaultValue = "false") Boolean showNonFinished,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "10") int limit) {
    Map<String, Object> payload = new HashMap<>();
    List<String> response = new ArrayList<String>();
    try {
      if (!userExistsCache.get(username)) {
        return new ResponseEntity<String>(JSONObject.quote("No such user"), HttpStatus.NOT_FOUND);
      }
      boolean usernameMatch = false;
      if (isAuthenticated(req)) {
        String sessionUsername = (String) req.getSession().getAttribute("username");
        usernameMatch = sessionUsername.equals(username);
      }
      response = new StatsDao(dbUrl, dbUsername, dbPassword)
          .userGames(username, showNonFinished, offset, limit);
      payload.put("games", response);
      payload.put("match", usernameMatch);

    } catch (SQLException ex) {
      return new ResponseEntity<String>(JSONObject.quote(ex.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(payload, HttpStatus.OK);
  }

  /**
   * Get a list of pages a player visited to finish a game.
   *
   * @param gameId name of the game to display
   * @param username name of the user to display
   * @return response a list of wiki page from start to finish
   */
  @RequestMapping(value = "/api/game/{gameId}/player/{username}/path/", method = RequestMethod.GET)
  public ResponseEntity<?> userGamePath(HttpServletRequest req, HttpServletResponse res,
      @PathVariable String gameId,
      @PathVariable String username) {
    if (!userExistsCache.get(username)) {
      return new ResponseEntity<String>(JSONObject.quote("No such user"), HttpStatus.NOT_FOUND);
    }
    String response = pathCache.get(new Pair<>(gameId, username));
    return new ResponseEntity<>(JSONObject.quote(response), HttpStatus.OK);
  }

  /**
   * Get a list of pages that are most used as start/end pages.
   *
   * @param limit how many pages to return, default 10, max 50
   * @return response a list of games
   */
  @RequestMapping(value = "/api/article/mostused", method = RequestMethod.GET)
  public ResponseEntity<?> topPages(HttpServletRequest req, HttpServletResponse res,
      @RequestParam(value = "limit", defaultValue = "10") int limit) {
    List<String> response = new ArrayList<String>();
    response = topPagesCache.get(limit);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Send the message given the game id, player name, and message content.
   * Only send if the player is in the given game.
   *
   * @param gameId name of the game to send message to
   * @param messageContent content of the message to send
   * @return response success if message send, not found otherwise
   */
  @RequestMapping(value = "/api/game/realtime/{gameId}/message/", method = RequestMethod.POST)
  public ResponseEntity<?> sendMessage(HttpServletRequest req, HttpServletResponse res,
      @PathVariable String gameId, String messageContent) {
    if (!isAuthenticated(req)) {
      return new ResponseEntity<String>(JSONObject.quote("Not logged in"), HttpStatus.UNAUTHORIZED);
    }
    String player = (String) req.getSession().getAttribute("username");
    try {
      PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
      messageContent = policy.sanitize(messageContent);
      syncGamesManager.sendMessage(gameId, player, messageContent);
    } catch (GameException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()), HttpStatus.NOT_FOUND);
    } catch (UserNotFoundException ex) {
      return new ResponseEntity<>(JSONObject.quote(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
    return new ResponseEntity<>(JSONObject.quote("success"), HttpStatus.OK);
  }
  /* API ENDS */

}
