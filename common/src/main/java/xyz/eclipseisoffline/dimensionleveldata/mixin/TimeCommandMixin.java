package xyz.eclipseisoffline.dimensionleveldata.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @Redirect(method = {"suggestTimeMarkers", "queryTime", "queryTimelineTicks", "queryTimelineRepetitions",
            "setTotalTicks", "addTime", "setTimeToTimeMarker", "setPaused", "setRate"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager useSourceLevelClockManager(MinecraftServer instance, @Local(argsOnly = true) CommandSourceStack source) {
        return source.getLevel().clockManager();
    }
}
