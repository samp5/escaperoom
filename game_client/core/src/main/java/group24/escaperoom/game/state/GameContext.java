package group24.escaperoom.game.state;

import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.game.world.Grid;
import group24.escaperoom.screens.GameScreen;

public class GameContext {
  public final Grid grid;
  public final GameScreen map;
  public final Player player;
  public GameContext (GameScreen map){
    this.grid = map.getGrid();
    this.map = map;
    this.player = GameScreen.player;
  }

  public GameContext (GameScreen map, Player player){
    this.grid = map.getGrid();
    this.map = map;
    this.player = player;
  }
}
