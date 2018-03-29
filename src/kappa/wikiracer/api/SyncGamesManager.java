package kappa.wikiracer.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import kappa.wikiracer.config.WebSocketConfig;
import kappa.wikiracer.exception.GameException;

public class SyncGamesManager {
  
  private static final String JOINED = "joined";
  
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
    if (game.addPlayer(player)) {
      Map<String, String> payload = new HashMap<>();
      payload.put(JOINED, player);
      simpMessagingTemplate.convertAndSend(WebSocketConfig.SOCKET_DEST + gameId, payload);
      return true;
    } else {
      return false;
    }
  }
}
