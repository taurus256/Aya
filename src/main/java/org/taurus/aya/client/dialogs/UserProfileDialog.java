package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

public class UserProfileDialog extends AbstractPropertiesDialog {

	boolean userHasChangedThePassword = false;
	DynamicForm bottomForm;
	PasswordItem oldPassword, newPassword1, newPassword2;
	UserProfileDialog thisDialog;
	
	public UserProfileDialog(Record user)
	{
		super(user, "document.png", ResourceType.TASK, GlobalData.getDataSource_user(), " профиля пользователя");

		thisDialog = this;
	}
	
	@Override
	public void constructInterface()
	{
		this.addItem(createFormLayout());
		this.addItem(createPasswordLayout());		
		this.addItem(createButtonsLayout());
	}
	
	private HLayout createPasswordLayout()
	{
		final HLayout hLayoutMain = new HLayout();
		hLayoutMain.setWidth100();
		hLayoutMain.setHeight(50);
		hLayoutMain.setMembersMargin(5);
		
		VLayout vlayoutContent =  new VLayout();
		vlayoutContent.setHeight100();
		
		final HLayout hlayoutLabel = new HLayout();
		hlayoutLabel.setMembersMargin(5);
		labelPermissions = new Label();
		labelPermissions.setIcon("forms/key_small.png");
		labelPermissions.setIconSize(32);
		labelPermissions.setHeight(50);
		labelPermissions.setWidth100();
		labelPermissions.setMargin(10);
		if (record.getAttributeAsString("passwordHash").equals("b480c074d6b75947c02681f31c90c668c46bf6b8"))
			labelPermissions.setContents("Задан стандартный пароль.<br>Рекомендуется сменить его");
		else
			labelPermissions.setContents("Раскройте, чтобы сменить пароль");
		hlayoutLabel.addMember(labelPermissions);
		
		final HLayout hlayoutControls = new HLayout();
		hlayoutControls.setWidth100();
		hlayoutControls.setVisible(false);
		vlayoutContent.setWidth100();
		hlayoutControls.addMember(createControls());
		vlayoutContent.addMember(hlayoutLabel);
		vlayoutContent.addMember(hlayoutControls);
		
		hLayoutMain.addMember(vlayoutContent);
		
		HLayout hLayoutButton = new HLayout();
		hLayoutButton.setWidth(50);
		hLayoutButton.setHeight100();
		hLayoutButton.setAlign(Alignment.CENTER);
		
		VLayout vLayoutButton =  new VLayout();
		vLayoutButton.setWidth("25px");
		vLayoutButton.setHeight100();
		vLayoutButton.setAlign(Alignment.CENTER);
				
		final ImgButton button = new ImgButton(){{
			setAlign(Alignment.CENTER);
			setSize("25px", "25px");
			setSrc("forms/down.png");
			setShowRollOver(false);
			setShowDown(false);
		}};
		button.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (hlayoutLabel.isVisible())
				{
					hlayoutLabel.hide();
					hlayoutControls.show();
					button.setWidth(0);
					button.hide();
					button.removeFromParent();
					hLayoutMain.setHeight(130);
					hLayoutMain.reflowNow();
					userHasChangedThePassword = true;					
				}
			}});
		vLayoutButton.addMember(button);
		hLayoutButton.addMember(vLayoutButton);
		hLayoutMain.addMember(hLayoutButton);
		canWriteToThisResource = true;

		return hLayoutMain;
	}
	
	private DynamicForm createControls()
	{
		bottomForm = new DynamicForm();
		bottomForm.setMargin(5);
		bottomForm.setWidth100();
		bottomForm.setHiliteRequiredFields(false);
		
		oldPassword = new PasswordItem("old_pass");
		oldPassword.setTitle("Старый пароль");
		oldPassword.setRequired(true);
		
		newPassword1 = new PasswordItem("new_pass1");
		newPassword1.setTitle("Новый пароль");
		newPassword1.setRequired(true);
		
		newPassword2 = new PasswordItem("new_pass2");
		newPassword2.setTitle("Повтор нового пароля");
		newPassword2.setRequired(true);
		
		bottomForm.setItems(oldPassword,newPassword1,newPassword2);

		return bottomForm;
	}
	
//	protected VLayout createButtonsLayout()
//	{
//		VLayout vlayout = new VLayout();
//		vlayout.setHeight("50px");
//		vlayout.setAlign(VerticalAlignment.CENTER);
//
//		HLayout hlayout = new HLayout();
//		hlayout.setAlign(VerticalAlignment.CENTER);
//
//		final IButton submitButton = new IButton("Сохранить");
//		submitButton.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				//Установка нового пароля, если он был задан
//				if (userHasChangedThePassword)
//				{
//					if (!bottomForm.validate()) return;
//
//					if (!getSHA1(oldPassword.getValueAsString()).equals(record.getAttributeAsString("passwordHash")))
//					{
//						SC.warn("Старый пароль введен неверно");
//						return;
//					}
//					if (!newPassword1.getValueAsString().equals(newPassword2.getValueAsString()))
//					{
//						SC.warn("Новые пароли не совпадают");
//						return;
//					}
//					//record.setAttribute("passwordHashh", getSHA1(newPassword1.getValueAsString()));
//					df.getField("passwordHash").setValue(getSHA1(newPassword1.getValueAsString()));
//				}
//
//				//saveDialogData();
//				SC.say("save data");
//				df.saveData(new DSCallback(){
//
//					@Override
//					public void execute(DSResponse dsResponse, Object data,
//                                        DSRequest dsRequest) {
//						GlobalData.getStatusBar().indicateMessage("Данные пользователя обновлены");
//						thisDialog.hide();
//					}});
//			}
//		});
//
//		submitButton.setWidth("150px");
//
//		final IButton cancelButton = new IButton("Отменить");
//		cancelButton.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				hide();
//			}
//		});
//
//		hlayout.addMember(submitButton);
//		hlayout.addMember(cancelButton);
//		hlayout.setWidth("400px");
//		hlayout.setMembersMargin(10);
//		hlayout.setMargin(10);
//		hlayout.setAlign(Alignment.RIGHT);
//
//		vlayout.addMember(hlayout);
//
//		return vlayout;
//	}

	/* Обращение к JavaScript-библиотеке для получения хеша sha1 */
	private native String getSHA1(String str)
	/*-{
		return window.parent.Sha1.hash(str,{ msgFormat: 'string', outFormat: 'hex' });
	}-*/;
}
