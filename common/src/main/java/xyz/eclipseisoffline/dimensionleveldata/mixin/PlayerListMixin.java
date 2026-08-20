package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    // Sync the clock states of the level the player is (re)spawning or teleporting into, instead of the
    // server-global ones
    @Redirect(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    public ServerClockManager useLevelClockManager(MinecraftServer instance, ServerPlayer player, ServerLevel level) {
        return level.clockManager();
    }
}
