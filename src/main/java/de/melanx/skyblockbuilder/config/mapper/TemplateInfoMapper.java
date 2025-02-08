package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import de.melanx.skyblockbuilder.config.values.providers.SpreadsProvider;
import de.melanx.skyblockbuilder.template.TemplateInfo;
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
        SpawnsProvider spawns = SpawnsProvider.fromJson(json.get("spawns"));

        String desc = "";
        if (json.has("desc")) {
            desc = json.get("desc").getAsString();
        }

        TemplateInfo.Offset offset = new TemplateInfo.Offset(TemplatesConfig.defaultOffset, 0, TemplatesConfig.defaultOffset);
        if (json.has("offset")) {
            JsonArray offsetArray = json.get("offset").getAsJsonArray();
            offset = new TemplateInfo.Offset(offsetArray.get(0).getAsInt() + TemplatesConfig.defaultOffset, offsetArray.get(1).getAsInt(), offsetArray.get(2).getAsInt() + TemplatesConfig.defaultOffset);
        } else if (TemplatesConfig.defaultOffset != 0) {
            offset = new TemplateInfo.Offset(TemplatesConfig.defaultOffset, 0, TemplatesConfig.defaultOffset);
        }

        String surroundingBlocks = "";
        if (json.has("surroundingBlocks")) {
            surroundingBlocks = json.get("surroundingBlocks").getAsString();
        }

        int surroundingMargin = 0;
        if (json.has("surroundingMargin")) {
            surroundingMargin = json.get("surroundingMargin").getAsInt();
        }

        SpreadsProvider spreads = SpreadsProvider.fromJson(json.get("spreads"));

        return new TemplateInfo(name, desc, file, spawns, offset, surroundingBlocks, spreads, surroundingMargin);
    }

    @Override
    public JsonObject toJson(TemplateInfo templateInfo) {
        JsonObject json = new JsonObject();
        json.addProperty("name", templateInfo.name());

        if (!templateInfo.desc().isBlank()) {
            json.addProperty("desc", templateInfo.desc());
        }

        json.addProperty("file", templateInfo.file());
        json.add("spawns", templateInfo.spawns().toJson());

        if (templateInfo.offset().x() != TemplatesConfig.defaultOffset || templateInfo.offset().z() != TemplatesConfig.defaultOffset) {
            JsonArray offsetArray = new JsonArray();
            offsetArray.add(templateInfo.offset().x());
            offsetArray.add(templateInfo.offset().y());
            offsetArray.add(templateInfo.offset().z());
            json.add("offset", offsetArray);
        }

        if (!templateInfo.surroundingBlocks().isEmpty()) {
            json.addProperty("surroundingBlocks", templateInfo.surroundingBlocks());
        }

        if (templateInfo.surroundingMargin() > 0) {
            json.addProperty("surroundingMargin", templateInfo.surroundingMargin());
        }

        if (templateInfo.spreads() != null) {
            json.add("spreads", templateInfo.spreads().toJson());
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
