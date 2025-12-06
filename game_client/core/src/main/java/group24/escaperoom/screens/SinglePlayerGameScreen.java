package group24.escaperoom.screens;

import java.util.Optional;
import java.util.function.Predicate;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import group24.escaperoom.entities.player.PlayerDetails;
import group24.escaperoom.ScreenManager;
import group24.escaperoom.data.Grid;
import group24.escaperoom.data.MapData;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.objects.ObjectLoader.LoadedObjects;
import group24.escaperoom.entities.properties.PlayerProperty;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.ui.ScreenShotter;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.data.MapLoader;

public class SinglePlayerGameScreen extends GameScreen {

  G24TextButton editButton = new G24TextButton("Back to Editor");

  public SinglePlayerGameScreen(MapData data, boolean verifying){
    super(data);
    applyGrid(data.getGrid());
    this.gameType = verifying? GameType.Verifying : GameType.Standard;

    if (verifying){
      actionlog.setVisible(false);
      render(0);
      ScreenShotter.takeScreenShot(data.getMetadata());
      actionlog.setVisible(true);
    }
  }

  public SinglePlayerGameScreen(MapData mapdata) {
    super(mapdata);
    applyGrid(mapdata.getGrid());
    this.gameType = GameType.Editor;
  }

  private void applyGrid(Grid grid){
    loadGrid(grid);
    loadPlayer();
  }

  public void loadPlayer() {
    Optional<Item> playerStart = grid.findItemWhere(new Predicate<Item>() {
      @Override
      public boolean test(Item arg0) {
        return arg0.hasProperty(PropertyType.Player);
      }
    });

    Item playerItem = playerStart
                        .orElse(
                          new Item(LoadedObjects.getItem("GameControl", "Mr. E").get()
                        ));

    player.setPosition(playerItem.getX(), playerItem.getY());
    player.setSize(playerItem.getWidth(), playerItem.getHeight());
    playerItem.remove();

    PlayerDetails details = playerItem.getProperty(PropertyType.Player, PlayerProperty.class).get().getDetails();
    player.setTexture(playerItem.getTexture());
    player.renderPriority = playerItem.renderPriority();
    playerId = playerItem.getID();

    followActor = Optional.of(player);
    player.setDetails(details);
  }

  @Override
  protected void init() {
    editButton.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        if (editButton.isChecked()) {
          MapLoader.tryLoadMap(metadata).ifPresent((g) -> {
            ScreenManager.instance().showScreen(new LevelEditorScreen(g));
          });
        }
      }
    });

    if (gameType == GameType.Editor) {
      rootTable.add(editButton).right().bottom().expandX();
    }
  }

  @Override
  public void render(float delta) {
    super.render(delta);
    player.stepAnimation(delta);
  }


  public void completeLevel(boolean success) {
    calculateStatistics(success);
    ScreenManager.instance().showScreen(new GameSummaryScreen(stats, metadata, gameType));
  }
}
