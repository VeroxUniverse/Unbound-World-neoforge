package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryBordersPayload;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class TerritoryBorderTracker {

    private static final int CHUNK_RADIUS = 4;
    private static final Map<UUID, List<BorderBox>> LAST_SENT = new HashMap<>();

    private TerritoryBorderTracker() {}

    public static void update(ServerPlayer player) {
        List<BorderBox> boxes = collect(player.serverLevel(), new ChunkPos(player.blockPosition()));
        List<BorderBox> previous = LAST_SENT.get(player.getUUID());

        if (previous == null || !previous.equals(boxes)) {
            LAST_SENT.put(player.getUUID(), boxes);
            PacketDistributor.sendToPlayer(player, new TerritoryBordersPayload(boxes));
        }
    }

    public static void forget(UUID playerId) {
        LAST_SENT.remove(playerId);
    }

    private static List<BorderBox> collect(ServerLevel level, ChunkPos center) {
        Map<BoundingBox, BorderBox> result = new LinkedHashMap<>();
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (StageDefinition stage : StageManager.getAllStages()) {
            StageStatus status = StageStatus.of(stage);
            if (status == StageStatus.CLEARED) continue;

            for (StageDefinition.Territory territory : stage.territories()) {
                if (territory.structures().isEmpty()) continue;
                if (status == StageStatus.CURRENT && !territory.protectStructures()) continue;

                int color = status == StageStatus.LOCKED ? BorderBox.LOCKED_COLOR : BorderBox.PROTECTED_COLOR;
                Predicate<Holder<Structure>> holderPredicate = TerritoryMatcher.structurePredicate(territory.structures());
                Predicate<Structure> structurePredicate = structure -> holderPredicate.test(registry.wrapAsHolder(structure));

                for (int chunkX = center.x - CHUNK_RADIUS; chunkX <= center.x + CHUNK_RADIUS; chunkX++) {
                    for (int chunkZ = center.z - CHUNK_RADIUS; chunkZ <= center.z + CHUNK_RADIUS; chunkZ++) {
                        if (!level.hasChunk(chunkX, chunkZ)) continue;

                        for (StructureStart start : level.structureManager().startsForStructure(new ChunkPos(chunkX, chunkZ), structurePredicate)) {
                            if (!start.isValid()) continue;
                            BoundingBox box = start.getBoundingBox();
                            result.putIfAbsent(box, new BorderBox(
                                    box.minX(), box.minY(), box.minZ(),
                                    box.maxX(), box.maxY(), box.maxZ(),
                                    color
                            ));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(result.values());
    }
}