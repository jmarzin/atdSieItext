package com.dgfip.jmarzin;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

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

    private Map<String, Map<TypeCourrier, List<PageLue>>> copies = new HashMap<String, Map<TypeCourrier, List<PageLue>>>();       // String : n° atd

    private Map<String, Map<TypeCourrier, List<PageLue>>> originaux = new HashMap<String, Map<TypeCourrier, List<PageLue>>>();    // String : n° atd

    private String getNumero(String chaine) {
        String numeroAtd = "";
        Pattern pattern = Pattern.compile(".*\\nN° (?:ATD|de la notification) : (\\d+)\\n.*", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(chaine);
        if(matcher.matches()) {
            numeroAtd = matcher.group(1);
        }
        return numeroAtd;
    }

    private void ajout(Map<String, Map<TypeCourrier, List<PageLue>>> courriers, String numeroAtd, FichierPdf fichier, int ipage) {
        Map<TypeCourrier,List<PageLue>> entree = new HashMap<TypeCourrier, List<PageLue>>();
        List<PageLue> listePageLues = new ArrayList<PageLue>();
        if (courriers.get(numeroAtd) != null){
            entree = courriers.get(numeroAtd);
            if (entree.get(fichier.getTypeFichier()) != null) {
                if(fichier.getTypeFichier().equals(TypeCourrier.SIE_ATD) ||
                        fichier.getTypeFichier().equals(TypeCourrier.SIE_ATD_BULLETIN_REPONSE)) {
                    log("nouvelle page " + fichier.getTypeFichier() + " pour l'atd N° " + numeroAtd +
                        " ; elle ne sera pas imprimée");
                    return;
                }
              listePageLues = entree.get(fichier.getTypeFichier());
          }
        }
        listePageLues.add(new PageLue(fichier, ipage));
        entree.put(fichier.getTypeFichier(), listePageLues);
        courriers.put(numeroAtd, entree);
    }

    void verif() {
        for (String cle: copies.keySet()) {
            if(copies.get(cle).containsKey(TypeCourrier.SIE_ATD) &&
                    !copies.get(cle).containsKey(TypeCourrier.SIE_ATD_BULLETIN_REPONSE)) {
                log("L'atd N° " + cle + " n'a pas de bulletin réponse ; il sera envoyé malgré tout.");
            }
            if(copies.get(cle).containsKey(TypeCourrier.SIE_ATD_BULLETIN_REPONSE) &&
                    !copies.get(cle).containsKey(TypeCourrier.SIE_ATD)) {
                log("L'atd N% " + cle + " n'a qu'un bulletin réponse ; celui-ci ne sera pas envoyé.");
                copies.remove(cle);
            }
        }
    }

    private Map<String,List<PageAModifier>> ecrit(String nom, Map<String, Map<TypeCourrier, List<PageLue>>> objets,
                                                  Map<String,List<PageAModifier>> listeFichiers,
                                                  int MAX_PAGES,File repertoire,
                                                  PdfReader verso,
                                                  String dateHeure) throws IOException, DocumentException {
        int nbTotalPages = 0;
        int nbPagesTraitees = 0;
        for(Map<TypeCourrier, List<PageLue>> mapType: objets.values()){
            for(List<PageLue> pageLues : mapType.values()) {
                nbTotalPages += pageLues.size();
            }
        }
        Document doc = new Document();
        PdfSmartCopy copy = null;
        PdfImportedPage versoPdf = null;
        int partie = 0;
        String nomFichier = "";
        TypeCourrier[] typesDoc = {TypeCourrier.SIE_ATD,
                TypeCourrier.SIE_ATD_BULLETIN_REPONSE,
                TypeCourrier.SIE_ATD_NOTIFICATION};
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
                        File fichierO = new File(nomFichier);
                        String nomFichierD = repertoire.getCanonicalPath() + File.separatorChar + "atdSie__" + nom + "_partie_1_" + dateHeure + ".pdf";
                        File fichierD = new File(nomFichierD);
                        if(!fichierO.renameTo(fichierD)) log(String.format("Rename de %s impossible", fichierO.getName()));
                        listeFichiers.put(nomFichierD, listeFichiers.get(nomFichier));
                        listeFichiers.remove(nomFichier);
                    }
                    nomFichier = repertoire.getCanonicalPath() + File.separatorChar + "atdSie__" + nom + "_partie_" +
                            partie + "_" + dateHeure + ".pdf";
                }
                listeFichiers.put(nomFichier,new ArrayList<PageAModifier>());
                copy = new PdfSmartCopy(doc, new FileOutputStream(nomFichier));
                doc.open();
                if(nom.equals("copies"))
                    versoPdf = copy.getImportedPage(verso,1);
            }
            Map<TypeCourrier, List<PageLue>> courrier;
            courrier = objets.get(cle);
            for (TypeCourrier typedoc: typesDoc) {
                if(courrier.containsKey(typedoc)) {
                    List<PageLue> pageLues = courrier.get(typedoc);
                    for (int i = 0; i < pageLues.size(); i++) { //PageLue pageLue: courrier.get(typedoc)) {
                        PageLue pageLue = pageLues.get(i);
                        PdfImportedPage pageOriginale = copy.getImportedPage(pageLue.getLecteurPdf(), pageLue.getIpage());
                        copy.addPage(pageOriginale);
                        if(i == 0 && typedoc != TypeCourrier.SIE_ATD_BULLETIN_REPONSE)
                            listeFichiers.get(nomFichier).add(new PageAModifier(copy.getPageNumber() - 1,typedoc));
                        nbPagesTraitees++;
                        jLabel.setText(String.format("Pages traitées %s : %d/%d", nom, nbPagesTraitees, nbTotalPages));
                        if (typedoc == TypeCourrier.SIE_ATD_BULLETIN_REPONSE || versoPdf == null)
                            copy.addPage(PageSize.A4, 0);
                        else
                            copy.addPage(versoPdf);
                    }
                }
            }
        }
        if(copy != null) doc.close();
        return listeFichiers;
    }

    Map<String,List<PageAModifier>> ecritCopies(Map<String,List<PageAModifier>> listeFichiers,
                                          int MAX_PAGES,
                                          File repertoire,
                                          PdfReader verso,
                                          String dateHeure) throws IOException, DocumentException {
        Map<String, Map<TypeCourrier, List<PageLue>>> objets = copies;
        listeFichiers = ecrit("copies", objets, listeFichiers, MAX_PAGES, repertoire, verso, dateHeure);
        return listeFichiers;
    }

    Map<String,List<PageAModifier>> ecritOriginaux(Map<String, List<PageAModifier>> listeFichiers,
                                                   int MAX_PAGES,
                                                   File repertoire,
                                                   PdfReader verso,
                                                   String dateHeure) throws IOException, DocumentException {
        Map<String, Map<TypeCourrier, List<PageLue>>> objets = originaux;
        listeFichiers = ecrit("originaux", objets, listeFichiers, MAX_PAGES, repertoire, verso, dateHeure);
        return listeFichiers;
    }

    void clicEsi(String nomFichier, int numeroMethode, List<PageAModifier> pages) throws IOException, DocumentException {
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
            Clicesiplus clic = new Clicesiplus(nomFichier);
            for (PageAModifier page : pages) {
                int i = page.getIpage();
                jLabel.setText(String.format("Fichier %s, pages converties : %d/%d",
                        nomFichier.substring(nomFichier.lastIndexOf('\\') + 1), i,clic.getNbTotalPages()));
                    //récupérer l'adresse du SIE
                    String[] texte1 = clic.getAdresse("Exp", page);
                    texte1[1] += " - recouvrement";
                    //récupérer l'adresse du destinataire
                    String[] texte2 = clic.getAdresse("Dest", page);
                    //effacer l'adresse destinataire
                    clic.deleteAdresse("Dest",page);
                    //replacer l'adresse SIE
                    clic.replaceAdresse("Exp", texte1, i);
                    //replacer l'adresse destinataire
                    clic.replaceAdresse("Dest",texte2, i);
                    //mettre les trois dièses
                    clic.diese(i);
            }
            clic.close();
            File fichier = new File(nomFichier);
            if(!fichier.delete()) log(String.format("Suppression de %s impossible", fichier.getName()));
        }
    }

    Courriers(RepertoireATraiter repertoireATraiter){
        int nbFichiers = repertoireATraiter.getFichiersPdf().size();
        int iFichiers = 1;
        for (FichierPdf fichier : repertoireATraiter.getFichiersPdf()) {
            jLabel.setText(String.format("Fichiers traités : %d/%d", iFichiers, nbFichiers));
            iFichiers++;
            PdfReader lecteurPdf = fichier.getLecteurPdf();
            TypeCourrier typeCourrier = fichier.getTypeFichier();
            if (typeCourrier != null) {
                if (typeCourrier == TypeCourrier.SIE_ATD) {
                    for (int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage++) {
                        String chaine = fichier.getChaine(ipage);
                        String numeroAtd = getNumero(chaine);
                        if (chaine.contains("N° 3735 Ampliation")) {
                            ajout(copies, numeroAtd, fichier, ipage);
                        } else if (chaine.contains("N° 3735 Original")) {
                            ajout(originaux, numeroAtd, fichier, ipage);
                        }
                    }
                } else if (typeCourrier == TypeCourrier.SIE_ATD_NOTIFICATION) {
                    String numeroNotif = "";
                    for (int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage++) {
                        String chaine = fichier.getChaine(ipage);
                        String numeroN = getNumero(chaine);
                        if (!numeroN.isEmpty()) {
                            numeroNotif = "N" + numeroN;
                        }
                        if (chaine.contains("N° 3738 Original")) {
                            ajout(originaux, numeroNotif, fichier, ipage);
                        } else if (chaine.contains("N° 3738 Ampliation")) {
                            ajout(copies, numeroNotif, fichier, ipage);
                        }
                    }
                } else if (typeCourrier == TypeCourrier.SIE_ATD_BULLETIN_REPONSE) {
                    for (int ipage = 1; ipage <= lecteurPdf.getNumberOfPages(); ipage++) {
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
}
