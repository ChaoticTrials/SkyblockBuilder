package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.values.TemplateSpawns;
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
public class TemplateSpawnsMapper implements ValueMapper<TemplateSpawns, JsonObject> {

    @Override
    public Class<TemplateSpawns> type() {
        return TemplateSpawns.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public TemplateSpawns fromJson(JsonObject json) {
        return TemplateSpawns.fromJson(json);
    }

    @Override
    public JsonObject toJson(TemplateSpawns spawns) {
        return TemplateSpawns.toJson(spawns);
    }

    @Override
    public ConfigEditor<TemplateSpawns> createEditor(ValidatorInfo<?> validator) {
        try {
            Method method = ModMappers.class.getDeclaredMethod("getMapper", Type.class);
            method.setAccessible(true);

            ModMappers modMappers = ModMappers.get(SkyblockBuilder.getInstance().modid);
            return new RecordValueMapper<>(SkyblockBuilder.getInstance().modid, TemplateSpawns.class, type -> {
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
