package org.taurus.aya.client.widgets;


import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.util.DateFormatStringFormatter;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import org.taurus.aya.client.GlobalData;

public class ChatGrid extends ListGrid {

	private Integer thisUserId;
	private String userName, remoteUserName;
	public static final String SENDER_ID_FIELD = "sender_id";
	public static final String TEXT_FIELD = "content";
	public static final String SEND_DATE_FIELD = "stamp";
	
	public ChatGrid(Record thisUser, Record remoteUser)
	{
		thisUserId = thisUser.getAttributeAsInt("id");
		this.userName = thisUser.getAttributeAsString("showedName");
		this.remoteUserName = remoteUser.getAttributeAsString("showedName");
		
		setSaveLocally(true);
		
		ListGridField textField = new ListGridField(TEXT_FIELD);
        textField.setCellFormatter(new CellFormatter() {
            @Override
            public String format(Object o, ListGridRecord listGridRecord, int i, int i1) {
            	
            	//Format the date
            	DateFormatStringFormatter dateFormatter = new DateFormatStringFormatter("HH:mm dd.MM.yyyy");
            	String date = dateFormatter.format(listGridRecord.getAttributeAsDate(SEND_DATE_FIELD));
            	
            	//Set styles for user name, message time and content
            	if (listGridRecord.getAttributeAsInt("sender_id").equals(thisUserId))
            		return "<span class=\"user\">" + userName + " </span><span class=\"stamp\">" + date + "</span>:<br><div class=\"content\">" + o.toString() + "</span>";
            	else
            		return "<span class=\"remote\">" + remoteUserName + " </span><span class=\"stamp\">" + date + "</span>:<br><div class=\"content\">" + o.toString() + "</span>";
            }
        });
        textField.setDateFormatter(DateDisplayFormat.TOUSSHORTDATETIME);
        setFields(textField);
        
//		addDataArrivedHandler(new DataArrivedHandler() {
//				@Override
//				public void onDataArrived(final DataArrivedEvent event) 
//				{
//					scrollToRow(event.getEndRow());				
//				}
//	        });
		
		//turn off record content clipping 
		setWrapCells(true);  
        setFixedRecordHeights(false);
        setCanDragSelectText(true);
        setCanFocus(true);
	}
	
	public ChatGrid(Record thisUser)
	{
		thisUserId = thisUser.getAttributeAsInt("id");
		this.userName = thisUser.getAttributeAsString("showedName");
		
		setSaveLocally(true);
		
		ListGridField textField = new ListGridField(TEXT_FIELD);
        textField.setCellFormatter(new CellFormatter() {
            @Override
            public String format(Object o, ListGridRecord listGridRecord, int i, int i1) {
            	
            	//Format the date
            	DateFormatStringFormatter dateFormatter = new DateFormatStringFormatter("HH:mm dd.MM.yyyy");
            	String date = dateFormatter.format(listGridRecord.getAttributeAsDate(SEND_DATE_FIELD));
            	
            	//Set styles for user name, message time and content
            	if (listGridRecord.getAttributeAsInt("sender_id").equals(thisUserId))
            		return "<span class=\"user\">" + userName + "</span><span class=\"stamp\">" + date + " </span>:<br><div class=\"content\">" + o.toString() + "</span>";
            	else
            		for (Record r : GlobalData.getUsers())
            			if (r.getAttributeAsInt("id").equals(listGridRecord.getAttributeAsInt("sender_id")))
            				return "<span class=\"remote\">" + r.getAttributeAsInt("showedName") + " </span><span class=\"stamp\">" + date + "</span>:<br><div class=\"content\">" + o.toString() + "</span>";
            	
            	return "Cannot convert";
            }
        });
        textField.setDateFormatter(DateDisplayFormat.TOUSSHORTDATETIME);
        setFields(textField);
        
//		addDataArrivedHandler(new DataArrivedHandler() {
//				@Override
//				public void onDataArrived(final DataArrivedEvent event) 
//				{
//					scrollToRow(event.getEndRow());				
//				}
//	        });
		
		//turn off record content clipping 
		setWrapCells(true);  
        setFixedRecordHeights(false);
	}

	
	@Override
    protected String getCellCSSText(ListGridRecord listGridRecord, int i, int i1) {
        String css = "border:0px; padding-top: 7px; padding-bottom: 7px; line-height:12pt;";

        Integer senderId = listGridRecord.getAttributeAsInt(SENDER_ID_FIELD);
        if  (thisUserId != null && senderId != null && thisUserId.equals(senderId))
        {
        	css += "text-align: right; background-color:white"; 
        }
        else
        { 
        	css += "text-align: left; background-color:#f7f7f7";
        }
        
        return css;
    }
}
