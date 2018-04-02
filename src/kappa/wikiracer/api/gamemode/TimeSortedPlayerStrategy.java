package kappa.wikiracer.api.gamemode;

import java.util.Map;

public class TimeSortedPlayerStrategy extends SortedPlayerStrategy {

  public TimeSortedPlayerStrategy(String player,
      Map<String, Integer> info) {
    super(player, info);
  }

  @Override
  public int compareTo(SortedPlayerStrategy o) {
    return getInfo().get("time").compareTo(o.getInfo().get("time")) == 0 ? getInfo().get("clicks")
        .compareTo(o.getInfo().get("clicks"))
        : getInfo().get("time").compareTo(o.getInfo().get("time"));
  }
}
