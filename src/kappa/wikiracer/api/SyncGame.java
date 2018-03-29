package kappa.wikiracer.api;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.UserNotFoundException;

public class SyncGame {
  private Set<String> players;
  private String host;
  private Boolean started;
  private Map<String, Boolean> finished;
  private Date creationTime;
  
  // Minutes
  private static final int TIMEOUT = 60;
  private static final int MAX_PLAYERS = 4;
  
  public SyncGame(String host) {
    this.host = host;
    started = false;
    players = new HashSet<>();
    players.add(host);
    finished = new HashMap<>();
    finished.put(host, false);
    creationTime = Calendar.getInstance().getTime();
  }
  
  public Boolean addPlayer(String player) throws GameException {
    if (players.size() > MAX_PLAYERS) {
      throw new GameException("Lobby full");
    }
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
  
  public Boolean isTimedOut() {
    return TimeUnit.MINUTES.convert(Calendar.getInstance().getTime().getTime() - creationTime.getTime(), TimeUnit.MILLISECONDS) >= TIMEOUT;
  }
  
  public Boolean inGame(String player) {
    return players.contains(player);
  }
  
}
