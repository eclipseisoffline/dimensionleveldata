package xyz.eclipseisoffline.dimensionleveldata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

import java.util.function.Supplier;

public class DimensionLevelData extends DerivedLevelData {

    private long dayTime = 0L;
    private int clearWeatherTime = 0;
    private boolean thundering = false;
    private int thunderTime = 0;
    private boolean raining = false;
    private int rainTime = 0;

    public DimensionLevelData(WorldData worldData, ServerLevelData wrapped) {
        super(worldData, wrapped);
    }

    @Override
    public long getDayTime() {
        return dayTime;
    }

    @Override
    public int getClearWeatherTime() {
        return clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int clearWeatherTime) {
        this.clearWeatherTime = clearWeatherTime;
    }

    @Override
    public boolean isThundering() {
        return thundering;
    }

    @Override
    public int getThunderTime() {
        return thunderTime;
    }

    @Override
    public boolean isRaining() {
        return raining;
    }

    @Override
    public int getRainTime() {
        return rainTime;
    }

    @Override
    public void setDayTime(long dayTime) {
        this.dayTime = dayTime;
    }

    @Override
    public void setThundering(boolean thundering) {
        this.thundering = thundering;
    }

    @Override
    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
    }

    @Override
    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    @Override
    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
    }

    public static DimensionLevelData createForLevel(DimensionDataStorage dataStorage, WorldData worldData, ServerLevelData primary) {
        return dataStorage.computeIfAbsent(Serialized.type(worldData, primary)).data;
    }

    private static class Serialized extends SavedData implements Supplier<DimensionLevelData> {

        private final DimensionLevelData data;

        private Serialized(DimensionLevelData data) {
            this.data = data;
            // Not ideal, but it'll work
            // Mojang should have made these properties dimension-based in the first place
            setDirty();
        }

        @Override
        public DimensionLevelData get() {
            return data;
        }

        private static SavedDataType<Serialized> type(WorldData worldData, ServerLevelData wrapped) {
            return new SavedDataType<>("dimension_level_data", () -> new Serialized(new DimensionLevelData(worldData, wrapped)), codec(worldData, wrapped), null);
        }

        private static Codec<Serialized> codec(WorldData worldData, ServerLevelData wrapped) {
            return RecordCodecBuilder.<DimensionLevelData>create(instance ->
                    instance.group(
                            Codec.LONG.fieldOf("day_time").forGetter(DimensionLevelData::getDayTime),
                            Codec.INT.fieldOf("clear_weather_time").forGetter(DimensionLevelData::getClearWeatherTime),
                            Codec.BOOL.fieldOf("thundering").forGetter(DimensionLevelData::isThundering),
                            Codec.INT.fieldOf("thunder_time").forGetter(DimensionLevelData::getThunderTime),
                            Codec.BOOL.fieldOf("raining").forGetter(DimensionLevelData::isRaining),
                            Codec.INT.fieldOf("rain_time").forGetter(DimensionLevelData::getRainTime)
                    ).apply(instance, (dayTime, clearWeatherTime, thundering, thunderTime, raining, rainTime) -> {
                        DimensionLevelData data = new DimensionLevelData(worldData, wrapped);
                        data.dayTime = dayTime;
                        data.clearWeatherTime = clearWeatherTime;
                        data.thundering = thundering;
                        data.thunderTime = thunderTime;
                        data.raining = raining;
                        data.rainTime = rainTime;
                        return data;
                    })
            ).xmap(Serialized::new, Serialized::get);
        }
    }
}
