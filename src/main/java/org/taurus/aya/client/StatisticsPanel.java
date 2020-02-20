package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.taurus.aya.shared.GraphData;

public class StatisticsPanel extends VLayout {

    Chart chart = new Chart();
    Label label = new Label("Статистика");

    public StatisticsPanel(){
        setMinWidth(300);
        setShowResizeBar(true);
        setCanDragResize(true);

        label.setWidth100();
        label.setHeight(30);
        label.setBackgroundColor("#f2f2f2");
        label.setAlign(Alignment.LEFT);
        label.setValign(VerticalAlignment.CENTER);
        label.setPadding(5);
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                GlobalData.getAnalyticService().getMonthGraph(GlobalData.getCurrentUser().getAttributeAsLong("id"), new AsyncCallback<GraphData>() {
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
                    }
                });
            }
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
        chart.setColors("#157efb","#ababab");

        this.addMember(label);
        this.addMember(chart);
    }
}
