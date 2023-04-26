package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.ModMappers;
import org.moddingx.libx.impl.config.mappers.special.RecordValueMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

@RegisterMapper
public class TemplateInfoMapper implements ValueMapper<TemplateInfo, JsonObject> {

    @Override
    public Class<TemplateInfo> type() {
        return TemplateInfo.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public TemplateInfo fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String file = json.get("file").getAsString();
        String spawns = json.get("spawns").getAsString();

        String desc = "";
        if (json.has("desc")) {
            desc = json.get("desc").getAsString();
        }

        TemplateInfo.Offset offset = new TemplateInfo.Offset(ConfigHandler.World.offset, 0, ConfigHandler.World.offset);
        if (json.has("offset")) {
            JsonArray offsetArray = json.get("offset").getAsJsonArray();
            offset = new TemplateInfo.Offset(offsetArray.get(0).getAsInt(), offsetArray.get(1).getAsInt(), offsetArray.get(2).getAsInt());
        }

        String str = json.get("direction").getAsString().toLowerCase(Locale.ROOT).strip();
        WorldUtil.Directions direction = WorldUtil.Directions.SOUTH;
        for (WorldUtil.Directions value : WorldUtil.Directions.values()) {
            if (value.name().toLowerCase(Locale.ROOT).equals(str)) {
                direction = value;
                break;
            }
        }

        String surroundingBlocks = "";
        if (json.has("surroundingBlocks")) {
            surroundingBlocks = json.get("surroundingBlocks").getAsString();
        }

        int surroundingMargin = 0;
        if (json.has("surroundingMargin")) {
            surroundingMargin = json.get("surroundingMargin").getAsInt();
        }

        return new TemplateInfo(name, desc, file, spawns, direction, offset, surroundingBlocks, surroundingMargin);
    }

    @Override
    public JsonObject toJson(TemplateInfo templateInfo) {
        JsonObject json = new JsonObject();
        json.addProperty("name", templateInfo.name());

        if (!templateInfo.desc().isBlank()) {
            json.addProperty("desc", templateInfo.desc());
        }

        json.addProperty("file", templateInfo.file());
        json.addProperty("spawns", templateInfo.spawns());
        json.addProperty("direction", templateInfo.direction().name().toLowerCase(Locale.ROOT));

        if (templateInfo.offset().x() != ConfigHandler.World.offset || templateInfo.offset().z() != ConfigHandler.World.offset) {
            JsonArray offsetArray = new JsonArray();
            offsetArray.add(templateInfo.offset().x());
            offsetArray.get(templateInfo.offset().y());
            offsetArray.add(templateInfo.offset().z());
            json.add("offset", offsetArray);
        }

        if (!templateInfo.surroundingBlocks().isEmpty()) {
            json.addProperty("surroundingBlocks", templateInfo.surroundingBlocks());
        }

        if (templateInfo.surroundingMargin() > 0) {
            json.addProperty("surroundingMargin", templateInfo.surroundingMargin());
        }

        return json;
    }


    @Override
    public ConfigEditor<TemplateInfo> createEditor(ValidatorInfo<?> validator) {
        try {
            Method method = ModMappers.class.getDeclaredMethod("getMapper", Type.class);
            method.setAccessible(true);

            ModMappers modMappers = ModMappers.get(SkyblockBuilder.getInstance().modid);
            return new RecordValueMapper<>(SkyblockBuilder.getInstance().modid, TemplateInfo.class, type -> {
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
