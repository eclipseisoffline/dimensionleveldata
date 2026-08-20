package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    // When the advance time game rule changes, vanilla broadcasts the global clock manager's full sync
    // packet to all players - send every player the clock states of their own dimension instead
    @Redirect(method = "onGameRuleChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    public void broadcastDimensionClockSync(PlayerList playerList, Packet<?> packet) {
        for (ServerPlayer player : playerList.getPlayers()) {
            if (player.level() instanceof ServerLevel level) {
                player.connection.send(level.clockManager().createFullSyncPacket());
            }
        }
    }
}
