package group24.escaperoom.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

import group24.escaperoom.AssetManager;
import group24.escaperoom.ScreenManager;
import group24.escaperoom.data.MapLoader;
import group24.escaperoom.data.User;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.screens.MapSelectScreen.MapSelectScreenBuilder;

public class OnlineMainMenu extends MenuScreen {
  G24TextButton myMapsButton = new G24TextButton("My Maps");
  private ChangeListener onMyMaps = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(
          new MapSelectScreenBuilder(new OnlineMainMenu())
              .withMaps(MapLoader.discoverMaps())
              .edit()
              .play()
              .delete()
              .creation()
              .verify()
              .build());
    }
  };

  G24TextButton logOutButton = new G24TextButton("Log Out");
  private ChangeListener onLogOut = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      User.logOut();
      ScreenManager.instance().showScreen(new MainMenuScreen());
    }
  };

  G24TextButton marketPlaceButton = new G24TextButton("Marketplace");
  private ChangeListener onMarketplace = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(new MarketplaceScreen());
    }
  };

  G24TextButton leaderBoardButton = new G24TextButton("Leaderboard");
  private ChangeListener onLeaderboard = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(new LeaderboardScreen());
    }
  };

  public OnlineMainMenu() {
    myMapsButton.addListener(onMyMaps);
    logOutButton.addListener(onLogOut);
    marketPlaceButton.addListener(onMarketplace);
    leaderBoardButton.addListener(onLeaderboard);

    rootTable.defaults().pad(10);

    Image title_img = null;
    try {
      String path = "textures/title_online.png";
      AssetManager.instance().load(path, Texture.class);
      AssetManager.instance().finishLoadingAsset(path);
      title_img = new Image(AssetManager.instance().get(path, Texture.class));
      title_img.setScaling(Scaling.contain);
    } catch (Exception gdxre) {
      System.err.println("failed to load title img");
      return;
    }
    rootTable.add(title_img).center().padLeft(20).padRight(20).padBottom(0);
    rootTable.row();
    rootTable.add(myMapsButton).minWidth(300);
    rootTable.row();
    rootTable.add(marketPlaceButton).minWidth(300);
    rootTable.row();
    rootTable.add(leaderBoardButton).minWidth(300);
    rootTable.row();
    rootTable.add(logOutButton).minWidth(300);
    rootTable.setFillParent(true);

  }
}
