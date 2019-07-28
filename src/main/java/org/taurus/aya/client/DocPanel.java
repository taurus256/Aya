/**
 * Боковая панель отображения дерева документов
 */
package org.taurus.aya.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.dialogs.AddFileDialog;
import org.taurus.aya.client.generic.GenericPanel;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

public class DocPanel extends GenericPanel {

	public DocPanel()
	{
		super(GlobalData.getDataSource_docs(), "document.png", ResourceType.DOCUMENT, "Новый документ", "документа");
		
		treeGrid.addDoubleClickHandler(new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
					selected = treeGrid.getSelectedRecord();
					TabManager.openTab(TabManager.ResourceType.DOCUMENT, selected);
			}});
		
		MenuItem menuAddFile = new MenuItem("Добавить файл");
		menuAddFile.setIcon("menu/file.png");
		menuAddFile.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event)
			{
				//Create new record
				Record newRecord = new Record();
				newRecord.setAttribute("isFolder", false);
				
				//Setting parent for newly created record
				if (selected == null) //ничего не выбрано
				{
					newRecord.setAttribute("parent", 0);
					SC.logWarn("DocPanel: selected record is NULL");
				}
				else
				{
					SC.logWarn("DocPanel: selected record ID=" + selected.getAttributeAsString("id"));
					if (selected.getAttributeAsBoolean("isFolder")) 
						newRecord.setAttribute("parent", selected.getAttribute("id")); // выбрана папка
					else
						newRecord.setAttribute("parent", selected.getAttribute("parent")); // выбран элемент в папке
					
					newRecord.setAttribute("author", GlobalData.getCurrentUser().getAttribute("id"));
				}
				
				AddFileDialog adf = new AddFileDialog(newRecord);
				adf.show();
			}
		});
		
		menu.addItem(menuAddFile,1);
		
		hrProperties.removeHandler();
		hrProperties = menuProperties.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				if (treeGrid.getSelectedRecord().getAttributeAsInt("type")==1)
				{
					GenericPropertiesDialog gpd = new GenericPropertiesDialog(treeGrid.getSelectedRecord(), iconFile, resourceType, dataSource, objectNameWho);
					gpd.constructInterface();
					gpd.show();
				}
				else
				{
					AddFileDialog adf = new AddFileDialog(treeGrid.getSelectedRecord());
					adf.show();
				}
			}
			
		});
		
		menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT + 3);
		}
}
