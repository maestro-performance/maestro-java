package net.orpiske.mpt.reports.plotter;

public class HdrPlotterWrapperFactory implements PlotterWrapperFactory<HdrPlotterWrapper> {
    @Override
    public HdrPlotterWrapper newPlotterWrapper() {
        return new HdrPlotterWrapper();
    }
}
