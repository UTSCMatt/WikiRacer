package kappa.wikiracer.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kappa.wikiracer.api.gamemode.GameModeStrategy;
import kappa.wikiracer.exception.GameException;
import kappa.wikiracer.exception.UserNotFoundException;

public class SyncGame {

  // Minutes
  private static final int TIMEOUT = 60;
  private static final int MAX_PLAYERS = 4;
  private Set<String> players;
  private String host;
  private String start;
  private Boolean started;
  private Map<String, Boolean> finished;
  private Map<String, StringBuilder> path;
  private Map<String, Integer> time;
  private Map<String, Integer> clicks;
  private Date creationTime;
  private GameModeStrategy gameMode;

  public SyncGame(String host, String startingArticle, GameModeStrategy gameMode) {
    this.host = host;
    started = false;
    players = new HashSet<>();
    players.add(host);
    finished = new HashMap<>();
    finished.put(host, false);
    creationTime = Calendar.getInstance().getTime();
    path = new HashMap<>();
    path.put(host, new StringBuilder(startingArticle));
    time = new HashMap<>();
    time.put(host, 0);
    clicks = new HashMap<>();
    clicks.put(host, 0);
    this.start = startingArticle;
    this.gameMode = gameMode;
  }

  public Boolean addPlayer(String player) throws GameException {
    if (players.size() > MAX_PLAYERS) {
      throw new GameException("Lobby full");
    }
    finished.put(player, false);
    path.put(player, new StringBuilder(start));
    time.put(player, 0);
    clicks.put(player, 0);
    return players.add(player);
  }

  public Boolean removePlayer(String player) throws UserNotFoundException {
    if (players.remove(player)) {
      finished.remove(player);
      path.remove(player);
      time.remove(player);
      clicks.remove(player);
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
    return TimeUnit.MINUTES
        .convert(Calendar.getInstance().getTime().getTime() - creationTime.getTime(),
            TimeUnit.MILLISECONDS) >= TIMEOUT;
  }

  public Boolean inGame(String player) {
    return players.contains(player);
  }

  public void goToPage(String player, Map<String, Object> info) throws UserNotFoundException {
    if (!players.contains(player)) {
      throw new UserNotFoundException(player + " not in game");
    }
    finished.put(player, (Boolean) info.get("finished"));
    path.get(player).append(" -> ").append(info.get("current_page"));
    time.put(player, (Integer) info.get("time"));
    clicks.put(player, (Integer) info.get("clicks"));
  }

  public Map<String, Object> getEndInfo() {
    Map<String, Object> playersInfo = new HashMap<>();
    for (String player : players) {
      Map<String, Object> playerInfo = new HashMap<>();
      playerInfo.put("path", path.get(player).toString());
      playerInfo.put("clicks", clicks.get(player));
      playerInfo.put("time", time.get(player));
      playersInfo.put(player, playerInfo);
      Map<String, Integer> info = new HashMap<>();
      info.put("clicks", clicks.get(player));
      info.put("time", time.get(player));
      gameMode.addInfo(player, info);
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("player_info", playersInfo);
    payload.put("rankings", gameMode.getRankings());
    return payload;
  }

  public Boolean hasPlayer(String player) {
    return players.contains(player);
  }

  public List<String> getPlayers() {
    return new ArrayList<>(players);
  }

}
