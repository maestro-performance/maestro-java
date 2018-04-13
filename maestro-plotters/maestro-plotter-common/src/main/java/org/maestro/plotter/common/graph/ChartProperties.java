/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.plotter.common.graph;

public class ChartProperties {
    private String title = "";
    private String seriesName = "";
    private String xTitle = "";
    private String yTitle = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getxTitle() {
        if (xTitle != null && xTitle.length() >= 2) {
            return this.xTitle.substring(0, 1).toUpperCase() + xTitle.substring(1);
        }
        else {
            return xTitle;
        }
    }

    public void setxTitle(String xTitle) {
        this.xTitle = xTitle;
    }

    public String getyTitle() {
        if (yTitle != null && yTitle.length() >= 2) {
            return this.yTitle.substring(0, 1).toUpperCase() + yTitle.substring(1);
        }
        else {
            return yTitle;
        }
    }

    public void setyTitle(String yTitle) {
        this.yTitle = yTitle;
    }
}
