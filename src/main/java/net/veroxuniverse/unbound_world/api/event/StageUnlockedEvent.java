package net.veroxuniverse.unbound_world.api.event;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.veroxuniverse.unbound_world.api.UnboundWorldApi;
import net.veroxuniverse.unbound_world.stage.StageDefinition;

import java.util.Optional;

/**
 * Fired on {@code NeoForge.EVENT_BUS} whenever the world's unlocked stage order changes,
 * including manual changes via {@code /unbound stage set} or {@code /unbound stage reset}.
 * <p>
 * This is NOT cancellable — the stage has already changed by the time this fires. Use it to
 * react to progression changes (e.g. spawn addon content, grant addon rewards), not to prevent them.
 */
public class StageUnlockedEvent extends Event {

    private final ServerLevel level;
    private final int previousOrder;
    private final int newOrder;

    public StageUnlockedEvent(ServerLevel level, int previousOrder, int newOrder) {
        this.level = level;
        this.previousOrder = previousOrder;
        this.newOrder = newOrder;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public int getPreviousOrder() {
        return this.previousOrder;
    }

    public int getNewOrder() {
        return this.newOrder;
    }

    /**
     * @return the stage definition that was just unlocked, if one is loaded for {@link #getNewOrder()}.
     * Empty if the new order does not correspond to any loaded stage (e.g. reset to Sealed World, -1).
     */
    public Optional<StageDefinition> getNewStage() {
        return UnboundWorldApi.getStageByOrder(this.newOrder);
    }
}