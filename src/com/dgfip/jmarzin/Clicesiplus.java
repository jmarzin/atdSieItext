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
    private Rectangle rectDest;
    private RegionTextRenderFilter filterExp;
    private RegionTextRenderFilter filterDest;
    private List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
    private PdfStamper stamper;
    private static final char[][] accents = {{'à','a'},{'é','e'},{'è','e'},{'ê','e'},{'ë','e'},
            {'ï','i'},{'ô','o'},{'ù','u'},{'.',' '}};

    Clicesiplus(Rectangle rectExp, Rectangle rectDest, String nomFichier, PdfReader lecteurPdf) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
        this.arial6 = new Font(bf, 6);
        this.arial8 = new Font(bf,8);
        bf = BaseFont.createFont("C:\\Windows\\Fonts\\OCR-B10BT.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
        this.ocr10 = new Font(bf, 10);
        this.rectDest = rectDest;
        this.filterExp = new RegionTextRenderFilter(rectExp);
        this.filterDest = new RegionTextRenderFilter(rectDest);
        this.stamper = new PdfStamper(lecteurPdf, new FileOutputStream(nomFichier.replaceAll(".pdf$", "_ClicEsi.pdf")));
    }

    String[] getAdresseExp(PdfReader lecteurPdf, int ipage) throws IOException {
        FilteredTextRenderListener strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filterExp);
        return PdfTextExtractor.getTextFromPage(lecteurPdf, ipage, strategy).split("\n");
    }
    String[] getAdresseDest(PdfReader lecteurPdf, int ipage) throws IOException {
        FilteredTextRenderListener strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filterDest);
        return PdfTextExtractor.getTextFromPage(lecteurPdf, ipage, strategy).split("\n");
    }
    void deleteAdresseDest(int ipage) throws IOException, DocumentException {
        cleanUpLocations.add(new PdfCleanUpLocation(ipage,rectDest));
        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations,stamper);
        cleaner.cleanUp();
        cleanUpLocations.clear();
    }

    private void replaceAdresse(String[] texte, int ipage, Float y, Font fonte, Float espace) {
        PdfContentByte canvas = stamper.getOverContent(ipage);
        for (String ligne: texte) {
            if(!ligne.startsWith("CS ")) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase(ligne, fonte),300f, y, 0);
                y -= espace;
            }
        }
    }
    void replaceAdresseExp(String[] texteExp, int ipage) {
        replaceAdresse(texteExp, ipage, 730f, arial8, 10f);

    }
    void replaceAdresseDest(String[] texteDest, int ipage) {
        replaceAdresse(enleveAccent(texteDest), ipage, 650f, ocr10, 12f);
    }
    void diese(int ipage) {
        PdfContentByte canvas = stamper.getOverContent(ipage);
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase("###", arial6),28.4f, 28.76f, 0);
    }
    void close() throws IOException, DocumentException {
        stamper.close();
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
