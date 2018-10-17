function maestroDataTable(element, url, dbColumns) {
    maestroDataTableOrdered(element, url, dbColumns, 1, 'desc')
};


function maestroDataTableOrdered(element, url, dbColumns, idx, order) {
    $(element).DataTable({
        columns: dbColumns,
        ajax: {
            url: url,
            dataSrc:  ''
        },
        order: [[ idx, order ]],
    });
};