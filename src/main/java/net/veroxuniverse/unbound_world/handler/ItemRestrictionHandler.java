package net.veroxuniverse.unbound_world.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ItemRestrictionHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || player.isCreative()) return;

        Slot slot = containerScreen.getSlotUnderMouse();
        if (slot == null) return;

        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean isShiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        int button = event.getButton();

        if (containerScreen.getMenu() instanceof InventoryMenu menu) {
            if (isShiftDown && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                if (slot.index >= 9 && slot.index <= 44) {
                    ItemStack slotStack = slot.getItem();
                    if (!slotStack.isEmpty() && slotStack.getItem() instanceof ArmorItem armorItem) {
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(slotStack.getItem());
                        if (isItemLocked(itemId)) {
                            EquipmentSlot eqSlot = armorItem.getEquipmentSlot();
                            int targetArmorIndex = switch (eqSlot) {
                                case HEAD -> 5;
                                case CHEST -> 6;
                                case LEGS -> 7;
                                case FEET -> 8;
                                default -> -1;
                            };

                            if (targetArmorIndex != -1 && !menu.getSlot(targetArmorIndex).hasItem()) {
                                event.setCanceled(true);
                                player.displayClientMessage(
                                        Component.translatable("message.unbound_world.item_locked"),
                                        true
                                );
                                return;
                            }
                        }
                    }
                }
            }

            if (slot.index >= 5 && slot.index <= 8) {
                ItemStack carried = menu.getCarried();
                if (!carried.isEmpty() && carried.getItem() instanceof ArmorItem) {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
                    if (isItemLocked(itemId)) {
                        event.setCanceled(true);
                        player.displayClientMessage(
                                Component.translatable("message.unbound_world.item_locked"),
                                true
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemStacked(ItemStackedOnOtherEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        Slot slot = event.getSlot();
        if (player.containerMenu instanceof InventoryMenu && slot.index >= 5 && slot.index <= 8) {
            ItemStack carried = event.getCarriedItem();
            if (!carried.isEmpty() && carried.getItem() instanceof ArmorItem) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
                if (isItemLocked(itemId)) {
                    event.setCanceled(true);
                    if (player.level().isClientSide) {
                        player.displayClientMessage(
                                Component.translatable("message.unbound_world.item_locked"),
                                true
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.setCanceled(true);
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.item_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = player.getMainHandItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.setCanceled(true);
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.item_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.clearModifiers();
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            List<Component> tooltip = event.getToolTip();

            tooltip.removeIf(component -> {
                if (component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
                    return translatable.getKey().startsWith("attribute.modifier.")
                            || translatable.getKey().startsWith("item.modifiers.");
                }
                return false;
            });

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.unbound_world.locked_title"));

            StageManager.getRequiredStageForItem(itemId).ifPresentOrElse(
                    stage -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_stage",
                            Component.translatable(stage.translationKey())
                    )),
                    () -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_stage",
                            Component.literal("Unknown Stage")
                    ))
            );

            tooltip.add(Component.translatable("tooltip.unbound_world.locked_status"));
        }
    }

    public static boolean isItemLocked(ResourceLocation itemId) {
        return StageManager.isItemLocked(itemId);
    }
}