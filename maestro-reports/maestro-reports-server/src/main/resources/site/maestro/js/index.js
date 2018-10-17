$(document).ready(function () {
    var url = $('[data-datatables]').attr('data-api')

    maestroDataTableOrdered('[data-datatables]', url, indexDbColumns, 0, 'desc')
})