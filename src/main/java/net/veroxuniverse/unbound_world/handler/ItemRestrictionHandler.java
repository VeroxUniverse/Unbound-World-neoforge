package net.veroxuniverse.unbound_world.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative()) return;

        Slot slot = containerScreen.getHoveredSlot();
        if (slot == null) return;

        if (slot instanceof ResultSlot || slot.container instanceof ResultContainer) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                Identifier blockId = stack.getItem() instanceof BlockItem bi
                        ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                        : null;

                if (isItemLocked(itemId) || (blockId != null && StageManager.isBlockLocked(blockId))) {
                    event.setCanceled(true);
                    player.sendOverlayMessage(
                            Component.translatable("message.unbound_world.item_locked")
                    );
                    return;
                }
            }
        }

        long window = mc.getWindow().handle();
        boolean isShiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        int button = event.getButton();

        if (containerScreen.getMenu() instanceof InventoryMenu menu) {
            if (isShiftDown && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                if (slot.index >= 9 && slot.index <= 44) {
                    ItemStack slotStack = slot.getItem();
                    Equippable equippable = slotStack.isEmpty() ? null : slotStack.get(DataComponents.EQUIPPABLE);
                    if (equippable != null) {
                        Identifier itemId = BuiltInRegistries.ITEM.getKey(slotStack.getItem());
                        if (isItemLocked(itemId)) {
                            EquipmentSlot eqSlot = equippable.slot();
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
                                    mc.gameMode.handleContainerInput(
                                            menu.containerId,
                                            slot.index,
                                            0,
                                            ContainerInput.PICKUP,
                                            player
                                    );
                                    mc.gameMode.handleContainerInput(
                                            menu.containerId,
                                            emptyTargetIndex,
                                            0,
                                            ContainerInput.PICKUP,
                                            player
                                    );
                                } else {
                                    player.sendOverlayMessage(
                                            Component.translatable("message.unbound_world.item_locked")
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
                Equippable carriedEquippable = carried.isEmpty() ? null : carried.get(DataComponents.EQUIPPABLE);
                if (carriedEquippable != null) {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
                    if (isItemLocked(itemId)) {
                        event.setCanceled(true);
                        player.sendOverlayMessage(
                                Component.translatable("message.unbound_world.item_locked")
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
            Equippable carriedEquippable = carried.isEmpty() ? null : carried.get(DataComponents.EQUIPPABLE);
            if (carriedEquippable != null) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
                if (isItemLocked(itemId)) {
                    event.setCanceled(true);
                    if (player.level().isClientSide()) {
                        player.sendOverlayMessage(
                                Component.translatable("message.unbound_world.item_locked")
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
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (isItemLocked(itemId)) {
                event.setCanceled(true);
                if (player.level().isClientSide()) {
                    player.sendOverlayMessage(
                            Component.translatable("message.unbound_world.item_locked")
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
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (isItemLocked(itemId)) {
                player.sendOverlayMessage(
                        Component.translatable("message.unbound_world.item_locked")
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = event.getItemStack();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.setCanceled(true);
            if (player.level().isClientSide()) {
                player.sendOverlayMessage(
                        Component.translatable("message.unbound_world.item_locked")
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack stack = player.getMainHandItem();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.setCanceled(true);
            if (player.level().isClientSide()) {
                player.sendOverlayMessage(
                        Component.translatable("message.unbound_world.item_locked")
                );
            }
        }
    }

    @SubscribeEvent
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (isItemLocked(itemId)) {
            event.clearModifiers();
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Identifier blockId = stack.getItem() instanceof BlockItem bi
                ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                : null;

        boolean blockLocked = blockId != null && StageManager.isBlockLocked(blockId);
        boolean oreLocked = !blockLocked && blockId != null && StageManager.isOreLocked(blockId);
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
        } else if (oreLocked) {
            List<Component> tooltip = event.getToolTip();
            tooltip.removeIf(component -> {
                if (component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
                    return translatable.getKey().startsWith("attribute.modifier.")
                            || translatable.getKey().startsWith("item.modifiers.");
                }
                return false;
            });

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.unbound_world.locked_ore_title"));
            StageManager.getRequiredStageForOre(blockId).ifPresentOrElse(
                    stage -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_ore_stage",
                            Component.translatable(stage.translationKey())
                    )),
                    () -> tooltip.add(Component.translatable(
                            "tooltip.unbound_world.locked_ore_stage",
                            Component.literal("Unknown Stage")
                    ))
            );
            tooltip.add(Component.translatable("tooltip.unbound_world.locked_ore_status"));
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

    public static boolean isItemLocked(Identifier itemId) {
        return StageManager.isItemLocked(itemId);
    }
}