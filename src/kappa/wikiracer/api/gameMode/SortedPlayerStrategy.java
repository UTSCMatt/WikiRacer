package kappa.wikiracer.api.gameMode;

import java.util.Collections;
import java.util.Map;

public abstract class SortedPlayerStrategy implements Comparable<SortedPlayerStrategy> {

  private String player;

  private Map<String, Integer> info;

  SortedPlayerStrategy(String player, Map<String, Integer> info) {
    this.player = player;
    this.info = info;
  }

  @Override
  public String toString() {
    return player;
  }

  @Override
  abstract public int compareTo(SortedPlayerStrategy o);

  public String getPlayer() {
    return player;
  }

  protected Map<String, Integer> getInfo() {
    return Collections.unmodifiableMap(info);
  }
}
