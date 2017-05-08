package com.dgfip.jmarzin;

import com.itextpdf.text.pdf.PdfReader;

class PageLue {

    private PdfReader lecteurPdf;
    PdfReader getLecteurPdf() {
        return lecteurPdf;
    }

    private int ipage;
    int getIpage() {
        return ipage;
    }

    PageLue(FichierPdf fichierPdf, int ipage) {
        this.lecteurPdf = fichierPdf.getLecteurPdf();
        this.ipage = ipage;
    }
}
