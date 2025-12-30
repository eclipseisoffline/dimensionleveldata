package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DerivedLevelData.class)
public interface DerivedLevelDataAccessor {

    @Accessor
    WorldData getWorldData();

    @Accessor
    ServerLevelData getWrapped();
}
