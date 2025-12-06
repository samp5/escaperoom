package group24.escaperoom.game.ui;


import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.ui.ConfirmDialog;
import group24.escaperoom.ui.widgets.G24Dialog;
import group24.escaperoom.ui.widgets.G24TextButton;

/**
 * Game dialog is different than {@link ConfirmDialog} in that it closes when the player moves away
 *
 */
public class GameDialog extends G24Dialog {
  Player player;
  Vector2 playerPos;

  public void setContent(Actor a){
    getContentTable().add(a).center();
  }

  public GameDialog(Player player, String title) {
    super(title);
    setModal(false);

    this.player = player;
    this.playerPos = player.getCenter();
    button(new G24TextButton("Continue..."));
  }

  public GameDialog(Actor a, Player player, String title) {
    this(player, title);

    setContent(a);
  }

  @Override
  public void act(float delta){
    super.act(delta);
    if (!playerPos.equals(player.getCenter())){
      hide(Actions.fadeOut(0.1f, Interpolation.fade));
    }
  }
}
