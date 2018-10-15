$(document).ready(function () {
    var url = $('[data-datatables]').attr('data-api')

    maestroDataTable('[data-datatables]', url, indexDbColumns)
})