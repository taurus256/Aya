package org.taurus.aya.client.widgets;

import com.smartgwt.client.widgets.HTMLPane;

public class CustomHTMLPane extends HTMLPane
{
    protected native void onInit()/*-{
        this.@org.taurus.aya.client.widgets.CustomHTMLPane::onInitialize()();
        
        // Handle redraw case
        var self = this.@com.smartgwt.client.widgets.BaseWidget::getOrCreateJsObj()();
        self.redraw = function() {
        }
        
    }-*/;
    
    protected void onInitialize()
    {
        super.onInit();
    }
}