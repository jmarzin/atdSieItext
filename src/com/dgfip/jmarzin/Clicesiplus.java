package com.dgfip.jmarzin;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Clicesiplus {
    private Font arial6;
    private Font arial8;
    private Font ocr10;
    private List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
    private PdfStamper stamper;
    private PdfReader lecteurPdf;

    private int nbTotalPages;
    int getNbTotalPages() {
        return nbTotalPages;
    }

    private static final char[][] accents = {{'à','a'},{'é','e'},{'è','e'},{'ê','e'},{'ë','e'},
            {'ï','i'},{'ô','o'},{'ù','u'},{'.',' '}};

    Clicesiplus(String nomFichier) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
        this.arial6 = new Font(bf, 6);
        this.arial8 = new Font(bf,8);
        bf = BaseFont.createFont("C:\\Windows\\Fonts\\OCR-B10BT.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
        this.ocr10 = new Font(bf, 10);
        this.lecteurPdf = new PdfReader(nomFichier);
        this.nbTotalPages = lecteurPdf.getNumberOfPages();
        this.stamper = new PdfStamper(lecteurPdf, new FileOutputStream(nomFichier.replaceAll(".pdf$", "_ClicEsi.pdf")));
    }

    String[] getAdresse(String adresse, PageAModifier page) throws IOException {
        Rectangle rect = (adresse == "Exp") ? page.getTypeCourrier().rectExp() : page.getTypeCourrier().rectDest();
        RegionTextRenderFilter filter = new RegionTextRenderFilter(rect);
        FilteredTextRenderListener strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
        return PdfTextExtractor.getTextFromPage(lecteurPdf, page.getIpage(), strategy).split("\n");
    }

    void deleteAdresse(String adresse, PageAModifier page) throws IOException, DocumentException {
        Rectangle rect = (adresse.equals("Exp")) ? page.getTypeCourrier().rectExp() : page.getTypeCourrier().rectDest();
        cleanUpLocations.add(new PdfCleanUpLocation(page.getIpage(),rect));
        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations,stamper);
        cleaner.cleanUp();
        cleanUpLocations.clear();
    }

    void replaceAdresse(String adresse, String[] texte, int ipage) {
        Float y;
        Float espace;
        Font fonte;

        if(adresse.equals("Exp")) {
            fonte = arial8;
            y = 730f;
            espace = 10f;
        } else {
            fonte = ocr10;
            y = 650f;
            espace = 12f;
        }
        PdfContentByte canvas = stamper.getOverContent(ipage);
        for (String ligne: texte) {
            if(!ligne.startsWith("CS ")) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase(ligne, fonte),300f, y, 0);
                y -= espace;
            }
        }
    }

    void diese(int ipage) {
        PdfContentByte canvas = stamper.getOverContent(ipage);
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase("###", arial6),28.4f, 28.76f, 0);
    }
    void close() throws IOException, DocumentException {
        stamper.close();
        lecteurPdf.close();
    }
    private String[] enleveAccent(String[] texte) {
        for(int i = 0; i< texte.length; i++) {
            if(texte[i].contains("é")){
                texte[i] = texte[i];
            }
            for(char[] lettre: accents){
                texte[i] = texte[i].replace(lettre[0],lettre[1]);
            }
        }
        return texte;
    }
}
