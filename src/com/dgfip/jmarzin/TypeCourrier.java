package com.dgfip.jmarzin;

import com.itextpdf.text.Rectangle;

public enum TypeCourrier {
    SIE_ATD(new Rectangle( 30, 842-92,170,842-44),
            false,
            new Rectangle(270f,662f,500f,742f),
            true,
            "N° 3735 Ampliation",
            ".*\\nN° (?:ATD) : (\\d+)\\n.*",
            "AtdSieSiret"),
    SIE_ATD_NOTIFICATION(new Rectangle( 30, 842-92,170,842-44),
            false,
            new Rectangle(270f,662f,500f,742f),
            true,
            "N° 3738 Original",
            ".*\\nN° (?:de la notification) : (\\d+)\\n.*",
            "AtdSieNotif"),
    SIE_ATD_VERSO(null,
            false,
            null,
            false,
            "ATD-MIRIAM-SP",
            null,
            null),
    SIE_ATD_BULLETIN_REPONSE(null,
            false,
            null,
            false,
            "BULLETIN-REPONSE A L'AVIS A TIERS DETENTEUR",
            ".*\\nN° (?:ATD) : (\\d+)\\n.*",
            "AtdSieSiret");

    private final Rectangle rectExp;
    private final boolean deleteExp;
    private final Rectangle rectDest;
    private final boolean deleteDest;
    private final String chaineType;
    private final String regexpCle;
    private final String prefixeCle;

    TypeCourrier(Rectangle rectExp, boolean deleteExp, Rectangle rectDest, boolean deleteDest, String chaineType, String regexpCle, String prefixeCle) {
        this.rectExp = rectExp;
        this.deleteExp = deleteExp;
        this.rectDest = rectDest;
        this.deleteDest = deleteDest;
        this.chaineType = chaineType;
        this.regexpCle = regexpCle;
        this.prefixeCle = prefixeCle;
    }
    public final Rectangle rectExp() { return rectExp;}
    public final boolean deleteExp() { return deleteExp;}
    public final Rectangle rectDest() { return rectDest;}
    public final boolean deleteDest() { return deleteDest;}
    public final String chaineType() { return chaineType;}
    public final String RegexpCle() { return regexpCle;}
    public final String prefixeCle() { return prefixeCle;}
}
