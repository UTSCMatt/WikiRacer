package kappa.wikiracer.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kappa.wikiracer.api.gameMode.ClicksGameModeStrategy;
import kappa.wikiracer.api.gameMode.GameModeStrategy;
import kappa.wikiracer.api.gameMode.TimeGameModeStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import kappa.wikiracer.config.WebSocketConfig;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.UserNotFoundException;

public class SyncGamesManager {
  
  private static final String JOINED = "joined";
  private static final String LEFT = "left";
  private static final String LOBBY_CLOSED = "lobby_closed";
  private static final String TIMED_OUT = "timed_out";
  private static final String PLAYER = "player";
  private static final String GAME_FINISHED = "game_finished";
  private static final int MAX_GAMES = 1000;
  
  private Map<String, SyncGame> games;
  private SimpMessagingTemplate simpMessagingTemplate;
  
  public SyncGamesManager(SimpMessagingTemplate simpMessagingTemplate) {
    games = new HashMap<>();
    this.simpMessagingTemplate = simpMessagingTemplate;
  }
  
  private Boolean trimGames() {
    Map<String, Boolean> payload = new HashMap<>();
    payload.put(TIMED_OUT, true);
    Boolean trimmed = false;
    for (String gameId : games.keySet()) {
      if (games.get(gameId).isTimedOut()) {
        trimmed = true;
        games.remove(gameId);
        simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
      }
    }
    return trimmed;
  }
  
  public void createGame(String gameId, String host, String startingArticle, String gameMode) throws GameException {
    if (games.size() > MAX_GAMES && !trimGames()) {
      throw new GameException("Too many games in progress, try again later");
    }
    GameModeStrategy mode;
    if (gameMode.equals("Clicks")) {
      mode = new ClicksGameModeStrategy();
    } else {
      mode = new TimeGameModeStrategy();
    }
    games.put(gameId, new SyncGame(host, startingArticle, mode));
  }
  
  public Boolean joinGame(String gameId, String player) throws GameException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (game.getStarted()) {
      throw new GameException("Cannot join started game");
    }
    if (game.inGame(player)) {
      throw new GameException("Already in game");
    }
    if (game.addPlayer(player)) {
      Map<String, String> payload = new HashMap<>();
      payload.put(JOINED, player);
      simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
      return true;
    } else {
      return false;
    }
  } 
  
  public Boolean leaveGame(String gameId, String player) throws GameException, UserNotFoundException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (game.getStarted()) {
      throw new GameException("Cannot leave started game");
    }
    Boolean lobbyClosed = false;
    if (game.removePlayer(player)) {
      games.remove(gameId);
      lobbyClosed = true;
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put(LEFT, player);
    payload.put(LOBBY_CLOSED, lobbyClosed);
    simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
    return lobbyClosed;
  }
  
  public void startGame(String gameId, String player) throws GameException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (!game.getHost().equals(player)) {
      throw new GameException("Only host can begin game");
    }
    game.startGame();
  }

  public void goToPage(String gameId, String player, Map<String, Object> info)
      throws GameException, UserNotFoundException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (!game.getStarted()) {
      throw new GameException(gameId + " has not started yet");
    }
    game.goToPage(player, info);
    Map<String, Object> payload = new HashMap<>(info);
    payload.remove("current_page");
    payload.put(PLAYER, player);
    simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
    if (game.isFinished()) {
      payload = new HashMap<>(game.getEndInfo());
      payload.put(GAME_FINISHED, true);
      simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
      games.remove(gameId);
    }
  }

  public List<String> getPlayers(String username, String gameId)
      throws GameException, UserNotFoundException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (!game.hasPlayer(username)) {
      throw new UserNotFoundException("Cannot check players of a game user is not in");
    }
    return game.getPlayers();
  }
}
