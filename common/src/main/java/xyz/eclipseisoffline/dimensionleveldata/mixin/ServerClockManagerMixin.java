package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerClockManager.class)
public abstract class ServerClockManagerMixin {

    // Clock updates are normally broadcast to all players - with per-dimension clock managers, only the
    // players in dimensions using this manager should receive its updates
    @Redirect(method = "modifyClock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    public void broadcastToOwnDimensionsOnly(PlayerList playerList, Packet<?> packet) {
        for (ServerPlayer player : playerList.getPlayers()) {
            if (player.level() instanceof ServerLevel level && level.clockManager() == (Object) this) {
                player.connection.send(packet);
            }
        }
    }
}
