package org.taurus.aya.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;

import java.sql.Date;

public class LoginDialog extends Window {
	
	private final DynamicForm dynForm = new DynamicForm();
	final IButton submitButton = new IButton("Войти");
	TextItem userLogin;
	PasswordItem password;
	
	public LoginDialog()
	{
		setCanDragReposition(false);  
		setCanDragResize(false);
		setSize("310px", "100px");
		setTitle("Выбор пользователя");
		setShowHeader(false);
		setBodyColor("rgb(253, 253, 253)");
//		setBodyStyle("s3_windowBody");
		setAutoSize(true);	
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		this.addItem(createLayout());
	}
	
	private VLayout createLayout()
	{
		VLayout vlayout = new VLayout();
		vlayout.setAlign(VerticalAlignment.CENTER);
		vlayout.setMembersMargin(10);
		vlayout.setMargin(10);
		vlayout.setHeight("80px");			
		vlayout.setWidth100();

		HLayout hLayout1 = new HLayout();
		hLayout1.setWidth100();
		hLayout1.setHeight("64px");
		hLayout1.setMembersMargin(10);
//		hLayout1.setHeight(90);
		
		Img img = new Img("forms/key.png",64,64);
		hLayout1.addMember(img);
		hLayout1.addMember(dynForm);
		hLayout1.setAlign(VerticalAlignment.CENTER);
		vlayout.addMember(hLayout1);
		dynForm.setWidth100();	
		dynForm.setHeight(50);
		dynForm.setWrapItemTitles(false);
		dynForm.setHiliteRequiredFields(false);

        userLogin = new TextItem("nickname");
        userLogin.setTitle("Пользователь"); 
        userLogin.setRequired(true);
        userLogin.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Enter")) doLogin();
			}
		});
        //itemName.setPickListWidth(250);  
        //userLogin.setOptionDataSource(GlobalData.getDataSource_user());  
          
        password = new PasswordItem("password");
        password.setTitle("Пароль");
        password.setRequired(true);
        password.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Enter")) doLogin();
			}
		});
        dynForm.setItems(userLogin,password);
  
		HLayout hLayout2 = new HLayout();
		final IButton submitButton = new IButton("Войти");
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doLogin();
			}
		});
		
		
		hLayout2.setWidth(320);
//		hLayout2.setHeight(90);
		hLayout2.setMembersMargin(10);  
		hLayout2.setAlign(Alignment.CENTER);
		hLayout2.addMember(submitButton);

		vlayout.addMember(hLayout2);
		vlayout.setWidth100();
		vlayout.setAlign(Alignment.RIGHT);
		vlayout.setAlign(VerticalAlignment.CENTER);
		
		return vlayout;
	}

	private void doLogin()
	{
		if (dynForm.validate())
		{
			//Проверка хэша пароля для данного пользователя
			Record data = new Record();
			data.setAttribute("nickname",userLogin.getValueAsString());
			data.setAttribute("password",password.getValueAsString());
			GlobalData.getDataSource_user().performCustomOperation("fetchByNickname",data, new DSCallback(){

				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {

					if ( (dsResponse.getData().length == 1) )
					{
						SC.logWarn("Initialization. Password hash OK");
						
						final Record selectedRecord = dsResponse.getData()[0];
						
						//Идем дальше. Генерируем новый UUID
						String usid = selectedRecord.getAttributeAsString("usid");

						// Пишем его в Cookie 
						Date d = new Date(System.currentTimeMillis()+30L*24*3600*1000);

						//Cookies.setCookie("usid", usid, d);
						Cookies.setCookie("usid", usid, new Date(System.currentTimeMillis()+30L*24*3600*1000),"","/",false);

						//USID установлен - перезагружаем страницу
						com.google.gwt.user.client.Window.Location.reload();

					}
					else
						SC.warn("Вход в систему","Пара логин/пароль не верна");
//						SC.say("Login:'" + userLogin.getValueAsString() + "', hash:'" + hash + "' dsr:" + dsResponse.getDataAsRecordList().getLength());
					
				}});
		}

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
