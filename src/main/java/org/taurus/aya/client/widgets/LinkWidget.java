package org.taurus.aya.client.widgets;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.tile.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tile.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tile.events.SelectionChangedEvent;
import com.smartgwt.client.widgets.tile.events.SelectionChangedHandler;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.TagView;
import org.taurus.aya.client.generic.GenericPanel;

public class LinkWidget extends VLayout {
	Record currentRecord;
	TagView tv;
	Canvas selectedTile = null;
	Record selectedRecord = null;
	final TileGrid tileGrid;
	
	public LinkWidget(Record record)
	{
		currentRecord = record;
		this.setWidth100();
		this.setHeight(200);

        tileGrid = new TileGrid();
        tileGrid.setTileWidth(300);  
        tileGrid.setTileHeight(100);  
        tileGrid.setHeight100();  
        tileGrid.setWidth100();
        tileGrid.setBackgroundColor("white");
        tileGrid.setTileMargin(0);
        
        tileGrid.setShowAllRecords(false);
        tileGrid.setAutoFetchData(true);
        final Criteria criteria = new Criteria();
        criteria.addCriteria("from_id", currentRecord.getAttributeAsInt("id"));
        criteria.addCriteria("from_type", ResourceType.TAG.ordinal());
        tileGrid.setInitialCriteria(criteria);
        tileGrid.setDataSource(GlobalData.getDataSource_links());
        tileGrid.setTileConstructor(CustomTagTile.class.getName());
        tileGrid.setCanAcceptDroppedRecords(true); 
        tileGrid.setCanAcceptDrop(true);
        
        this.addMember(tileGrid);
        
        tileGrid.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				GlobalData.getStatusBar().stopIndicateProcess(); // loading now complete
			}
		});
        
        tileGrid.addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Delete"))
					tileGrid.removeData(tileGrid.getSelectedRecord());;
			}});
        
    tileGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			
			@Override
			public void onSelectionChanged(SelectionChangedEvent event) {
				selectedRecord = event.getRecord();
				Canvas tile = (Canvas) tileGrid.getTile(selectedRecord);
				tile.setBorder("2px solid #157efb");
				if (selectedTile != null) selectedTile.setBorder("none");
				selectedTile = tile;
			}
		});
        
	tileGrid.addDropHandler(new DropHandler() {
        public void onDrop(DropEvent event) {
        	SC.logWarn("addDropHandler start");
        	GenericPanel sourcePanel = GlobalData.getCurrentPanel();
        	ResourceType resourceType = sourcePanel.getResourceType();
        	Record sourceRecord = sourcePanel.getTreeSelectedRecord();
        	Record r = new Record();
        	SC.logWarn("addDropHandler start - 1");
        	r.setAttribute("from_id",currentRecord.getAttributeAsInt("id"));
        	r.setAttribute("from_type", ResourceType.TAG.ordinal());
        	SC.logWarn("addDropHandler start - 2");
			r.setAttribute("name", sourceRecord.getAttribute("name"));        	
        	r.setAttribute("to_id",sourceRecord.getAttributeAsInt("id"));
        	r.setAttribute("to_type", resourceType.ordinal());
        	SC.logWarn("addDropHandler switch");
        	switch (resourceType)
        	{
        	case TASK: 
        		{
        			r.setAttribute("image", "task.png");
        			break;
        		}
			case DOCUMENT: 
				{
					r.setAttribute("image", "document.png");
					break;
				}
				
			case SCRIPT: 
				{
					r.setAttribute("image", "script.png");
					break;	
				}
			case TAG:
				break;
			default: SC.warn("Этот объект не может быть ассоциирован с тегом");
				break;
        	}
        	SC.logWarn("addDropHandler event");
        	event.cancel();
        	SC.logWarn("addDropHandler end");
        	tileGrid.getDataSource().addData(r);
        }
    });
	}
}
