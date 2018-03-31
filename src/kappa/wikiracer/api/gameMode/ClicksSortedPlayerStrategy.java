package kappa.wikiracer.api.gameMode;

import java.util.Map;

public class ClicksSortedPlayerStrategy extends SortedPlayerStrategy {

  public ClicksSortedPlayerStrategy(String player,
      Map<String, Integer> info) {
    super(player, info);
  }

  @Override
  public int compareTo(SortedPlayerStrategy o) {
    return getInfo().get("clicks").compareTo(o.getInfo().get("clicks")) == 0 ? getInfo().get("time")
        .compareTo(o.getInfo().get("time"))
        : getInfo().get("clicks").compareTo(o.getInfo().get("clicks"));
  }
}
