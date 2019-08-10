package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class TaskAnalyseData implements IsSerializable {

    String panelText;

    List<Advice> advices;

    public TaskAnalyseData(){}

    public TaskAnalyseData(String panelText, List<Advice> advices){
        this.panelText = panelText;
        this.advices = advices;
    }

    public String getPanelText() {
        return panelText;
    }

    public void setPanelText(String panelText) {
        this.panelText = panelText;
    }

    public List<Advice> getAdvices() {
        return advices;
    }

    public void setAdvices(List<Advice> advices) {
        this.advices = advices;
    }



}
