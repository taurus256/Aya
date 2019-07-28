package org.taurus.aya.client;

/* Интерфейс боковой панели.
 * Поддерживает методы, необходимые для переноса записей из любой панели методом drag-n-drop 
 * */

import com.smartgwt.client.data.Record;

public interface SidePanel {
	public Record getTreeSelectedRecord();
	public TabManager.ResourceType getResourceType();
	public void update();
}
