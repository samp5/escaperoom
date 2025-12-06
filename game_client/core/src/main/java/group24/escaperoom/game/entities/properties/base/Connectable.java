package group24.escaperoom.game.entities.properties.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import group24.escaperoom.editor.tools.TiledBrush;
import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.properties.Connector;
import group24.escaperoom.game.entities.properties.Connector.ConnectorType;
import group24.escaperoom.game.entities.properties.ConnectorBridge;
import group24.escaperoom.game.entities.properties.ConnectorRelay;
import group24.escaperoom.game.entities.properties.ConnectorSink;
import group24.escaperoom.game.entities.properties.ConnectorSource;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.TiledBrushable;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.screens.GameScreen;
import group24.escaperoom.screens.MapScreen;

public interface Connectable {

  public static final class ConnectableItem {
    public final Item item;
    public final Connectable connectable;

    public ConnectableItem(Item item, Connectable connectable) {
      this.item = item;
      this.connectable = connectable;
    }
  }

  public static final class Utils {

    public static Optional<ConnectableItem> isConnectable(Item i) {
      for (ConnectorType t : ConnectorType.values()) {
        Optional<ConnectableItem> ci = matches(i, t);
        if (ci.isPresent()) {
          return ci;
        }
      }
      return Optional.empty();
    }

    private static <T extends ItemPropertyValue, P extends ItemProperty<T>> Optional<ConnectableItem> tryGet(Item i,
        PropertyType propertyType, Class<P> expectedClass, ConnectorType connectorType) {
      try {
        Optional<Connectable> oC = i.getProperty(propertyType, expectedClass).map((c) -> (Connectable) c);
        if (oC.isPresent() && oC.get().getConnectorType() == connectorType) {
          return Optional.of(new ConnectableItem(i, oC.get()));
        }
      } catch (ClassCastException cce) {
      }
      return Optional.empty();
    }

    /**
     * @param i             An item to inspect
     * @param connectorType the type of connector we are interested in
     */
    public static Optional<ConnectableItem> matches(Item i, ConnectorType connectorType) {

      Optional<ConnectableItem> oCI = Optional.empty();

      oCI = tryGet(i, PropertyType.Connector, Connector.class, connectorType);
      if (oCI.isPresent()) {
        return oCI;
      }

      oCI = tryGet(i, PropertyType.ConnectorSource, ConnectorSource.class, connectorType);
      if (oCI.isPresent()) {
        return oCI;
      }

      oCI = tryGet(i, PropertyType.ConnectorRelay, ConnectorRelay.class, connectorType);
      if (oCI.isPresent()) {
        return oCI;
      }

      oCI = tryGet(i, PropertyType.ConnectorSink, ConnectorSink.class, connectorType);
      if (oCI.isPresent()) {
        return oCI;
      }

      oCI = tryGet(i, PropertyType.ConnectorBridge, ConnectorBridge.class, connectorType);
      if (oCI.isPresent()) {
        return oCI;
      }

      return Optional.empty();
    }

    /**
     * @param pos    A position to inspect
     * @param screen ref to the current map
     * @param type   the type of connector we are interested in finding
     */
    public static Optional<ConnectableItem> connectableAt(IntVector2 pos, MapScreen screen, ConnectorType type) {

      return screen.getItemsAt(pos.x, pos.y).flatMap((items) -> {

        for (int i = 0; i < items.length; i++) {
          Optional<ConnectableItem> oCI = matches(items[i], type);
          if (oCI.isPresent()) {
            return oCI;
          }
        }

        return Optional.empty();

      });

    }

    /**
     * 
     * Connectable items (even those that are not tileable) need to influence the
     * textures of surrounding tileable items.
     *
     * This function takes an item that was just placed, and updates any tileable
     * connectable items that surround this item.
     *
     * Note that this will do nothing for already tileable connectable items, as
     * their update logic is handled on placement
     * (either in game with the {@link GameScreen} DragAndDrop or in the level
     * editor in
     * {@link group24.escaperoom.editor.core.DragManager}
     *
     */
    public static void maybeUpateSurroundingTileables(Item justPlaced, MapScreen screen) {
      isConnectable(justPlaced).ifPresent((ci) -> {
        // do nothing if this item has the TiledBrushable property
        ci.item.getProperty(PropertyType.TiledBrushable, TiledBrushable.class).ifPresentOrElse((__) -> {
        },
            () -> {

              // try update in connection directions
              IntVector2[] updatePositions = ci.connectable.connectionDirections();

              for (IntVector2 updatePos : updatePositions) {
                // get items at position
                screen.getItemsAt(justPlaced.getX() + updatePos.x, justPlaced.getY() + updatePos.y)
                    .ifPresent((Item[] items) -> {
                      // for each item
                      Arrays.stream(items).forEach((item) -> {
                        isConnectable(item).ifPresent((ci2) -> {
                          // if connectable and a type match
                          if (ci2.connectable.getConnectorType() == ci.connectable.getConnectorType()) {
                            // update tileable if it is!
                            ci2.item.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
                            .ifPresent((tbp) -> {
                              TiledBrush.updateTiles(ci2.item.getPosition(), screen, ci2.item, false);
                            });
                          }
                        });
                      });
                    });
              }
            });
      });
    }
  }

  public static final IntVector2[] defaultConnectionDirections = { 
      new IntVector2(-1, 1), new IntVector2(0, 1), new IntVector2(1, 1), 
      new IntVector2(-1, 0),  new IntVector2(1, 0),
      new IntVector2(-1, -1), new IntVector2(0, -1), new IntVector2(1, -1), 
  };

  /**
   * Potentially propagate the the connectors current siganl
   * 
   * @param seen is IDs of all {@code Connectable} items which have already
   *             propagated
   */
  public void propagate(GameContext ctx, HashSet<Integer> seen);


  /**
   * By default, connectable items connect visually to 8 directions. 
   *
   *  ^   ^   ^
   *  ^   me  ^ 
   *  ^   ^   ^
   *
   *  Override this method to change visual connection directions
   */
  default public IntVector2[] connectionDirections(){
    return defaultConnectionDirections;
  }

  /**
   * Called when this {@code Connectable} receives a signal from another.
   * 
   * @param source the Connectable sending the signal
   * @param pos    the source's position
   * @param seen   A set of already visited IDs
   */
  public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen);

  /**
   * Set the {@code Connectable} to be {@code connected}
   *
   * This may or may not propagate the signal
   */
  public void setActive(boolean connected, GameContext ctx);

  /**
   * Get the type of connector.
   */
  public ConnectorType getConnectorType();

  /**
   * Get whether or not this Connectable is connected (active)
   */
  public boolean isConnected();
}
