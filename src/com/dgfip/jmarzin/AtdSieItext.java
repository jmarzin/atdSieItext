package com.dgfip.jmarzin;


import com.itextpdf.text.DocumentException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AtdSieItext {

    //limite de pages
    private static final int MAX_PAGES = 0;
    public static JLabel jLabel = new JLabel("Demande du répertoire"); // champ d'affichage des étapes
    public static JTextArea display = new JTextArea(16, 60);  // champ d'affichage de la log

    public static void log (String texte) {
        System.out.println(texte);
        display.setText(display.getText()+texte+"\n");
    }

    public static void main(String[] args) throws IOException {

        //fenêtre de suivi
        JFrame fenetre = new JFrame();
        fenetre.setTitle("Traitement des courriers d'ATD");
        fenetre.setSize(700, 350);
        fenetre.setLocationRelativeTo(null);
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setVisible(true);
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan,BoxLayout.PAGE_AXIS));
        fenetre.setContentPane(pan);
        pan.setVisible(true);
        jLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pan.add(jLabel);
        display.setEditable(false); // set textArea non-editable
        display.setVisible(true);
        JScrollPane scroll = new JScrollPane(display);
        display.setAlignmentX(Component.CENTER_ALIGNMENT);
        pan.add(scroll);

        //Date-heure
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat formatDateHeure = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss-SSS");
        String dateHeure = formatDateHeure.format(now);

        //Choix du répertoire
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(pan);
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }

        //Identification des fichiers PDF
        RepertoireATraiter repATraiter = new RepertoireATraiter(fc,"atdSie__");

        //Vérification de la présence de verso.pdf
        if(repATraiter.isAtdSie() && repATraiter.getVerso() == null) {
            JOptionPane.showMessageDialog(null,
                    "Le fichier verso.pdf est absent.",
                    "Erreur",
                     JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        //Transformation clic'esi ou pas
        Object[] options = {"Aucune", "ClicEsiPlus"};
        int n = JOptionPane.showOptionDialog(pan,
                "Choisissez la transformation à appliquer",
                "Transformation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title
        boolean clicEsi = (n == 1);

        //ouvrir le fichier de log
        String fichierLog = repATraiter.getRepertoire().getAbsolutePath() +
                File.separator + "atdSie__CR_" + dateHeure + ".txt";
        try {
            PrintStream stream = new PrintStream(fichierLog);
            System.setOut(stream);
            System.setErr(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Construction des courriers
        Courriers courriers = new Courriers(repATraiter);

        //Vérification des courriers
        courriers.verif();

        //Ecriture du fichier des copies
        List<String> listeFichiers = new ArrayList<String>();
        try {
            listeFichiers = courriers.ecritCopies(listeFichiers, MAX_PAGES,repATraiter.getRepertoire(),repATraiter.getVerso() ,dateHeure);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        //Ecriture du fichier des originaux
        try {
            listeFichiers = courriers.ecritOriginaux(listeFichiers, MAX_PAGES,repATraiter.getRepertoire(),null ,dateHeure);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        //fermeture des fichiers
        for (FichierPdf fichier : repATraiter.getFichiersPdf()) {
            fichier.getLecteurPdf().close();
        }

        //déplacement des fichiers
        jLabel.setText("Déplacement des fichiers");
        AtomicReference<File> repTraites = new AtomicReference<File>();
        try {
            repTraites.set(new File(repATraiter.getRepertoire().getCanonicalPath() +
                    File.separatorChar + "dejaTraites"));
            if(!repTraites.get().exists() || repTraites.get().isFile()) {
                repTraites.get().mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String fichier : repATraiter.getFichiersADeplacer()) {
            String nomOr = repATraiter.getRepertoire().getAbsolutePath() + File.separator + fichier;
            String nomDest = repATraiter.getRepertoire().getAbsolutePath() + File.separator +
                    "dejaTraites" + File.separator + fichier;
            new File(nomOr).renameTo(new File(nomDest));
        }

        //Appel de clic'esi plus
        if(clicEsi){
            for (String nomFichier: listeFichiers) {
                try {
                    courriers.clicEsi(nomFichier, 2);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
        log("Fin du traitement");
        jLabel.setText("Traitement terminé, consultez le compte-rendu ci-dessous.");
    }
}
