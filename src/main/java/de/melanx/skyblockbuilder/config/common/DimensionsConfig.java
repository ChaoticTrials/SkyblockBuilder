package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.Optional;

@RegisterConfig("dimensions")
public class DimensionsConfig {

    public static class Overworld {

        @Config("Should overworld generate as in default world type? [default: false]")
        public static boolean Default = false;
    }

    public static class Nether {

        @Config("Should nether generate as in default world type? [default: false]")
        public static boolean Default = false;

        @Config("File name in template directory of a valid template containing a nether portal")
        public static Optional<String> netherPortalStructure = Optional.empty();
    }

    public static class End {

        @Config("Should end generate as in default world type? [default: false]")
        public static boolean Default = false;

        @Config("Should the main island be generated as normal? [default: true]")
        public static boolean mainIsland = true;
    }
}
