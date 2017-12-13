package net.orpiske.mpt.reports.plotter;

public class BmicPlotterWrapperFactory implements PlotterWrapperFactory<BmicPlotterWrapper> {

    @Override
    public BmicPlotterWrapper newPlotterWrapper() {
        return new BmicPlotterWrapper();
    }
}
