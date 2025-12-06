package group24.escaperoom.entities.properties;



import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.GameEvent;
import group24.escaperoom.data.GameEvent.EventType;
import group24.escaperoom.data.GameEventBus;
import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.screens.BackManager;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24TextInput;
import group24.escaperoom.ui.GameDialog;
import group24.escaperoom.ui.editor.ConfigurationMenu;
import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.ConfigurationMenu.HandlesMenuClose;
import group24.escaperoom.ui.editor.Menu.MenuEntry;
import group24.escaperoom.ui.editor.Menu.MenuEntryBuilder;

public class PassphraseLock extends LockingMethod implements StringItemPropertyValue {
  protected class TryUnlock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Enter passphrase";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      if (ctx.player == null) {
        return ActionResult.DEFAULT;
      }

      GameDialog dialog = new GameDialog(ctx.player, "Enter Passphrase");

      Table table = new Table();
      G24TextInput[] chars = new G24TextInput[passphrase.length()];
      for (int i = 0; i < passphrase.length(); i++) {
        G24TextInput input = new G24TextInput();
        input.setMessageText("?");
        input.enableAutoFocusTraversal();
        input.setMaxLength(1);
        chars[i] = input;
      }

      G24TextButton submitButton = new G24TextButton("Try Unlock");

      submitButton.setProgrammaticChangeEvents(false);
      submitButton.addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (submitButton.isPressed()) {
            String result = new String();
            for (G24TextInput input : chars) {
              result += input.getText();
            }
            if (result.equals(passphrase)) {
              isLocked = false;
              if (isBarrier) {
                owner.ifPresent((i) -> {
                  i.setBlocksPlayer(false);
                  i.setAlpha(0.5f);
                });
              }
              dialog.hide();
              submitButton.setDisabled(true);
              GameEventBus.get().post(
                new GameEvent.Builder(EventType.ItemStateChange, ctx)
                  .message(owner.get().getItemName() + " clicked open...")
                  .build()
              );

              if (actor.getStage().getKeyboardFocus() instanceof G24TextInput){
                BackManager.goBack();
              }
              BackManager.goBack();
            }
          }
          submitButton.setChecked(false);
        }
      });
      table.defaults().pad(0).center();
      for (G24TextInput input : chars) {
        table.add(input).maxWidth(20).minWidth(0);
      }
      table.row();
      table.add(submitButton).align(Align.center).colspan(chars.length);
      table.row();

      dialog.setContent(table);
      return new ActionResult().showsDialog(dialog);
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked();
    }
  };

  public String passphrase = "a1b2";

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public PassphraseLock() {}

  public PassphraseLock(String val) {
    passphrase = val;
  }

  @Override
  public String getValue() {
    return passphrase;
  }

  @Override
  public void setValue(String value) {
    this.passphrase = value;
  }

  @Override
  public boolean isLocked() {
    return isLocked;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, getName()).spawns((e) -> {
      return new ConfigurationMenu<TextConfigurationContent>(e, configurationDisplay(), getName(), parent.getScreen());
    }).build();
  }

  private class TextConfigurationContent extends G24TextInput implements HandlesMenuClose {
    public TextConfigurationContent(String content){
      super(content);
    }
    @Override
    public void handle() {
    }
  }

  public TextConfigurationContent configurationDisplay() {
    TextConfigurationContent input = new TextConfigurationContent(this.passphrase);
    input.setMaxLength(10);
    input.setWidth(100);
    input.setAlphanumeric();

    input.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        passphrase = input.getText();
      }
    });
    return input;
  }

  @Override
  public String getName() {
    return "Passphrase";
  }

  @Override
  public Array<PlayerAction> getActions() {
    return Array.with(new TryUnlock());
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("passphrase", this.passphrase);
  }

  @Override
  public void read(Json json, JsonValue data) {
    super.read(json, data);
    this.passphrase = data.getString("passphrase", "a12b");
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.PassphraseLock;
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new PassphraseLock();
  }

  @Override
  protected PlayerAction maybeGetLockAction() {
    return null;
  }

  @Override
  protected PlayerAction maybeGetUnlockAction() {
    return new TryUnlock();
  }
}
