package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class TerritoryMatcher {

    public record Match(StageDefinition stage, StageDefinition.Territory territory, boolean inStructure) {
        public StageStatus status() {
            return StageStatus.of(this.stage);
        }
    }

    private TerritoryMatcher() {}

    public static List<Match> matchesAt(ServerLevel level, BlockPos pos) {
        List<Match> matches = new ArrayList<>();
        Holder<Biome> biome = null;

        for (StageDefinition stage : StageManager.getAllStages()) {
            for (StageDefinition.Territory territory : stage.territories()) {
                if (isInStructure(level, pos, territory)) {
                    matches.add(new Match(stage, territory, true));
                    continue;
                }
                if (!territory.biomes().isEmpty()) {
                    if (biome == null) {
                        biome = level.getBiome(pos);
                    }
                    if (matchesBiome(biome, territory.biomes())) {
                        matches.add(new Match(stage, territory, false));
                    }
                }
            }
        }
        return matches;
    }

    public static boolean isInStructure(ServerLevel level, BlockPos pos, StageDefinition.Territory territory) {
        if (territory.structures().isEmpty()) return false;
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structurePredicate(territory.structures()));
        return start.isValid();
    }

    public static Predicate<Holder<Structure>> structurePredicate(List<String> entries) {
        List<Predicate<Holder<Structure>>> predicates = new ArrayList<>();
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id != null) {
                    TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, id);
                    predicates.add(holder -> holder.is(tag));
                }
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null) {
                    ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
                    predicates.add(holder -> holder.is(key));
                }
            }
        }
        return holder -> {
            for (Predicate<Holder<Structure>> predicate : predicates) {
                if (predicate.test(holder)) return true;
            }
            return false;
        };
    }

    public static boolean matchesBiome(Holder<Biome> biome, List<String> entries) {
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id != null && biome.is(TagKey.create(Registries.BIOME, id))) return true;
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null && biome.is(ResourceKey.create(Registries.BIOME, id))) return true;
            }
        }
        return false;
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        return findProtection(level, pos) != null;
    }

    public static Match findProtection(ServerLevel level, BlockPos pos) {
        for (StageDefinition stage : StageManager.getAllStages()) {
            if (StageStatus.of(stage) == StageStatus.CLEARED) continue;

            for (StageDefinition.Territory territory : stage.territories()) {
                if (!territory.protectStructures()) continue;
                if (isInStructure(level, pos, territory)) {
                    return new Match(stage, territory, true);
                }
            }
        }
        return null;
    }
}