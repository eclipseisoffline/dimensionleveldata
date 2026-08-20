package xyz.eclipseisoffline.dimensionleveldata.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.world.level.saveddata.WeatherData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeatherCommand.class)
public abstract class WeatherCommandMixin {

    // Same weather parameter assignments as MinecraftServer#setWeatherParameters, but applied to the
    // weather data of the level the command was executed in
    @Redirect(method = {"setClear", "setRain", "setThunder"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setWeatherParameters(IIZZ)V"))
    private static void setSourceLevelWeather(MinecraftServer instance, int clearTime, int weatherTime, boolean isRaining, boolean isThundering,
                                              @Local(argsOnly = true) CommandSourceStack source) {
        WeatherData weatherData = source.getLevel().getWeatherData();
        weatherData.setClearWeatherTime(clearTime);
        weatherData.setRainTime(weatherTime);
        weatherData.setThunderTime(weatherTime);
        weatherData.setRaining(isRaining);
        weatherData.setThundering(isThundering);
    }
}
