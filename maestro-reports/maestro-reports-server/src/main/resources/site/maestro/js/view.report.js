$(document).ready(function () {
    var testId = getUrlVars()["test-id"];
    var testNumber = getUrlVars()["test-number"];

    var url = $('[data-datatables]').attr('data-api') + '/test/' + testId + '/number/' + testNumber + '/properties';

    console.log("Loading data from " + url)
    maestroDataTable('[data-datatables]', url)
})