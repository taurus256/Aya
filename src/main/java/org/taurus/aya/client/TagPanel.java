package org.taurus.aya.client;

import com.smartgwt.client.data.AdvancedCriteria;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPanel;

public class TagPanel extends GenericPanel {
	public TagPanel()
	{
		super(GlobalData.getDataSource_tags(), "tag.png", ResourceType.TAG, "Новый тег", "тега");
		treeGrid.setCanDragRecordsOut(true);
		
		AdvancedCriteria laneCriteria = new AdvancedCriteria();
		laneCriteria.addCriteria(GlobalData.createSearchCriteria());
		treeGrid.setInitialCriteria(laneCriteria);
		treeGrid.setDragDataAction(DragDataAction.COPY);

		treeGrid.addDoubleClickHandler(new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				selected = treeGrid.getSelectedRecord();
				
				if (selected != null && !selected.getAttributeAsBoolean("isFolder")) //если выбрана не папка
				{	
					GlobalData.getStatusBar().indicateProcess("Открываю список объектов для тега \"" + selected.getAttribute("name") + "\"...");
					TabManager.openTab(TabManager.ResourceType.TAG, selected);
				}
			}});
	}
}
