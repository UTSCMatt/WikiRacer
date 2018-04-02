package kappa.wikiracer.api.gamemode;

import java.util.Map;

public class ClicksGameModeStrategy extends GameModeStrategy {

  @Override
  public void addInfo(String player, Map<String, Integer> info) {
    addInfo(new ClicksSortedPlayerStrategy(player, info));
  }
}
