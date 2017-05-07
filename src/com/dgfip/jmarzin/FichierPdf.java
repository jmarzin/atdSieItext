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
        if (chaine.contains("BULLETIN-REPONSE A L'AVIS A TIERS DETENTEUR")) {
            this.typeFichier = TypeCourrier.SIE_ATD_BULLETIN_REPONSE; //"Bulletin_reponse_atd";
            this.atdSie = true;
        }
        else if (chaine.contains("N° 3735 Original")) {
            this.typeFichier = TypeCourrier.SIE_ATD;//"Atd";
            this.atdSie = true;
        }
        else if (chaine.contains("N° 3738 Original")){
            this.typeFichier = TypeCourrier.SIE_ATD_NOTIFICATION;//"Notification_atd";
            this.atdSie = true;
        }
        else if (chaine.contains("ATD-MIRIAM-SP")) {
            this.typeFichier = TypeCourrier.SIE_ATD_VERSO;//"Verso";
            this.atdSie = true;
        } else {
            this.typeFichier = null;
            this.atdSie = false;
        }
    }
}
