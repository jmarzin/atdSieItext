package com.dgfip.jmarzin;

import com.itextpdf.text.pdf.PdfReader;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dgfip.jmarzin.AtdSieItext.jLabel;

class RepertoireATraiter {

    private boolean atdSie = false;
    boolean isAtdSie() { return atdSie;}

    private File repertoire;
    File getRepertoire() { return repertoire;}

    private List<FichierPdf> fichiersPdf = new ArrayList<FichierPdf>();
    List<FichierPdf> getFichiersPdf() { return fichiersPdf;}

    private List<String> fichiersADeplacer = new ArrayList<String>();
    List<String> getFichiersADeplacer() { return fichiersADeplacer;}

    private PdfReader verso = null;
    PdfReader getVerso() { return verso;}

    RepertoireATraiter(JFileChooser fc, String exclus) {
        this.repertoire = fc.getSelectedFile().getAbsoluteFile();
        File[] listeFichiers = fc.getSelectedFile().listFiles(new OnlyFile("pdf"));
        int nbFichiers = 0;
        if(listeFichiers != null) nbFichiers = listeFichiers.length;
        for(int i = 0; i < nbFichiers; i++) {
            jLabel.setText((i+1) + " fichier(s) traité()s");
            if(!listeFichiers[i].getName().startsWith(exclus)) {
                FichierPdf fic = new FichierPdf(listeFichiers[i]);
                this.fichiersPdf.add(fic);
                if(fic.isAtdSie()) atdSie = true;
                TypeCourrier typeFichier = fic.getTypeFichier();
                if (typeFichier == TypeCourrier.SIE_ATD_VERSO) { //;"Verso") {
                    this.verso = fic.getLecteurPdf();
                } else {
                    this.fichiersADeplacer.add(listeFichiers[i].getName());
                }
            } else {
                this.fichiersADeplacer.add(listeFichiers[i].getName());
            }
        }
    }
}
