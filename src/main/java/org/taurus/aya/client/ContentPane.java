package org.taurus.aya.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

public abstract class ContentPane extends VLayout {
	
	private boolean deferredUpdate = false;
	private boolean showCloseMessage = false;
	private String closeMessage="Есть несохраненные изменения!<br>Закрыть вкладку?";
	

	public abstract Record getResource(); // вернуть запись с ресурсом, для которог открыта вкладка (задачу, документ и т.п.)
	public abstract TabManager.ResourceType getResourceType(); // вернуть тип ресурса
	public abstract void updateContent(Boolean all); // обновить всё содержмое вкладки

	public boolean isShowCloseMessage() {
		return showCloseMessage;
	}
	
	public void setShowCloseMessage(boolean showCloseMessage) {
		this.showCloseMessage = showCloseMessage;
	}

	public void setCloseMessage(String closeMessage) {
		this.closeMessage = closeMessage;
	}
	
	//this method called when user attempts to close the tab
	public void onClose(final Tab tab)
	{
		if (getResourceType()==null || getResource()==null) { SC.logWarn("ContentPane: Error! Pane has been not properly initialized!"); return;};
		
		final Dialog dialog = new Dialog();
		dialog.setButtons(Dialog.YES, Dialog.NO, Dialog.CANCEL);
		
		if (showCloseMessage)
			SC.confirm("Закрытие вкладки",closeMessage, new BooleanCallback(){

				@Override
				public void execute(Boolean value) {
					if (value!=null)
						TabManager.removeTab(tab, getResourceType(), getResource());
				}}, dialog);
		else
			TabManager.removeTab(tab, getResourceType(), getResource());
	}  
	
	// установить флаг отложенного обновленя (при активации вкладки)
	public void setDeferredUpdateFlag()
	{
		deferredUpdate = true;
	}
	
	// вызвать отложенное обновление
	public void deferredUpdate()
	{
		SC.logWarn("ContentPane:call deferredUpdate()");
		if (deferredUpdate) 
		{			
			SC.logWarn("ContentPane:call updateContent");
			updateContent(true);
		}
	}
}
