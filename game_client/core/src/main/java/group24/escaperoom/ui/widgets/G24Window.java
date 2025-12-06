package group24.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.ui.SmallLabel;

public class G24Window extends Window  {
  protected SmallLabel label;
  public G24Window(String title){
    super(title, AbstractScreen.skin);
    
    label = G24StyleWindow(this, title);
  }

  public G24Window(String title,  String style){
    super(title, AbstractScreen.skin, style);
  }
  public static SmallLabel G24StyleWindow(Window window, String title){
    return G24StyleWindow(window, title, "default");
  }

  public static SmallLabel G24StyleWindow(Window window, String title, String style){
    window.setStyle(AbstractScreen.skin.get(style, WindowStyle.class));
    window.getTitleTable().clearChildren();
    window.getTitleTable().defaults().pad(10);
    SmallLabel label = null;
    if (!title.isEmpty()){
      label = new SmallLabel(title, "bubble");
    }
    window.getTitleTable().add(label).align(Align.center).padBottom(15);
    window.padTop(40);
    return label;
  }

  @Override 
  public Label getTitleLabel(){
    return label;
  }

  public void close() {
    remove();
  }
}
