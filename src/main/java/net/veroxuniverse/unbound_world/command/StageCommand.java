package net.veroxuniverse.unbound_world.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;
import net.veroxuniverse.unbound_world.stage.WorldStageSavedData;
import net.veroxuniverse.unbound_world.util.StageNotifier;

import java.util.Optional;

public class StageCommand {

    private static final SuggestionProvider<CommandSourceStack> STAGE_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    StageManager.getAllStages().stream().map(s -> String.valueOf(s.order())),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("unbound")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("stage")
                                // /unbound stage get
                                .then(Commands.literal("get")
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            int current = WorldStageSavedData.get(level).getUnlockedOrder();
                                            String stageDisplay = getStageDisplayName(current);

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§6[Unbound World]§r Current Stage: §e" + stageDisplay + " §7(Order: " + current + ")"), false);
                                            return 1;
                                        })
                                )
                                // /unbound stage set <order>
                                .then(Commands.literal("set")
                                        .then(Commands.argument("order", IntegerArgumentType.integer(-1))
                                                .suggests(STAGE_SUGGESTIONS)
                                                .executes(context -> {
                                                    int order = IntegerArgumentType.getInteger(context, "order");
                                                    ServerLevel level = context.getSource().getLevel();
                                                    WorldStageSavedData data = WorldStageSavedData.get(level);

                                                    data.setUnlockedOrder(level, order);

                                                    findStageByOrder(order).ifPresent(stage ->
                                                            StageNotifier.broadcastStageUnlocked(level.getServer(), stage)
                                                    );

                                                    String stageDisplay = getStageDisplayName(order);
                                                    context.getSource().sendSuccess(() ->
                                                            Component.literal("§6[Unbound World]§r Stage set to: §a" + stageDisplay + " §7(" + order + ")"), true);
                                                    return 1;
                                                })
                                        )
                                )
                                // /unbound stage reset
                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            WorldStageSavedData.get(level).setUnlockedOrder(level, -1);

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§6[Unbound World]§r World progression reset to §cSealed World§r (-1)."), true);
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static String getStageDisplayName(int order) {
        if (order < 0) {
            return "Sealed World";
        }
        return findStageByOrder(order)
                .map(s -> Component.translatable(s.translationKey()).getString())
                .orElse("Stage " + order);
    }

    private static Optional<StageDefinition> findStageByOrder(int order) {
        return StageManager.getAllStages().stream()
                .filter(s -> s.order() == order)
                .findFirst();
    }
}