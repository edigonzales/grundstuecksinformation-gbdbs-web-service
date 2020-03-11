package ch.so.agi.gbdbs.webservice;

public class SimpleGrundstueck {
    private String egrid;
    private String gemeinde;
    private String kanton;
    private String grundbuch;
    private String grundbuchamt;
    private int flaechenmass;
    
    public String getEgrid() {
        return egrid;
    }
    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }
    public String getGemeinde() {
        return gemeinde;
    }
    public void setGemeinde(String gemeinde) {
        this.gemeinde = gemeinde;
    }
    public String getKanton() {
        return kanton;
    }
    public void setKanton(String kanton) {
        this.kanton = kanton;
    }
    public String getGrundbuch() {
        return grundbuch;
    }
    public void setGrundbuch(String grundbuch) {
        this.grundbuch = grundbuch;
    }
    public String getGrundbuchamt() {
        return grundbuchamt;
    }
    public void setGrundbuchamt(String grundbuchamt) {
        this.grundbuchamt = grundbuchamt;
    }
    public int getFlaechenmass() {
        return flaechenmass;
    }
    public void setFlaechenmass(int flaechenmass) {
        this.flaechenmass = flaechenmass;
    }
}
