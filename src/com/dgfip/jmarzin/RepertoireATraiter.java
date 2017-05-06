package com.dgfip.jmarzin;

import com.itextpdf.text.pdf.PdfReader;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dgfip.jmarzin.AtdSieItext.jLabel;


/**
 * Created by jacquesmarzin on 30/04/2017.
 */
public class RepertoireATraiter {

    private boolean atdSie = false;
    public boolean isAtdSie() { return atdSie;}

    private File repertoire;
    public File getRepertoire() { return repertoire;}

    private List<FichierPdf> fichiersPdf;
    public List<FichierPdf> getFichiersPdf() { return fichiersPdf;}

    private List<String> fichiersADeplacer;
    public List<String> getFichiersADeplacer() { return fichiersADeplacer;}

    private PdfReader verso = null;
    public PdfReader getVerso() { return verso;}

    RepertoireATraiter(JFileChooser fc, String exclus) {
        this.repertoire = fc.getSelectedFile().getAbsoluteFile();
        File[] listeFichiers = fc.getSelectedFile().listFiles(new OnlyFile("pdf"));
        int i;
        this.fichiersPdf = new ArrayList<FichierPdf>();
        this.fichiersADeplacer = new ArrayList<String>();
        for(i = 0; i < listeFichiers.length; i++) {
            jLabel.setText(i + " fichier(s) traité()s");
            FichierPdf fic = null;
            if(!listeFichiers[i].getName().startsWith(exclus)) {
                fic = new FichierPdf(listeFichiers[i]);
                this.fichiersPdf.add(fic);
                if(fic.isAtdSie()) atdSie = true;
                String typeFichier = fic.getTypeFichier();
                if (typeFichier == "Verso") {
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
