package xyz.eclipseisoffline.dimensionleveldata.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @Redirect(method = {"setTime", "addTime"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> useOnlySourceLevel(MinecraftServer instance, CommandSourceStack source) {
        return Collections.singleton(source.getLevel());
    }
}
