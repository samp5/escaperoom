package group24.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import group24.escaperoom.screens.AbstractScreen;

public class G24Label extends Label {

  public G24Label(String content){
    this(content, "default", 0.65f);
  }

  public G24Label(String content, String style){
    this(content, style, 0.65f);
  }

  public G24Label(String content, String style, float scale){
    super(content, AbstractScreen.skin);
    setStyle(AbstractScreen.skin.get(style, LabelStyle.class));
    setFontScale(scale);
  }
}
