package xyz.eclipseisoffline.dimensionleveldata.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.eclipseisoffline.dimensionleveldata.DimensionLevelData;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    @Shadow
    public abstract DimensionDataStorage getDataStorage();

    @Shadow
    @Final
    @Mutable
    private ServerLevelData serverLevelData;
    @Unique
    private boolean primaryLevel = true;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    // Injecting at tail of method, and not directly replacing the serverLevelData PUTFIELD call, because getDataStorage() is not available yet when that call is made
    @Inject(method = "<init>", at = @At("TAIL"))
    public void setDimensionLevelData(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                      ServerLevelData serverLevelData, ResourceKey<Level> dimension, LevelStem levelStem,
                                      boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners, boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {
        if (serverLevelData instanceof DerivedLevelDataAccessor derived) {
            this.serverLevelData = DimensionLevelData.createForLevel(getDataStorage(), derived.getWorldData(), derived.getWrapped());
            ((LevelAccessor) this).setLevelData(this.serverLevelData);
            primaryLevel = false;
        }
    }

    @Redirect(method = "tickTime", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;tickTime:Z"))
    public boolean alwaysTickTime(ServerLevel instance) {
        return true;
    }

    @WrapOperation(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setGameTime(J)V"))
    public void noGameTimeTickWhenDerived(ServerLevelData instance, long gameTime, Operation<Void> original) {
        if (primaryLevel) {
            original.call(instance, gameTime);
        }
    }

    @WrapOperation(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/timers/TimerQueue;tick(Ljava/lang/Object;J)V"))
    public void noScheduledEventsWhenDerived(TimerQueue<MinecraftServer> instance, Object object, long gameTime, Operation<Void> original) {
        if (primaryLevel) {
            original.call(instance, object, gameTime);
        }
    }
}
