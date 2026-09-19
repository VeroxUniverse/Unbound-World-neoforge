package net.veroxuniverse.unbound_world.compat.curios;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.veroxuniverse.unbound_world.stage.StageManager;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

public class CuriosRestrictionHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCurioCanEquip(CurioCanEquipEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || player.isCreative()) return;

        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (StageManager.isItemLocked(itemId)) {
            event.setEquipResult(TriState.FALSE);
            if (player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.unbound_world.item_locked"));
            }
        }
    }

    public static void checkAndUnequipCurios(ServerPlayer player) {
        if (player.isCreative()) return;

        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.getCurios().forEach((identifier, stacksHandler) -> {
                var stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        if (StageManager.isItemLocked(itemId)) {
                            ItemStack toReturn = stack.copy();
                            stacks.setStackInSlot(i, ItemStack.EMPTY);

                            if (!player.getInventory().add(toReturn)) {
                                player.drop(toReturn, false);
                            }

                            player.sendSystemMessage(
                                    Component.translatable("message.unbound_world.item_locked")
                            );
                        }
                    }
                }
            });
        });
    }
}