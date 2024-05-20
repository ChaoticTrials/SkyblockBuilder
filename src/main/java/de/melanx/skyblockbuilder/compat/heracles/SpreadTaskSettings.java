package de.melanx.skyblockbuilder.compat.heracles;

import de.melanx.skyblockbuilder.util.SkyPaths;
import earth.terrarium.heracles.api.client.settings.CustomizableQuestElementSettings;
import earth.terrarium.heracles.api.client.settings.Setting;
import earth.terrarium.heracles.api.client.settings.SettingInitializer;
import earth.terrarium.heracles.client.widgets.boxes.AutocompleteEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpreadTaskSettings implements SettingInitializer<SpreadLocationTask>, CustomizableQuestElementSettings<SpreadLocationTask> {

    public static final SpreadTaskSettings INSTANCE = new SpreadTaskSettings();
    private static final MyAutocompleteTextSetting SPREAD_SETTING = new MyAutocompleteTextSetting(
            () -> {
                try {
                    return Files.list(SkyPaths.SPREADS_DIR)
                            .filter(s -> s.toString().endsWith(".nbt") || s.toString().endsWith(".snbt"))
                            .filter(Files::isRegularFile)
                            .map(s -> {
                                String fileName = SkyPaths.SPREADS_DIR.relativize(s).toString();
                                int extensionMarker = fileName.lastIndexOf('.');
                                if (extensionMarker > 0) {
                                    fileName = fileName.substring(0, extensionMarker);
                                }

                                return fileName;
                            })
                            .toList();
                } catch (IOException e) {
                    return new ArrayList<>();
                }
            },
            (text, item) -> {
                String[] keywords = text.split("\\|");
                for (String keyword : keywords) {
                    if (item.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)) && !item.equalsIgnoreCase(keyword)) {
                        return true;
                    }
                }

                return false;
            },
            s -> s.contains("|") ? s : s.strip()
    );

    @Override
    public CreationData create(@Nullable SpreadLocationTask task) {
        CreationData data = CustomizableQuestElementSettings.super.create(task);
        data.put("spread", SpreadTaskSettings.SPREAD_SETTING, task == null ? "" : String.join("|", task.predicate().getSpreads()));
        return data;
    }


    @Override
    public SpreadLocationTask create(String id, SpreadLocationTask task, Data data) {
        return this.create(task, data, (title, icon) -> {
            String[] spreads = data.get("spread", SpreadTaskSettings.SPREAD_SETTING).orElse("").split("\\|");
            SpreadPredicate predicate;
            if (spreads.length == 0) {
                predicate = SpreadPredicate.ALWAYS_TRUE;
            } else {
                predicate = SpreadPredicate.create(Arrays.asList(spreads));
            }

            return new SpreadLocationTask(id, title, icon, predicate);
        });
    }

    // Mainly copied from parent class
    private record MyAutocompleteTextSetting(Supplier<List<String>> suggestions, BiPredicate<String, String> filter, Function<String, String> mapper)
            implements Setting<String, AutocompleteEditBox<String>> {
        @Override
        public AutocompleteEditBox<String> createWidget(int width, String value) {
            MyAutocompleteEditBox box = new MyAutocompleteEditBox(Minecraft.getInstance().font, 0, 0, width, 11, this.filter, this.mapper, s -> {});
            box.setSuggestions(this.suggestions.get());
            box.setValue(this.mapper.apply(value));
            box.setMaxLength(Short.MAX_VALUE);
            return box;
        }

        @Override
        public String getValue(AutocompleteEditBox<String> widget) {
            return widget.value();
        }
    }

    // Mainly copied from parent class
    private static class MyAutocompleteEditBox extends AutocompleteEditBox<String> {

        private final List<String> suggestions = new ArrayList<>();
        private final Function<String, String> mapper;

        public MyAutocompleteEditBox(Font font, int x, int y, int width, int height, BiPredicate<String, String> filter, Function<String, String> mapper, Consumer<String> onEnter) {
            super(font, x, y, width, height, filter, mapper, onEnter);
            this.mapper = mapper;
        }

        public void setSuggestions(Collection<String> suggestions) {
            super.setSuggestions(suggestions);
            this.suggestions.clear();
            this.suggestions.addAll(suggestions);
            this.filter();
        }

        @Override
        public String value() {
            String text = this.getValue();
            String[] parts = text.split("\\|");

            Set<String> toKeep = new HashSet<>();
            for (String part : parts) {
                for (String suggestion : this.suggestions) {
                    if (this.mapper.apply(suggestion).equals(part.strip())) {
                        toKeep.add(part);
                        break;
                    }
                }
            }

            if (toKeep.isEmpty()) {
                return null;
            }

            return String.join("|", toKeep);
        }

    }
}
