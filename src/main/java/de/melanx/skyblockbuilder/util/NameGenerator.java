package de.melanx.skyblockbuilder.util;

import java.util.Random;

public class NameGenerator {

    private static final String[] random1 = new String[]{
            "Del", "Mel", "Nep", "Yog", "Mal", "Fin", "Kat", "Cas", "Kal", "Ag", "Nik", "Tro", "Keg",
            "Mo", "Pro", "Par", "Sir", "Sah", "Kak", "Dea", "Sta", "Kaz", "Cly", "Ska", "Pal", "Tig"
    };

    private static final String[] random2 = new String[]{
            "ran", "ing", "tor", "aga", "hup", "bor", "tat", "ben", "fin", "sola", "salo", "sin", "ava",
            "mel", "craf", "snap", "rod", "app", "kala", "kelo", "zerom", "zoro", "tiri", "var", "pero", "melo"
    };

    private static final String[] random3 = new String[]{
            "cod", "ack", "oid", "lan", "ance", "por", "rec", "jus", "wol", "syn", "bot", "for", "kirim", "goa",
            "grimm", "law", "worga", "ridan", "weeze", "bas", "rev", "kurt", "make", "poke", "rore", "stri"
    };

    public static String randomName(Random rand) {
        return random1[rand.nextInt(random1.length)] + random2[rand.nextInt(random2.length)] + random3[rand.nextInt(random3.length)];
    }
}
