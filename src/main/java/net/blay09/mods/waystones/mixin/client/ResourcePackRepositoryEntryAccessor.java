package net.blay09.mods.waystones.mixin.client;

import net.minecraft.PackMetadataSection;
import net.minecraft.ResourcePack;
import net.minecraft.ResourcePackRepositoryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.awt.image.BufferedImage;

@Mixin(ResourcePackRepositoryEntry.class)
public interface ResourcePackRepositoryEntryAccessor {
    @Accessor("reResourcePack")
    void waystones$setResourcePack(ResourcePack resourcePack);

    @Accessor("rePackMetadataSection")
    void waystones$setMetadata(PackMetadataSection metadata);

    @Accessor("texturePackIcon")
    void waystones$setIcon(BufferedImage icon);
}
