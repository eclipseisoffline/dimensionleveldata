package xyz.eclipseisoffline.dimensionleveldata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.NotNull;

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
        return dataStorage.computeIfAbsent(Serialized.factory(worldData, primary), "dimension_level_data").data;
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
        public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putLong("day_time", data.dayTime);
            tag.putInt("clear_weather_time", data.clearWeatherTime);
            tag.putBoolean("thundering", data.thundering);
            tag.putInt("thunder_time", data.thunderTime);
            tag.putBoolean("raining", data.raining);
            tag.putInt("rain_time", data.rainTime);

            return tag;
        }

        @Override
        public DimensionLevelData get() {
            return data;
        }

        private static Serialized load(WorldData worldData, ServerLevelData wrapped, CompoundTag tag, HolderLookup.Provider registries) {
            DimensionLevelData data = new DimensionLevelData(worldData, wrapped);
            data.dayTime = tag.getLong("day_time");
            data.clearWeatherTime = tag.getInt("clear_weather_time");
            data.thundering = tag.getBoolean("thundering");
            data.thunderTime = tag.getInt("thunder_time");
            data.raining = tag.getBoolean("raining");
            data.rainTime = tag.getInt("rain_time");

            return new Serialized(data);
        }

        private static Factory<Serialized> factory(WorldData worldData, ServerLevelData wrapped) {
            return new Factory<>(() -> new Serialized(new DimensionLevelData(worldData, wrapped)),
                    (tag, registries) -> load(worldData, wrapped, tag, registries), null);
        }
    }
}
