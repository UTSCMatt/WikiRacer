package kappa.wikiracer.api.gameMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public abstract class GameModeStrategy {

  private ArrayList<SortedPlayerStrategy> rankings;

  protected GameModeStrategy() {
    rankings = new ArrayList<>();
  }

  public ArrayList<SortedPlayerStrategy> getRankings() {
    Collections.sort(rankings);
    return rankings;
  }

  protected void addInfo(SortedPlayerStrategy info) {
    rankings.add(info);
  }

  public abstract void addInfo(String player, Map<String, Integer> info);
}
