package org.taurus.aya.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.DateUtil;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.dialogs.LoginDialog;

import java.util.Date;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Aya implements EntryPoint {
    
 
	 private VLayout mainLayout;
     private HLayout northLayout;
     private HLayout southLayout;
     private VLayout eastLayout;
     private HLayout westLayout;
     private StatusBar statusBar;
     @SuppressWarnings("static-access")
	public void onModuleLoad() {
    	mainLayout.resizeFonts(1);
    	 
    	Date date = new Date();
//    	SC.logWarn(date.toString());
//    	DateUtil.setDefaultDisplayTimezone("+04:00");
//    	SC.logWarn(date.toString());

//    	DateUtil.setDefaultDisplayTimezone("+03:00");
// 		DateUtil.setAdjustForDST(false);

		KeyIdentifier debugKey = new KeyIdentifier();
		debugKey.setCtrlKey(true);
		debugKey.setKeyName("D");

		Page.registerKey(debugKey, new PageKeyHandler() {
			public void execute(String keyName) {
				SC.showConsole();
			}
		});

        // Настройка вывода дат
		/*
        DateUtil.setShortDateDisplayFormatter(new DateDisplayFormatter() {
			@Override
			public String format(Date date) {
				if(date == null) return null;
               final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM");
               String format = dateFormatter.format(date);
               SC.logWarn("date formatter work :" + format);
               return format;
			}
        });
        DateUtil.setNormalDateDisplayFormatter(new DateDisplayFormatter() {
			@Override
			public String format(Date date) {
				if(date == null) return null;
               final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM");
               String format = dateFormatter.format(date);
               SC.logWarn("normal date formatter work :" + format);
               return format;
			}
        });
        DateUtil.setNormalDateDisplayFormatter(new DateDisplayFormatter() {
			@Override
			public String format(Date date) {
				if(date == null) return null;
                final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yy");
                String format = dateFormatter.format(date);
                SC.logWarn("LONG date formatter work :" + format);
                return format;
			}
         });*/

		
		 Window.enableScrolling(false);
         Window.setMargin("0px");


         // Создание раскладки виджетов главного окна
         
         SC.logWarn("Initialization. Cookie is:" + Cookies.getCookie("usid"));
         final  String USID = Cookies.getCookie("usid");
         if (USID == null)
         {
        	 //remove the load indicator
			 DOM.getElementById("splash").removeChild(DOM.getElementById("infinity"));
				
        	 // USID в cookie отсутствует - первый вход на этой машине
        	 SC.logWarn("Initialization. No USID. Call user selection dialog");
        	 LoginDialog d = new LoginDialog();
			 d.show();
         }
         else
         {
        	 // USID есть. Поиск в БД пользователя с данным USID
	         Criteria crit = new Criteria();
	         Record r =new Record();
	         r.setAttribute("usid", USID);
	         GlobalData.getDataSource_user().performCustomOperation("fetchByUSID",r, new DSCallback(){

				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {

					//begin authorization process
					if (dsResponse.getData().length == 0)
					{
						// Пользователь c таким USID не найден (скорее всего, был выполнен вход на другой машине и USID перезаписался).
						// Отображение диалога выбора пользователя
						SC.logWarn("Initialization. No user with given USID. Call user selection dialog");

						LoginDialog d = new LoginDialog();

						d.show();
					}
					else
					{
						//Пользователь определен. Переустанавливаем cookie

			        	 Date d = new Date(System.currentTimeMillis()+30L*24*3600*1000);
						 Cookies.setCookie("usid", USID, d,"","/",false);
						 SC.logWarn("Initialization. Cookie is re-setted to " + d);

						 // Запоминаем его

						SC.logWarn("Initialization. User name is " + dsResponse.getData()[0].getAttributeAsString("firstname") + " user id = " + dsResponse.getData()[0].getAttributeAsString("id"));
						GlobalData.setCurrentUser(dsResponse.getData()[0]);

						// и запрашиваем данные групп
						Criteria criteria_group = new Criteria();
						GlobalData.getDataSource_relation_user_group().fetchData(criteria_group,new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {

								SC.logWarn("Initialization. Received list of groups");

								for (Record R: dsResponse.getData())
									SC.logWarn("\tUser belong to group " + R.getAttribute("name"));

								GlobalData.setCurrentUserGroups(dsResponse.getData());

								GlobalData.getDataSource_user().performCustomOperation("fetchAllDomainUsers", new Record(), new DSCallback(){
									@Override
									public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
										GlobalData.setUsers(dsResponse.getData());

										// отрисовываем интерфейс
										// Создание раскладки виджетов главного окна
										mainLayout = new VLayout();
										mainLayout.setWidth100();
										mainLayout.setHeight100();

										northLayout = new HLayout();
										northLayout.setHeight(27);

										VLayout vLayout = new VLayout();

										vLayout.addMember(new ApplicationMenu());
										northLayout.addMember(vLayout);

										westLayout = new NavigationArea();
										westLayout.setWidth("15%");

										eastLayout = new MainArea();
										eastLayout.setWidth("85%");

										southLayout = new HLayout();
										southLayout.setMembers(westLayout, eastLayout);

										mainLayout.addMember(northLayout);
										mainLayout.addMember(southLayout);

										statusBar = new StatusBar();
										mainLayout.addMember(statusBar);

										RootLayoutPanel.get().add(mainLayout);

										// Инициализация служебных объектов
										//TODO ViewManager.init();
										TabManager.init();

										SC.logInfo("Initialization: user list has " + dsResponse.getData().length + " records");
									}
								});

							}});

					}

					//remove the load indicator
					 DOM.getElementById("splash").removeChild(DOM.getElementById("infinity"));
				}});
         }
    }
}
