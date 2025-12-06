package group24.escaperoom.game.ui;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.ui.widgets.G24TextButton;

public class PlayerInventoryCloseButton extends G24TextButton {

    public PlayerInventoryCloseButton(String label, Player player) {
        super(label);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.setInventoryOpen(false);
            }
        });
    }
}
