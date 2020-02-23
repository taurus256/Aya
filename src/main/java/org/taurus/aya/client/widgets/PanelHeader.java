package org.taurus.aya.client.widgets;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class PanelHeader extends HLayout {

    public PanelHeader(String caption, Runnable r) {
        Label label= new Label(caption);
        label.setBackgroundColor("#f2f2f2");
        label.setAlign(Alignment.LEFT);
        label.setValign(VerticalAlignment.CENTER);
        label.setPadding(5);
        label.setWidth100();
        label.setHeight(32);
        Img img = new Img("forms/minus.png");
        img.setWidth(32);
        img.setHeight(32);
        img.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                r.run();
            }
        });
        this.addMember(label);
        this.addMember(img);

        this.setWidth100();
        this.setHeight(32);
        this.setBackgroundColor("#f2f2f2");
    }
}
