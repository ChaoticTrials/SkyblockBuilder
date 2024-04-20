package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import net.minecraft.core.BlockPos;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.ModMappers;
import org.moddingx.libx.impl.config.mappers.special.RecordValueMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@RegisterMapper
public class SpreadInfoMapper implements ValueMapper<TemplateInfo.SpreadInfo, JsonObject> {

    @Override
    public Class<TemplateInfo.SpreadInfo> type() {
        return TemplateInfo.SpreadInfo.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public TemplateInfo.SpreadInfo fromJson(JsonObject json) {
        BlockPos minOffset;
        BlockPos maxOffset;

        if (json.has("minOffset") && json.has("maxOffset")) {
            minOffset = BlockPosMapper.fromJsonArray(json.getAsJsonArray("minOffset"));
            maxOffset = BlockPosMapper.fromJsonArray(json.getAsJsonArray("maxOffset"));
        } else if (json.has("offset")) {
            minOffset = BlockPosMapper.fromJsonArray(json.getAsJsonArray("offset"));
            maxOffset = minOffset;
        } else {
            throw new IllegalArgumentException("No offset provided: " + json);
        }

        return new TemplateInfo.SpreadInfo(json.get("file").getAsString(), minOffset, maxOffset);
    }

    @Override
    public JsonObject toJson(TemplateInfo.SpreadInfo value) {
        JsonObject json = new JsonObject();
        json.addProperty("file", value.file());
        if (value.minOffset().asLong() == value.maxOffset().asLong()) {
            json.add("offset", BlockPosMapper.toJsonArray(value.minOffset()));
        } else {
            json.add("minOffset", BlockPosMapper.toJsonArray(value.minOffset()));
            json.add("maxOffset", BlockPosMapper.toJsonArray(value.maxOffset()));
        }

        return json;
    }

    @Override
    public ConfigEditor<TemplateInfo.SpreadInfo> createEditor(ValidatorInfo<?> validator) {
        try {
            Method method = ModMappers.class.getDeclaredMethod("getMapper", Type.class);
            method.setAccessible(true);

            ModMappers modMappers = ModMappers.get(SkyblockBuilder.getInstance().modid);
            return new RecordValueMapper<>(SkyblockBuilder.getInstance().modid, TemplateInfo.SpreadInfo.class, type -> {
                try {
                    return (ValueMapper<?, ?>) method.invoke(modMappers, type);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }).createEditor(validator);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
