package org.taurus.aya.client;

import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPanel;

public class ScriptPanel extends GenericPanel {

	public ScriptPanel()
	{
		super(GlobalData.getDataSource_scripts(), "script.png", ResourceType.SCRIPT, "Новый скрипт", "скрипта");
		treeGrid.addDoubleClickHandler(new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
					selected = treeGrid.getSelectedRecord();
					if (selected != null && !selected.getAttributeAsBoolean("isFolder")) //если выбрана не папка
					{
						GlobalData.getStatusBar().indicateProcess("Открываю скрипт \"" + selected.getAttribute("name") + "\"...");
						TabManager.openTab(ResourceType.SCRIPT, selected);
					}
			}
		});
	}
	
	@Override
	public ResourceType getResourceType()
	{
		return resourceType;
	}
}
