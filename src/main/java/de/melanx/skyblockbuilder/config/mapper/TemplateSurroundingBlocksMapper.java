package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;
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
public class TemplateSurroundingBlocksMapper implements ValueMapper<TemplateSurroundingBlocks, JsonObject> {

    @Override
    public Class<TemplateSurroundingBlocks> type() {
        return TemplateSurroundingBlocks.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public TemplateSurroundingBlocks fromJson(JsonObject json) {
        return TemplateSurroundingBlocks.fromJson(json);
    }

    @Override
    public JsonObject toJson(TemplateSurroundingBlocks templateSurroundingBlocks) {
        return TemplateSurroundingBlocks.toJson(templateSurroundingBlocks);
    }

    @Override
    public ConfigEditor<TemplateSurroundingBlocks> createEditor(ValidatorInfo<?> validator) {
        try {
            Method method = ModMappers.class.getDeclaredMethod("getMapper", Type.class);
            method.setAccessible(true);

            ModMappers modMappers = ModMappers.get(SkyblockBuilder.getInstance().modid);
            return new RecordValueMapper<>(SkyblockBuilder.getInstance().modid, TemplateSurroundingBlocks.class, type -> {
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
