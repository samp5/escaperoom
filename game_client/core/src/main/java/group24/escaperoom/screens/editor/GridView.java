package group24.escaperoom.screens.editor;

import java.util.Collection;

import group24.escaperoom.entities.Item;

public interface GridView {

  /**
   * Given some {@link Item}, return the {@link ItemDecoration} which should be applied
   */
  public ItemDecoration decorate(Item item);

  default public void apply(Collection<Item> items){
    for (Item item: items){
      ItemDecoration decoration = decorate(item);
      if (decoration != null){
        decoration.applyTo(item);
      }
    }
  }

  default public void reset(Collection<Item> items){
    for (Item item: items){
      ItemDecoration decoration = decorate(item);
      if (decoration != null){
        decoration.removeFrom(item);
      }
    }
  }

  public static GridView compose(GridView ... views){

    GridView gridView = new GridView() {
      @Override
      public ItemDecoration decorate(Item item) {
        ItemDecoration merged = new ItemDecoration();
        for (GridView view : views){
          ItemDecoration decoration = view.decorate(item);
          if (decoration != null) merged.merge(decoration);
        }

        return merged.hasModifications() ? merged : null;
      }
    };

    return gridView;
  }
}
