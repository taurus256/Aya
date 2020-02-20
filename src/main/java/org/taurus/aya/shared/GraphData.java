package org.taurus.aya.shared;

public class GraphData implements com.google.gwt.user.client.rpc.IsSerializable {
    Long[] seriesLocal;
    Long[] seriesGroup;
    String[] captions;

    public GraphData(){
        seriesLocal= new Long[0];
        seriesGroup = new Long[0];
        captions = new String[0];
    }
    
    public GraphData(Long[] seriesLocal, Long[] seriesGroup, String[] captions) {
        this.seriesLocal = seriesLocal;
        this.seriesGroup = seriesGroup;
        this.captions = captions;
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
}
