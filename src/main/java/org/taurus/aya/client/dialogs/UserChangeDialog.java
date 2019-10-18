package org.taurus.aya.client.dialogs;

import com.google.gwt.user.client.Cookies;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;

import java.sql.Date;

public class UserChangeDialog extends Window {
	
	private final DynamicForm dynForm = new DynamicForm();
	
	ComboBoxItem userLogin;
	
	public UserChangeDialog()
	{
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("180px", "80px");
		setTitle("Выбор пользователя");
		setBodyColor("rgb(253, 253, 253)");
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);

		this.addItem(createLayout());
	}
	
	private VLayout createLayout()
	{
		VLayout vlayout = new VLayout();
		vlayout.setMembersMargin(10);
		vlayout.setMargin(10);
		vlayout.setHeight("80px");			
		
		vlayout.addMember(dynForm);
		dynForm.setWidth100();
		dynForm.setWrapItemTitles(false);

		  
        userLogin = new ComboBoxItem("nickname");
        userLogin.setValueField("nickname");
        userLogin.setDisplayField("nickname");
        userLogin.setTitle("Пользователь");  
        //itemName.setPickListWidth(250);  
        userLogin.setOptionDataSource(GlobalData.getDataSource_user());  
          
        dynForm.setItems(userLogin);
  
		HLayout hlayout = new HLayout();
		final IButton submitButton = new IButton("Выбрать");
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//Получаем новый UUID
				String usid = generateUUID();

				// Пишем его в Cookie 
				Cookies.setCookie("usid", "", new java.util.Date(System.currentTimeMillis()+30L*24*3600*1000),"","/",false);
				
				// И в БД
				Record selectedRecord = userLogin.getSelectedRecord();
				selectedRecord.setAttribute("usid",usid);

				GlobalData.getDataSource_user().updateData(selectedRecord, new DSCallback(){
					@Override
					public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
						//Данные обновились - перезагружаем страницу
						com.google.gwt.user.client.Window.Location.reload();
					}});
			}
		});
		
		final IButton cancelButton = new IButton("Отменить");
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		hlayout.setWidth("180px");
		hlayout.setMembersMargin(10);  
		hlayout.setAlign(Alignment.RIGHT);
		hlayout.addMember(submitButton);
		hlayout.addMember(cancelButton);
		
		vlayout.addMember(hlayout);
		vlayout.setWidth("180px");
		vlayout.setAlign(Alignment.RIGHT);
		vlayout.setAlign(VerticalAlignment.CENTER);
		
		return vlayout;
	}

	
	/* Javascript-функция получения uuid */
	public native String generateUUID() 
	/*-{
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	    });
	    return uuid;
	}-*/;
}
