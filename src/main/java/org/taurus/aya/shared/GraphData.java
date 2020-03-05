package org.taurus.aya.shared;

public class GraphData implements com.google.gwt.user.client.rpc.IsSerializable {
    Long[] seriesLocal;
    Long[] seriesGroup;
    String[] captions;
    private String statistics;

    public GraphData(){
        seriesLocal= new Long[0];
        seriesGroup = new Long[0];
        captions = new String[0];
    }
    
    public GraphData(Long[] seriesLocal, Long[] seriesGroup, String[] captions, String statistics) {
        this.seriesLocal = seriesLocal;
        this.seriesGroup = seriesGroup;
        this.captions = captions;
        this.statistics = statistics;
    }

    public Long[] getSeriesLocal() {
        return seriesLocal;
    }

    public Long[] getSeriesGroup() {
        return seriesGroup;
    }

    public String[] getCaptions() {
        return captions;
    }

    public String getStatistics() { return statistics;}
}
