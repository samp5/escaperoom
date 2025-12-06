package group24.escaperoom.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.Notification;
import group24.escaperoom.data.NotificationBus;
import group24.escaperoom.data.NotificationBus.NotificationListener;

public class NotificationOverlay extends Table implements NotificationListener {
  private final Array<Toast> activeNotifiations = new Array<>();

  private static class Toast extends Container<SmallLabel> {
    final Notification notification;

    Toast(Notification notif){
      this.notification = notif;
      setActor(new SmallLabel(notification.getMessage(), "bubble"));
    }
  }

	@Override
	public void onNotify(Notification notification) {
    Toast toast = new Toast(notification);
    showToast(toast);
	}

  private void showToast(Toast toast){
    activeNotifiations.add(toast);
    add(toast).right().pad(4);
    row();

    if (toast.notification.getSource() != null){
      toast.notification.getSource().addAction(
        Actions.sequence(
          Actions.color(new Color(1,0,0,1), 0.2f), 
          Actions.color(new Color(1,0,0,0.5f), 0.2f), 
          Actions.color(new Color(1,1,1,1), 0.2f)));
    }


    if (!toast.notification.isPersistent()){
      addAction(Actions.sequence(
        Actions.delay(toast.notification.getDuration())
        , Actions.run(() -> removeToast(toast))));
    }
  }

  public NotificationOverlay(){
    setFillParent(true);
    align(Align.topRight);
    pad(10);
    NotificationBus.get().addListener(this);
  }

  private void removeToast(Toast toast){
    activeNotifiations.removeValue(toast, false);
    if (activeNotifiations.isEmpty()){
      clear();
    }
    toast.remove();
  }
}
