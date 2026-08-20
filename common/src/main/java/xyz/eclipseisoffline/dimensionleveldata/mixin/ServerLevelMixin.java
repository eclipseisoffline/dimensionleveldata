package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    @Final
    private ServerLevelData serverLevelData;

    @Shadow
    public abstract SavedDataStorage getDataStorage();

    @Shadow
    public abstract TickRateManager tickRateManager();

    @Unique
    private @Nullable ServerClockManager dimensionClockManager;

    // The primary level (the level whose level data is not derived, usually the overworld) keeps using the
    // server-global weather data and clock manager, all other levels store their own in their level data storage
    @Unique
    private boolean derivedLevel() {
        return serverLevelData instanceof DerivedLevelData;
    }

    @Redirect(method = {"<init>", "getWeatherData"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWeatherData()Lnet/minecraft/world/level/saveddata/WeatherData;"))
    public WeatherData useDimensionWeatherData(MinecraftServer server) {
        if (derivedLevel()) {
            return getDataStorage().computeIfAbsent(WeatherData.TYPE);
        }
        return server.getWeatherData();
    }

    @Redirect(method = {"clockManager()Lnet/minecraft/world/clock/ServerClockManager;", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    public ServerClockManager useDimensionClockManager(MinecraftServer server) {
        if (!derivedLevel()) {
            return server.clockManager();
        }
        if (dimensionClockManager == null) {
            // init must only be called once per manager, as it resets the clock states to the ones read from disk
            dimensionClockManager = getDataStorage().computeIfAbsent(ServerClockManager.TYPE);
            dimensionClockManager.init(server);
        }
        return dimensionClockManager;
    }

    // Vanilla only ticks the server-global clock manager (in MinecraftServer#tickChildren), so the
    // per-dimension managers are ticked here, with the same tick rate condition vanilla uses
    @Inject(method = "tick", at = @At("HEAD"))
    public void tickDimensionClocks(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (dimensionClockManager != null && tickRateManager().runsNormally()) {
            dimensionClockManager.tick();
        }
    }
}
