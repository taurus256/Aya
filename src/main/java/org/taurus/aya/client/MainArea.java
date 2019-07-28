package org.taurus.aya.client;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

public class MainArea extends VLayout {

    final TabSet topTabSet = new TabSet();

	public MainArea() {

	    super();
		this.setHeight100();
	    
	    this.setOverflow(Overflow.AUTO);
	    	     
	    topTabSet.setTabBarPosition(Side.TOP);
	    topTabSet.setTabBarAlign(Side.LEFT);
	    topTabSet.setHeight100();
	    topTabSet.setMinHeight(0);
	    topTabSet.setTabBarThickness(32);
	    
	    VLayout hlayout = new VLayout();
	    hlayout.addMember(new HTMLFlow("Tab3"));
	    
 
	    topTabSet.selectTab(0);
	    GlobalData.setTopTabSet(topTabSet);
	    this.addMember(topTabSet);
}
    
private void addTabToTopTabset(String title, Canvas pane, boolean closable) {
	    Tab tab = createTab(title, pane, closable);
	    topTabSet.addTab(tab);
	    //topTabSet.selectTab(tab);
}

private Tab createTab(String title, Canvas pane, boolean closable) {
	    Tab tab = new Tab(title);
	    tab.setCanClose(closable);
	    tab.setPane(pane);
	    return tab;
}
}