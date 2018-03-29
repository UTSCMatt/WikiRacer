package kappa.wikiracer.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.UserNotFoundException;

public class SyncGame {
  private Set<String> players;
  private String host;
  private Boolean started;
  private Map<String, Boolean> finished;
  
  public SyncGame(String host) {
    this.host = host;
    started = false;
    players = new HashSet<>();
    players.add(host);
    finished = new HashMap<>();
    finished.put(host, false);
  }
  
  public Boolean addPlayer(String player) {
    finished.put(player, false);
    return players.add(player);
  }
  
  public Boolean removePlayer(String player) throws UserNotFoundException {
    if (players.remove(player)) {
      finished.remove(player);
      return host.equals(player);
    }
    throw new UserNotFoundException(player + " not in game");
  }

  public Boolean getStarted() {
    return started;
  }

  public void setStarted(Boolean started) {
    if (!this.started) {
      this.started = started;
    }
  }
  
  public void startGame() throws GameException {
    if (started) {
      throw new GameException("game already started");
    }
    started = true;
  }

  public String getHost() {
    return host;
  }
  
  public Boolean isFinished() {
    return !finished.containsValue(false);
  }
  
}
