package group24.escaperoom.ui.widgets;

import group24.escaperoom.screens.BackManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;

import group24.escaperoom.screens.AbstractScreen;

public class G24Dialog extends Dialog {
  public G24Dialog(String title){
    super(title, AbstractScreen.skin);
    G24Window.G24StyleWindow(this, title);
  }

  public <T> void waitFor(CompletableFuture<T> future, Function<T, Void> onComplete) {
    future.thenAccept((T t) -> {
      Gdx.app.postRunnable(() -> {
        onComplete.apply(t);
      });
    });

  }

  @Override
  public Dialog show(Stage stage) {
    BackManager.addBack(() -> {
      if (getStage() != null){
        hide();
        return true;
      } else {
        return false;
      }
    });
    return super.show(stage);
  }

  @Override
  protected void result(Object object) {
    hide();
    cancel();
  }
}
