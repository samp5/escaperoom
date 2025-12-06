package group24.escaperoom.ui;

import java.util.HashMap;
import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.ui.widgets.G24Dialog;
import group24.escaperoom.ui.widgets.G24TextButton;

public class ConfirmDialog extends G24Dialog {
  @SuppressWarnings("unused")
  private Builder builder = new Builder("<no title>");

  private enum ButtonActionType {
    Confirm,
    Cancel,
    TakeAction,
  }

  public interface CloseHandler {
    public boolean onClose();
  }

  private static class ButtonAction {
    @SuppressWarnings("unused")
    ButtonActionType type;
    CloseHandler action;
    public ButtonAction(ButtonActionType type, CloseHandler action){
      this.type = type;
      this.action = action;
    }
  }


  public static class Builder {
    private String title; 
    private String confirmText = "Confirm";
    private String cancelText = "Cancel";
    private HashMap<G24TextButton, ButtonAction> otherActions = new HashMap<>();
    private Array<ContentSettings> content = new Array<>();
    private ButtonAction onConfirm = new ButtonAction(ButtonActionType.Confirm, () -> true);
    private ButtonAction onCancel = new ButtonAction(ButtonActionType.Cancel, () -> true);
    private Optional<Button> spawnedBy = Optional.empty();
    private record ContentSettings(Actor a, boolean row) {}


    public Builder(String title){
      this.title = title;
    }

    public Builder(String title, Actor content){
      this.title = title;
      this.content.add(new ContentSettings(content, false));
    }

    public Builder onConfirm(CloseHandler runnable){
      this.onConfirm.action = runnable;
      return this;
    }
    public Builder withContent(Actor content, boolean rowAfter){
      this.content.add(new ContentSettings(content, rowAfter)); 
      return this;
    }

    /**
     * Toggles whether {@code b} is enabled when showing and hiding this dialog
     *
     * Assuming {@code b} spawns this dialog, this prevents multiple dialogs
     * on repeated button presses
     *
     * @param b button to toggle enable status
     * @return the builder
     */
    public Builder disableSpawner(Button b){
      this.spawnedBy = Optional.of(b);
      return this;
    }

    public Builder onCancel(CloseHandler handler){
      this.onCancel.action = handler;
      return this;
    }

    public Builder withButton(String buttonText, CloseHandler action){
      this.otherActions.put(
        new G24TextButton(buttonText), 
        new ButtonAction(ButtonActionType.TakeAction, action)
      );

      return this;
    }

    public Builder confirmText(String msg){
      this.confirmText = msg;
      return this;
    }

    public Builder cancelText(String msg){
      this.cancelText = msg;
      return this;
    }

    public ConfirmDialog build(){
      return new ConfirmDialog(this);
    }
  }

	public ConfirmDialog(String title) {
    this(new Builder(title));
	}

  protected ConfirmDialog(Builder builder){
    super(builder.title);

    setModal(true);

    this.builder = builder;

    getContentTable().defaults().padLeft(5).padRight(5);
    builder.content.forEach(cs -> {
      getContentTable().add(cs.a);
      if (cs.row) getContentTable().row();
    });
    builder.otherActions.forEach((b, a) -> button(b, a));

    G24TextButton confirmButton = new G24TextButton(builder.confirmText);
    G24TextButton cancelButton = new G24TextButton(builder.cancelText);
    confirmButton.autoResetCheck();
    cancelButton.autoResetCheck();

    button(confirmButton, builder.onConfirm);
    button(cancelButton, builder.onCancel);
  }

  @Override
  protected void result(Object object) {
    ButtonAction buttonAction = (ButtonAction)object;
    if (!buttonAction.action.onClose()){
      cancel();
      return;
    }
    super.result(object);
  }

  @Override
  public Dialog show(Stage stage) {
      builder.spawnedBy.ifPresent((b) -> b.setDisabled(true));
      return super.show(stage);
  }

  @Override
  public void hide() {
    builder.spawnedBy.ifPresent((b) -> b.setDisabled(false));
    super.hide();
  }
}
