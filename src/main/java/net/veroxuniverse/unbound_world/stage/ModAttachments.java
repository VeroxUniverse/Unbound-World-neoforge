package net.veroxuniverse.unbound_world.stage;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.veroxuniverse.unbound_world.UnboundWorld;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UnboundWorld.MOD_ID);

    public static final Supplier<AttachmentType<WorldStageData>> WORLD_STAGE_DATA = ATTACHMENT_TYPES.register(
            "world_stage_progression",
            () -> AttachmentType.builder(() -> new WorldStageData())
                    .serialize(WorldStageData.CODEC)
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}