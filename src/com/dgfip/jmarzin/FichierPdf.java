package com.dgfip.jmarzin;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;

class FichierPdf {

    private boolean atdSie = false;
    boolean isAtdSie() {
        return atdSie;
    }

    private PdfReader lecteurPdf;
    PdfReader getLecteurPdf() { return lecteurPdf;}

    private TypeCourrier typeFichier;
    TypeCourrier getTypeFichier() { return typeFichier;}

    String getChaine(int page) {
        String chaine = "";
        try {
            chaine = PdfTextExtractor.getTextFromPage(lecteurPdf, page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chaine;
    }

    FichierPdf(File fichier) {
        try {
            this.lecteurPdf = new PdfReader(fichier.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String chaine = this.getChaine(1);
        for(TypeCourrier typeCourrier : TypeCourrier.values()) {
            if(chaine.contains(typeCourrier.chaineType())) {
                this.typeFichier = typeCourrier;
                break;
            }
        }
        if(typeFichier == TypeCourrier.SIE_ATD ||
                typeFichier == TypeCourrier.SIE_ATD_BULLETIN_REPONSE ||
                typeFichier == TypeCourrier.SIE_ATD_NOTIFICATION) {
            this.atdSie = true;
        }
    }
}
