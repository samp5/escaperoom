package group24.escaperoom.entities.properties;

import java.util.HashSet;


import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.Types.IntVector2;

public class ConnectorSink extends Connector {

  private static final PropertyDescription description = new PropertyDescription(
    "Connector Sink",
    "Accepts signals",
    "Connector items can have different types, connector sinks receive signals from other connectors of their same types.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription() {
    return description;
  }

	@Override
	public void propagate(GameContext ctx, HashSet<Integer> seen) {
    // Sinks do not propagate
	}

	@Override
	public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen) {
    if (source.getConnectorType() != this.type)
      return;
    this.connected = source.isConnected();
    updateColor();
	}

	@Override
	public void setActive(boolean connected, GameContext ctx) {
    this.connected = connected;
	}

	@Override
	public String getDisplayName() {
    return "Connector Sink";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.ConnectorSink;
	}
}
