package org.maestro.reports.plotter;

public class BmicPlotterWrapperFactory implements PlotterWrapperFactory<BmicPlotterWrapper> {

    @Override
    public BmicPlotterWrapper newPlotterWrapper() {
        return new BmicPlotterWrapper();
    }
}
