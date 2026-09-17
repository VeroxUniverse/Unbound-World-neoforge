package net.veroxuniverse.unbound_world.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
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

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative()) return;

        Slot slot = containerScreen.getSlotUnderMouse();
        if (slot == null) return;

        if (slot instanceof ResultSlot || slot.container instanceof ResultContainer) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ResourceLocation blockId = stack.getItem() instanceof BlockItem bi
                        ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                        : null;

                if (isItemLocked(itemId) || (blockId != null && StageManager.isBlockLocked(blockId))) {
                    event.setCanceled(true);
                    player.displayClientMessage(
                            Component.translatable("message.unbound_world.item_locked"),
                            true
                    );
                    return;
                }
            }
        }

        long window = mc.getWindow().getWindow();
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

                                int targetStart = (slot.index >= 36) ? 9 : 36;
                                int targetEnd = (slot.index >= 36) ? 35 : 44;

                                int emptyTargetIndex = -1;
                                for (int i = targetStart; i <= targetEnd; i++) {
                                    if (!menu.getSlot(i).hasItem()) {
                                        emptyTargetIndex = i;
                                        break;
                                    }
                                }

                                if (emptyTargetIndex != -1 && mc.gameMode != null) {
                                    mc.gameMode.handleInventoryMouseClick(
                                            menu.containerId,
                                            slot.index,
                                            0,
                                            ClickType.PICKUP,
                                            player
                                    );
                                    mc.gameMode.handleInventoryMouseClick(
                                            menu.containerId,
                                            emptyTargetIndex,
                                            0,
                                            ClickType.PICKUP,
                                            player
                                    );
                                } else {
                                    player.displayClientMessage(
                                            Component.translatable("message.unbound_world.item_locked"),
                                            true
                                    );
                                }
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
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
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
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (isItemLocked(itemId)) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.item_locked"),
                        true
                );
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
        if (stack.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ResourceLocation blockId = stack.getItem() instanceof BlockItem bi
                ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                : null;

        boolean blockLocked = blockId != null && StageManager.isBlockLocked(blockId);
        boolean itemLocked = isItemLocked(itemId);

        if (blockLocked) {
            List<Component> tooltip = event.getToolTip();
            tooltip.removeIf(component -> {
                if (component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
                    return translatable.getKey().startsWith("attribute.modifier.")
                            || translatable.getKey().startsWith("item.modifiers.");
                }
                return false;
            });

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_title"));
            StageManager.getRequiredStageForBlock(blockId).ifPresentOrElse(
                    stage -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_block_stage",
                            Component.translatable(stage.translationKey())
                    )),
                    () -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_block_stage",
                            Component.literal("Unknown Stage")
                    ))
            );
            tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_status"));
        } else if (itemLocked) {
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