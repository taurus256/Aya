package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.taurus.aya.client.widgets.PanelHeader;
import org.taurus.aya.shared.GraphData;

import java.util.Date;

public class StatisticsPanel extends VLayout {

    StatisticsPanel panel = this;
    Chart chart = new Chart();
    Label label = new Label("обновить график");
    HTMLPane pane = new HTMLPane();
    Date startDate, endDate;

    public StatisticsPanel(){
        setWidth(0);
        setShowResizeBar(true);
        setResizeBarSize(5);
        setOverflow(Overflow.HIDDEN);
        setBorder("1px solid #ababab");

        PanelHeader header = new PanelHeader("Статистика", new Runnable() {
            @Override
            public void run() {
                panel.setWidth(0);
            }
        });

        label.setWidth100();
        label.setHeight(30);
        label.setAlign(Alignment.LEFT);
        label.setValign(VerticalAlignment.CENTER);
        label.setPadding(5);
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {refreshData();}
        });

        chart.setType(Series.Type.AREA)
                .setChartTitleText("Время работы")
                .setMarginRight(20)
                .setWidth(300)
                .setHeight(250).setAreaPlotOptions(new AreaPlotOptions().setMarker(new Marker()
                .setEnabled(false)
                .setSymbol(Marker.Symbol.CIRCLE)
                .setRadius(1)
                .setHoverState(new Marker()
                        .setEnabled(true)
                )
        )  );

        chart.getYAxis().setAxisTitleText("");
        chart.setMarginLeft(15);
        chart.setMarginRight(10);
        chart.setColors("#157efb","#f2f2f2");

        pane.setWidth100();
        pane.setHeight100();
        pane.setMargin(10);

        this.addMember(header);
        this.addMember(label);
        this.addMember(chart);
        this.addMember(pane);

        initializeDates();
        GlobalData.setStatisticsPanel(this);
    }

    public void refreshData(){
        GlobalData.getAnalyticService().getMonthGraph(GlobalData.getCurrentUser().getAttributeAsLong("id"), startDate, endDate, new AsyncCallback<GraphData>() {
            @Override
            public void onFailure(Throwable caught) {
                SC.logWarn("Ошибка при вызове удаленного сервиса");
            }

            @Override
            public void onSuccess(GraphData data) {
                chart.removeAllSeries();
                Series series = chart.createSeries()
                        .setName("Затраты своего времени")
                        .setPoints(data.getSeriesLocal());
                Series seriesGroup = chart.createSeries()
                        .setName("Затраты времени группы")
                        .setPoints(data.getSeriesGroup());
                chart.getXAxis().setCategories(data.getCaptions());
                chart.addSeries(series);
                chart.addSeries(seriesGroup);

                pane.setContents(data.getStatistics());
            }
        });
    }

    private void initializeDates()
    {
        startDate = new Date();
        endDate = new Date();

        startDate.setDate(1);
        endDate.setDate(1);
        if (startDate.getMonth() < 11) {
            endDate.setMonth(startDate.getMonth() + 1);
            endDate.setYear(startDate.getYear());
        }
        else
        {
            endDate.setMonth(0);
            endDate.setYear(startDate.getYear() + 1);
        }
        endDate = new Date(endDate.getTime() - 24 * 3600 * 1000);
    }

    public void showPanel(){
        this.setWidth(300);
        refreshData();
    }

    public void updateDates(Date start, Date end){
        startDate=start;
        endDate=end;
        refreshData();
    }

    public void hidePanel(){
        this.setWidth(0);
    }
}
