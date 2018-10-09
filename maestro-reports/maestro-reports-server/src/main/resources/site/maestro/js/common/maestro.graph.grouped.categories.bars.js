//function groupedBarGraph(url, element, values, groups, yLabel) {
//    axios.get(url).then(function (response) {
//        var chartData = response.data
//
//        var c3ChartDefaults = $().c3ChartDefaults();
//        var lineChartConfig = c3ChartDefaults.getDefaultGroupedBarConfig();
//        lineChartConfig.bindto = element;
//
//        // Latency distributions per test
//        lineChartConfig.data = {
//            json: chartData.Pairs,
//            keys: {
//                value: values
//            },
//            type: 'bar',
//            groups: [
//                groups
//            ],
//        };
//
//        lineChartConfig.legend = {
//            position: 'right'
//        }
//
//        lineChartConfig.axis = {
//            x: {
//                type: 'category',
//                categories: chartData.Categories
//            },
//            y: {
//               label: {
//                   text: yLabel,
//               }
//           }
//        };
//
//        var lineChart = c3.generate(lineChartConfig);
//
//      })
//      .catch(function (error) {
//        console.log(error);
//      });
//    }