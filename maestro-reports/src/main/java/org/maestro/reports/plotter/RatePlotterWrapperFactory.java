package org.maestro.reports.plotter;

public class RatePlotterWrapperFactory implements PlotterWrapperFactory<RatePlotterWrapper> {
    @Override
    public RatePlotterWrapper newPlotterWrapper() {
        return new RatePlotterWrapper();
    }
}
