package net.blay09.mods.waystones.mixin.client;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.client.resource.WaystonesAlternateResourcePack;
import net.minecraft.PackMetadataSection;
import net.minecraft.ResourcePack;
import net.minecraft.ResourcePackRepository;
import net.minecraft.ResourcePackRepositoryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

@Mixin(ResourcePackRepository.class)
public abstract class MixinResourcePackRepository {
    @Shadow
    private List repositoryEntriesAll;

    @Shadow
    @Final
    public ResourcePack rprDefaultResourcePack;

    private static WaystonesAlternateResourcePack waystones$modernityPack;

    @Inject(method = "updateRepositoryEntriesAll", at = @At("RETURN"))
    private void waystones$addModernityPack(CallbackInfo callback) {
        for (Object value : repositoryEntriesAll) {
            if (value instanceof ResourcePackRepositoryEntry entry
                    && entry.getResourcePack() != null
                    && WaystonesAlternateResourcePack.PACK_NAME.equals(entry.getResourcePack().getPackName())) {
                return;
            }
        }
        try {
            if (waystones$modernityPack == null) {
                waystones$modernityPack = new WaystonesAlternateResourcePack();
            }
            ResourcePackRepository repository = (ResourcePackRepository) (Object) this;
            ResourcePackRepositoryEntry entry = createEntry(repository);
            ResourcePackRepositoryEntryAccessor accessor = (ResourcePackRepositoryEntryAccessor) entry;
            accessor.waystones$setResourcePack(waystones$modernityPack);
            accessor.waystones$setMetadata(new PackMetadataSection(
                    "Modernity textures for Waystones-X, by DarkBum", 1));
            BufferedImage icon = waystones$modernityPack.getPackImage();
            accessor.waystones$setIcon(icon != null ? icon : rprDefaultResourcePack.getPackImage());
            repositoryEntriesAll.add(entry);
            Waystones.LOGGER.info("Modernity resource pack registered");
        } catch (ReflectiveOperationException exception) {
            Waystones.LOGGER.warn("Failed to register the Modernity resource pack", exception);
        }
    }

    private static ResourcePackRepositoryEntry createEntry(ResourcePackRepository repository)
            throws ReflectiveOperationException {
        for (Constructor<?> constructor : ResourcePackRepositoryEntry.class.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length >= 2
                    && parameters[0] == ResourcePackRepository.class
                    && parameters[1] == File.class) {
                Object[] arguments = new Object[parameters.length];
                arguments[0] = repository;
                arguments[1] = new File("waystones_modernity");
                constructor.setAccessible(true);
                return (ResourcePackRepositoryEntry) constructor.newInstance(arguments);
            }
        }
        throw new NoSuchMethodException("ResourcePackRepositoryEntry constructor");
    }
}
