package net.veroxuniverse.unbound_world.api.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.veroxuniverse.unbound_world.stage.StageDefinition;

/**
 * Fired on {@code NeoForge.EVENT_BUS} whenever a registered boss (main or optional boss of any
 * loaded stage) is killed by a player, after Unbound World's own drop and progression handling
 * has run.
 * <p>
 * This is NOT cancellable and does not affect drops or progression itself — it is purely
 * informational, intended for addons that want to react to boss kills (e.g. granting a loot bag
 * in addition to, or instead of, the normal drops via their own separate handler).
 */
public class BossDefeatedEvent extends Event {

    private final ServerLevel level;
    private final LivingEntity boss;
    private final ServerPlayer killer;
    private final StageDefinition.BossInfo bossInfo;
    private final StageDefinition owningStage;
    private final boolean wasMainBoss;

    public BossDefeatedEvent(
            ServerLevel level,
            LivingEntity boss,
            ServerPlayer killer,
            StageDefinition.BossInfo bossInfo,
            StageDefinition owningStage,
            boolean wasMainBoss
    ) {
        this.level = level;
        this.boss = boss;
        this.killer = killer;
        this.bossInfo = bossInfo;
        this.owningStage = owningStage;
        this.wasMainBoss = wasMainBoss;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public LivingEntity getBoss() {
        return this.boss;
    }

    public ServerPlayer getKiller() {
        return this.killer;
    }

    public StageDefinition.BossInfo getBossInfo() {
        return this.bossInfo;
    }

    public StageDefinition getOwningStage() {
        return this.owningStage;
    }

    /**
     * @return true if this was the stage's main boss (the one that advances progression),
     * false if it was one of the stage's optional preparation bosses.
     */
    public boolean wasMainBoss() {
        return this.wasMainBoss;
    }
}