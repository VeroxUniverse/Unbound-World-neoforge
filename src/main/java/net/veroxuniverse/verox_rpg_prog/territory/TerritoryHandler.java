package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryBorderFlashPayload;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryStatePayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public final class TerritoryHandler {

    private static final int CHECK_INTERVAL = 20;
    private static final int BORDER_INTERVAL = 40;
    private static final Map<UUID, Integer> ACTIVE_TINTS = new HashMap<>();

    private TerritoryHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % BORDER_INTERVAL == 0) {
            TerritoryBorderTracker.update(player);
        }
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        ServerLevel level = player.serverLevel();
        TerritoryMatcher.Match sealed = null;

        if (!player.isCreative() && !player.isSpectator()) {
            for (TerritoryMatcher.Match match : TerritoryMatcher.matchesAt(level, player.blockPosition())) {
                if (match.status() == StageStatus.LOCKED) {
                    sealed = match;
                    break;
                }
            }
        }

        int tint = sealed != null ? sealed.territory().tintColor() : 0;
        Integer previous = ACTIVE_TINTS.get(player.getUUID());

        if (previous == null || previous != tint) {
            ACTIVE_TINTS.put(player.getUUID(), tint);
            PacketDistributor.sendToPlayer(player, new TerritoryStatePayload(tint));

            if (sealed != null) {
                player.displayClientMessage(Component.translatable(
                        "message.verox_rpg_prog.territory_sealed",
                        Component.translatable(sealed.stage().translationKey())
                ), true);
            }
        }

        if (sealed != null) {
            float damage = sealed.territory().damagePerSecond()
                    + player.getMaxHealth() * sealed.territory().maxHealthPercentPerSecond();
            if (damage > 0.0F) {
                player.hurt(TerritoryDamage.source(level), damage);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE_TINTS.remove(event.getEntity().getUUID());
        TerritoryBorderTracker.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TerritoryBorderTracker.forget(player.getUUID());
            TerritoryBorderTracker.update(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (denyIfProtected(level, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (denyIfProtected(level, event.getPos(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<BlockPos> affected = event.getAffectedBlocks();
        affected.removeIf(pos -> TerritoryMatcher.isProtected(level, pos));
    }

    private static boolean denyIfProtected(ServerLevel level, BlockPos pos, Player player) {
        if (player.isCreative()) return false;

        TerritoryMatcher.Match protection = TerritoryMatcher.findProtection(level, pos);
        if (protection == null) return false;

        player.displayClientMessage(Component.translatable(
                "message.verox_rpg_prog.territory_protected",
                Component.translatable(protection.stage().translationKey())
        ), true);

        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.6F, 0.6F);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new TerritoryBorderFlashPayload(pos));
        }
        return true;
    }
}