package org.taurus.aya.client;
/**
 * Боковая панель
 */
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.tree.TreeGrid;
import org.taurus.aya.client.widgets.PanelHeader;

public class NavigationArea extends com.smartgwt.client.widgets.layout.VLayout {
	
	private TaskPanel taskPanel;
	NavigationArea navigationArea = this;

    public NavigationArea() {
    	
		super();
	
	    this.setOverflow(Overflow.HIDDEN);
	    this.setShowResizeBar(false);
	    this.setWidth(250);
	    this.setMinWidth(250);
	    this.setAnimateMembers(false);
		this.setBorder("1px solid #ababab");
		
	    taskPanel = new TaskPanel();
	    taskPanel.setHeight100();

	    PanelHeader header = new PanelHeader("Задачи", new Runnable(){
			@Override
			public void run() {
				navigationArea.setWidth(0);
			}
		});
	    header.setHeight(32);

		this.addMember(header);
	    this.addMember(taskPanel);
	    
	    GlobalData.setNavigationArea(this);
	}

	public TaskPanel getTaskPanel() {
		return taskPanel;
	}
}
