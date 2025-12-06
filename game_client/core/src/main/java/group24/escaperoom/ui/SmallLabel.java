package group24.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import group24.escaperoom.screens.AbstractScreen;

public class SmallLabel extends Label {
    public SmallLabel(String text){
        super(text, AbstractScreen.skin);
        setFontScale(0.65f);
    }
    public SmallLabel(String text, String style){
        super(text, AbstractScreen.skin);
        setStyle(AbstractScreen.skin.get(style, LabelStyle.class));
        setFontScale(0.65f);
    }
    public SmallLabel(String text, String style, float scale){
        super(text, AbstractScreen.skin);
        setStyle(AbstractScreen.skin.get(style, LabelStyle.class));
        setFontScale(scale);
    }
}
