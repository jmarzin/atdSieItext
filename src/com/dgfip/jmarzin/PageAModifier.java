package com.dgfip.jmarzin;

/**
 * Created by jmarzin-cp on 08/05/2017.
 */
class PageAModifier {
    int getIpage() {
        return ipage;
    }

    private int ipage;

    TypeCourrier getTypeCourrier() {
        return typeCourrier;
    }

    private TypeCourrier typeCourrier;
    PageAModifier (int ipage, TypeCourrier typeCourrier) {
        this.ipage = ipage;
        this.typeCourrier = typeCourrier;
    }
}

