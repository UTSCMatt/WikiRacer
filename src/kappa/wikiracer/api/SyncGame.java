package kappa.wikiracer.api;

import java.util.HashSet;
import java.util.Set;

public class SyncGame {
  private Set<String> players;
  private String host;
  
  public SyncGame(String host) {
    this.host = host;
    players = new HashSet<>();
    players.add(host);
  }
  
  public Boolean addPlayer(String player) {
    return players.add(player);
  }
  
  public Boolean removePlayer(String player) {
    players.remove(player);
    return host.equals(player);
  }
}
