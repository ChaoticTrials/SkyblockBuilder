package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonPrimitive;
import de.melanx.skyblockbuilder.template.NetherPortalTemplate;
import de.melanx.skyblockbuilder.util.SkyPaths;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class NetherPortalTemplateMapper implements ValueMapper<NetherPortalTemplate, JsonPrimitive> {

    private static final InputProperties<NetherPortalTemplate> DEFAULT_PROPERTIES = new InputProperties<>() {

        @Override
        public NetherPortalTemplate defaultValue() {
            return null;
        }

        @Override
        public NetherPortalTemplate valueOf(String str) {
            return new NetherPortalTemplate(str);
        }

        @Override
        public String toString(NetherPortalTemplate str) {
            return str.getFilePath();
        }

        @Override
        public boolean isValid(String str) {
            return SkyPaths.NBT_OR_SNBT.test(SkyPaths.TEMPLATES_DIR.resolve(str).toFile());
        }
    };

    @Override
    public Class<NetherPortalTemplate> type() {
        return NetherPortalTemplate.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public NetherPortalTemplate fromJson(JsonPrimitive json) {
        return new NetherPortalTemplate(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(NetherPortalTemplate value) {
        return new JsonPrimitive(value.getFilePath());
    }

    @Override
    public ConfigEditor<NetherPortalTemplate> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(DEFAULT_PROPERTIES, validator);
    }
}
