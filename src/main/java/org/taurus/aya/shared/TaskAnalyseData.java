package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TaskAnalyseData implements IsSerializable {

    String panelText;

    String advancedText;

    public TaskAnalyseData(){}

    public TaskAnalyseData(String text)
    {
        setPanelText(text);
    }

    public TaskAnalyseData(String text, String advancedText)
    {
        setPanelText(text);
        setAdvancedText(advancedText);
    }

    public String getPanelText() {
        return panelText;
    }

    public void setPanelText(String panelText) {
        this.panelText = panelText;
    }

    public String getAdvancedText() {
        return advancedText;
    }

    public void setAdvancedText(String advancedText) {
        this.advancedText = advancedText;
    }
}
