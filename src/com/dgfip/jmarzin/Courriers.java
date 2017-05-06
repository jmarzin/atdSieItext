package com.dgfip.jmarzin;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dgfip.jmarzin.AtdSieItext.jLabel;
import static com.dgfip.jmarzin.AtdSieItext.log;

class Courriers {


    private Map<String, Map<String, List<Page>>> copies = new HashMap<String, Map<String, List<Page>>>();       // String 1 : n° atd, String 2 : type courrier

    private Map<String, Map<String, List<Page>>> originaux = new HashMap<String, Map<String, List<Page>>>();    // String 1 : n° atd, String 2 : type courrier

    private String getNumero(String chaine) {
        String numeroAtd = "";
        Pattern pattern = Pattern.compile(".*\\nN° (?:ATD|de la notification) : (\\d+)\\n.*", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(chaine);
        if(matcher.matches()) {
            numeroAtd = matcher.group(1);
        }
        return numeroAtd;
    }

    private void ajout(Map<String, Map<String, List<Page>>> courriers, String numeroAtd, FichierPdf fichier, int ipage) {
        Map<String,List<Page>> entree = new HashMap<String, List<Page>>();
        List<Page> listePages = new ArrayList<Page>();
        if (courriers.get(numeroAtd) != null){
            entree = courriers.get(numeroAtd);
            if (entree.get(fichier.getTypeFichier()) != null) {
                if(fichier.getTypeFichier().equals("Atd") || fichier.getTypeFichier().equals("Bulletin_reponse_atd")) {
                    log("nouvelle page " + fichier.getTypeFichier() + " pour l'atd N° " + numeroAtd +
                        " ; elle ne sera pas imprimée");
                    return;
                }
              listePages = entree.get(fichier.getTypeFichier());
          }
        }
        listePages.add(new Page(fichier, ipage));
        entree.put(fichier.getTypeFichier(), listePages);
        courriers.put(numeroAtd, entree);
    }

    void verif() {
        for (String cle: copies.keySet()) {
            if(copies.get(cle).containsKey("Atd") && !copies.get(cle).containsKey("Bulletin_reponse_atd")) {
                log("L'atd N° " + cle + " n'a pas de bulletin réponse ; il sera envoyé malgré tout.");
            }
            if(copies.get(cle).containsKey("Bulletin_reponse_atd") && !copies.get(cle).containsKey("Atd")) {
                log("L'atd N% " + cle + " n'a qu'un bulletin réponse ; celui-ci ne sera pas envoyé.");
                copies.remove(cle);
            }
        }
    }

    private List<String> ecrit(String nom, Map<String, Map<String, List<Page>>> objets, List<String> listeFichiers, int MAX_PAGES,File repertoire, PdfReader verso, String dateHeure) throws IOException, DocumentException {
        int nbTotalPages = 0;
        int nbPagesTraitees = 0;
        for(Map<String, List<Page>> mapType: objets.values()){
            for(List<Page> pages: mapType.values()) {
                nbTotalPages += pages.size();
            }
        }
        Document doc = new Document();
        PdfSmartCopy copy = null;
        PdfImportedPage versoPdf = null;
        int partie = 0;
        String nomFichier = "";
        String[] typesDoc = {"Atd","Bulletin_reponse_atd","Notification_atd"};
        Object[] clesTriees = objets.keySet().toArray();
        Arrays.sort(clesTriees);
        for (Object cle : clesTriees) {
            if(copy == null || (MAX_PAGES > 0 && copy.getPageNumber() > MAX_PAGES)) {
                if(copy != null) copy.close();
                partie ++;
                if(partie == 1) {
                    nomFichier = repertoire.getCanonicalPath() + File.separatorChar + "atdSie__" + nom + "_"+ dateHeure + ".pdf";
                } else {
                    if(partie == 2) {
                        File fichier = new File(nomFichier);
                        nomFichier = repertoire.getCanonicalPath() + File.separatorChar + "atdSie__" + nom + "_partie_1_" + dateHeure + ".pdf";
                        fichier.renameTo(fichier);
                    }
                    nomFichier = repertoire.getCanonicalPath() + File.separatorChar + "atdSie__" + nom + "_partie_" +
                            partie + "_" + dateHeure + ".pdf";
                }
                listeFichiers.add(nomFichier);
                copy = new PdfSmartCopy(doc, new FileOutputStream(nomFichier));
                doc.open();
                if(nom.equals("copies"))
                    versoPdf = copy.getImportedPage(verso,1);
            }
            Map<String, List<Page>> courrier;
            courrier = objets.get(cle);
            for (String typedoc: typesDoc) {
                if(courrier.containsKey(typedoc))
                    for (Page page: courrier.get(typedoc)) {
                        PdfImportedPage pageOriginale = copy.getImportedPage(page.getLecteurPdf(), page.getIpage());
                        copy.addPage(pageOriginale);
                        nbPagesTraitees ++;
                        jLabel.setText(String.format("Pages traitées %s : %d/%d", nom, nbPagesTraitees, nbTotalPages));
                        if(typedoc.equals("Bulletin_reponse_atd") || versoPdf == null)
                            copy.addPage(PageSize.A4, 0);
                        else
                            copy.addPage(versoPdf);
                    }
            }
        }
        if(copy != null) doc.close();
        return listeFichiers;
    }

    List<String> ecritCopies(List<String> listeFichiers, int MAX_PAGES, File repertoire, PdfReader verso, String dateHeure) throws IOException, DocumentException {
        Map<String, Map<String, List<Page>>> objets = copies;
        listeFichiers = ecrit("copies", objets, listeFichiers, MAX_PAGES, repertoire, verso, dateHeure);
        return listeFichiers;
    }

    List<String> ecritOriginaux(List<String> listeFichiers, int MAX_PAGES, File repertoire, PdfReader verso, String dateHeure) throws IOException, DocumentException {
        Map<String, Map<String, List<Page>>> objets = originaux;
        listeFichiers = ecrit("originaux", objets, listeFichiers, MAX_PAGES, repertoire, verso, dateHeure);
        return listeFichiers;
    }

    void clicEsi(String nomFichier, int numeroMethode) throws IOException, DocumentException {
        if(numeroMethode == 1){
            String[] commande = new String[] {"C:\\Program Files\\LibreOffice 4\\program\\sdraw",
                    nomFichier,
                    "macro:///Standard.ClicEsi.ClicEsiPlus()"};
            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec(commande);
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            PdfReader lecteurPdf = new PdfReader(nomFichier);
            int nbTotalPages = lecteurPdf.getNumberOfPages();
            Rectangle rect1 = new Rectangle( 30, 842-92,170,842-44);
            Rectangle rect2 = new Rectangle(270f,662f,500f,742f);
            RegionTextRenderFilter filter1 = new RegionTextRenderFilter(rect1);
            RegionTextRenderFilter filter2 = new RegionTextRenderFilter(rect2);
            PdfStamper stamper = new PdfStamper(lecteurPdf, new FileOutputStream(nomFichier.replace(".pdf", "_ClicEsi.pdf")));
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            Font arial6 = new Font(bf, 6);
            Font arial8 = new Font(bf, 8);
            bf = BaseFont.createFont("C:\\Windows\\Fonts\\OCR-B10BT.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
            Font ocr10 = new Font(bf, 10);
            for (int i = 1; i <= lecteurPdf.getNumberOfPages(); i++) {
                jLabel.setText(String.format("Fichier %s, pages converties : %d/%d",
                        nomFichier.substring(nomFichier.lastIndexOf('\\') + 1), i,nbTotalPages));
                String texte = PdfTextExtractor.getTextFromPage(lecteurPdf, i);
                if((texte.contains("DIRECTION GENERALE DES FINANCES PUBLIQUES") &&
                        (texte.contains("N° 3738") || texte.contains("N° 3735")))) {
                    //récupérer l'adresse du SIE
                    FilteredTextRenderListener strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter1);
                    String[] texte1 = PdfTextExtractor.getTextFromPage(lecteurPdf, i, strategy).split("\n");
                    texte1[1] += " - recouvrement";
                    //récupérer l'adresse du destinataire
                    strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter2);
                    String[] texte2 = PdfTextExtractor.getTextFromPage(lecteurPdf, i, strategy).split("\n");
                    //effacer l'adresse destinataire
                    cleanUpLocations.add(new PdfCleanUpLocation(i,rect2));
                    PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations,stamper);
                    cleaner.cleanUp();
                    cleanUpLocations.clear();
                    //replacer l'adresse SIE
                    PdfContentByte canvas = stamper.getOverContent(i);
                    Float y = 730f;
                    for (String ligne: texte1) {
                        if(!ligne.startsWith("CS ")) {
                            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                    new Phrase(ligne, arial8),300f, y, 0);
                            y -= 10;
                        }
                    }
                    //replacer l'adresse destinataire
                    y = 650f;
                    for (String ligne: texte2) {
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                new Phrase(ligne, ocr10),300f, y, 0);
                        y -= 12;
                    }
                    //mettre les trois dièses
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                            new Phrase("###", arial6),28.4f, 28.76f, 0);
                }
            }
            stamper.close();
            lecteurPdf.close();
            File fichier = new File(nomFichier);
            fichier.delete();
        }
    }

    Courriers(RepertoireATraiter repertoireATraiter){
        int nbFichiers = repertoireATraiter.getFichiersPdf().size();
        int iFichiers = 1;
        for (FichierPdf fichier : repertoireATraiter.getFichiersPdf()) {
            jLabel.setText(String.format("Fichiers traités : %d/%d", iFichiers, nbFichiers));
            iFichiers ++;
            PdfReader lecteurPdf = fichier.getLecteurPdf();
            if (fichier.getTypeFichier().equals("Atd")) {
                for(int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage ++) {
                    String chaine = fichier.getChaine(ipage);
                    String numeroAtd = getNumero(chaine);
                    if(chaine.contains("N° 3735 Ampliation")) {
                        ajout(copies,numeroAtd,fichier,ipage);
                    } else if (chaine.contains("N° 3735 Original")) {
                        ajout(originaux, numeroAtd, fichier, ipage);
                    }
                }
            } else if (fichier.getTypeFichier().equals("Notification_atd")) {
                String numeroNotif = "";
                for(int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage ++) {
                    String chaine = fichier.getChaine(ipage);
                    String numeroN = getNumero(chaine);
                    if(!numeroN.isEmpty()) { numeroNotif = "N" + numeroN;}
                    if (chaine.contains("N° 3738 Original")) {
                        ajout(originaux, numeroNotif, fichier, ipage);
                    } else if (chaine.contains("N° 3738 Ampliation")) {
                        ajout(copies, numeroNotif, fichier, ipage);
                    }
                }
            } else if (fichier.getTypeFichier().equals("Bulletin_reponse_atd")) {
                for(int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage ++) {
                    String chaine = fichier.getChaine(ipage);
                    String numeroAtd = "";
                    if (chaine.contains("BULLETIN-REPONSE A L'AVIS A TIERS DETENTEUR")) {
                        numeroAtd = getNumero(chaine);
                    }
                    ajout(copies, numeroAtd, fichier, ipage);
                }
            }
        }
    }
}
