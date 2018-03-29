package kappa.wikiracer.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import kappa.wikiracer.config.WebSocketConfig;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.UserNotFoundException;

public class SyncGamesManager {
  
  private static final String JOINED = "joined";
  private static final String LOBBY_CLOSED = "lobby_closed";
  
  private Map<String, SyncGame> games;
  private SimpMessagingTemplate simpMessagingTemplate;
  
  public SyncGamesManager(SimpMessagingTemplate simpMessagingTemplate) {
    games = new HashMap<>();
    this.simpMessagingTemplate = simpMessagingTemplate;
  }
  
  public void createGame(String gameId, String host) {
    games.put(gameId, new SyncGame(host));
  }
  
  public Boolean joinGame(String gameId, String player) throws GameException {
    SyncGame game = games.get(gameId);
    if (game == null) {
      throw new GameException(gameId + " not found");
    }
    if (game.getStarted()) {
      throw new GameException("Cannot join started game");
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
    Map<String, Boolean> payload = new HashMap<>();
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
}
