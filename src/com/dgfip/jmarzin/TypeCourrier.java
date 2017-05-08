package com.dgfip.jmarzin;

import com.itextpdf.text.Rectangle;

public enum TypeCourrier {
    SIE_ATD(new Rectangle( 30, 842-92,170,842-44),
            new Rectangle(270f,662f,500f,742f)),
    SIE_ATD_NOTIFICATION(new Rectangle( 30, 842-92,170,842-44),
            new Rectangle(270f,662f,500f,742f)),
    SIE_ATD_VERSO(null, null),
    SIE_ATD_BULLETIN_REPONSE(null, null);

    private final Rectangle rectExp;
    private final Rectangle rectDest;

    TypeCourrier(Rectangle rectExp, Rectangle rectDest) {
        this.rectExp = rectExp;
        this.rectDest = rectDest;
    }
    public final Rectangle rectExp() { return rectExp;}
    public final Rectangle rectDest() { return rectDest;}
}
