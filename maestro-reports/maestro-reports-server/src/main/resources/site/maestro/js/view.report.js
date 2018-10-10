$(document).ready(function () {
    var testId = getUrlVars()["test-id"];
    var testNumber = getUrlVars()["test-number"];

    var url = $('[data-datatables]').attr('data-api') + '/test/' + testId + '/number/' + testNumber + '/properties';

    console.log("Loading data from " + url)
    maestroDataTable('[data-datatables]', url)
})


function groupedBarGraphServiceTime(url, element, groups, yLabel) {
    axios.get(url).then(function (response) {
        var chartData = response.data

        var c3ChartDefaults = $().c3ChartDefaults();
        var lineChartConfig = c3ChartDefaults.getDefaultLineConfig();
        lineChartConfig.bindto = element;

//        console.log("Data " + chartData)

        lineChartConfig.data = {
            x: 'Percentiles',
            json: chartData,
        };

        lineChartConfig.legend = {
            position: 'right'
        };


        lineChartConfig.axis = {
            x: {
                label: {
                    text: 'Percentiles',
                },
                tick: {
                    rotate: 90,
                    multiline: false,
                    // Uses https://github.com/d3/d3-format
                    format: d3.format(".4f")
                }
            },
            y: {
                label: {
                    text: 'Milliseconds',
                }
            }
        };
        lineChartConfig.point = {
            show: false
        }

        var lineChart = c3.generate(lineChartConfig);

      })
      .catch(function (error) {
        console.log(error);
      });
    }

$(document).ready(function () {
    var reportId = getUrlVars()["report-id"];

    var url = $('[graphs]').attr('graph-api') + reportId;

    var element = '#bar-chart-3';

    var groups = [];
    var yLabel = 'Milliseconds';

    console.log("Loading data from " + url)
    groupedBarGraphServiceTime(url, element, groups, yLabel)
})