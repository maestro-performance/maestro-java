$(document).ready(function () {
    var url = $('[data-datatables]').attr('data-api')

    maestroDataTableOrdered('[data-datatables]', url, indexDetailedDbColumns, 0, 'desc')
})