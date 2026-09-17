package net.veroxuniverse.unbound_world.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.veroxuniverse.unbound_world.stage.WorldStageSavedData;

public class StageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("unbound")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("stage")
                                .then(Commands.literal("get")
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            int current = WorldStageSavedData.get(level).getUnlockedOrder();
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§6[Unbound World]§r Current unlocked order: §e" + current), false);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("order", IntegerArgumentType.integer(-1))
                                                .executes(context -> {
                                                    int order = IntegerArgumentType.getInteger(context, "order");
                                                    ServerLevel level = context.getSource().getLevel();
                                                    WorldStageSavedData.get(level).setUnlockedOrder(order);

                                                    context.getSource().sendSuccess(() ->
                                                            Component.literal("§6[Unbound World]§r Stage progression set to order: §a" + order), true);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            WorldStageSavedData.get(level).setUnlockedOrder(-1);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§6[Unbound World]§r Stage progression reset to start (-1)."), true);
                                            return 1;
                                        })
                                )
                        )
        );
    }
}