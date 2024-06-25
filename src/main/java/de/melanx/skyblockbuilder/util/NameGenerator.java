package de.melanx.skyblockbuilder.util;

import java.util.Random;

public class NameGenerator {

    private static final String[] random1 = new String[]{
            "Lum", "Zar", "Thal", "Vex", "Ori", "Sol", "Drak", "Aer", "Gor", "Xan", "Yar", "Bel", "Quin",
            "Nym", "Ryn", "Fay", "Zyn", "Kor", "Lir", "Tyn", "Jor", "Vyr", "Gal", "Eld", "Myr", "Ven", "Kai",
            "Zul", "Mir", "Thar", "Tel", "Gar", "Hal", "Kor", "Fin", "Dar", "Nik", "Tar", "Pol", "Gre", "Ket",
            "Ard", "Sar", "Mer", "Lun", "Fel", "Vor", "Xyl", "Jal", "Pyr", "Tav", "Nor", "Kyn", "Des", "Rol",
            "Vin", "Wyn", "Mal", "Sor", "Tyr", "Dan"
    };

    private static final String[] random2 = new String[]{
            "vyn", "dar", "lix", "nor", "rah", "syl", "ron", "fyr", "mir", "zan", "tor", "lyr", "dor", "ven",
            "kal", "ryn", "zor", "vak", "nir", "fin", "sal", "tir", "rek", "vas", "mir", "zon", "kil", "mar",
            "las", "nim", "dur", "lar", "ral", "vos", "kin", "tar", "ris", "vin", "dal", "kor", "tel", "zan",
            "mel", "sor", "val", "ran", "tin", "mor", "lyn", "ver", "ras", "nol", "vak", "jir", "tol", "ran",
            "yor", "tas", "vim", "wer", "kyl", "zin"
    };

    private static final String[] random3 = new String[]{
            "dus", "ren", "lyn", "thos", "mir", "van", "thar", "los", "ris", "das", "wor", "lin", "far", "mel",
            "jor", "vin", "sol", "gar", "tur", "lis", "von", "zar", "ker", "wyn", "rod", "jax", "tir", "val",
            "rex", "nor", "xis", "jar", "dan", "rul", "tar", "kon", "gar", "lyn", "dor", "mak", "fen", "sol",
            "rav", "kol", "tak", "zar", "lek", "mon", "zil", "vas", "tum", "vor", "nis", "wen", "tar", "jul",
            "gor", "lin", "kos", "zar", "ron", "mek"
    };

    public static String randomName(Random rand) {
        return random1[rand.nextInt(random1.length)] + random2[rand.nextInt(random2.length)] + random3[rand.nextInt(random3.length)];
    }
}
