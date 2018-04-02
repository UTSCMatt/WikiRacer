package kappa.wikiracer.api.gamemode;

import java.util.Map;

public class TimeGameModeStrategy extends GameModeStrategy {

  @Override
  public void addInfo(String player, Map<String, Integer> info) {
    addInfo(new TimeSortedPlayerStrategy(player, info));
  }
}
