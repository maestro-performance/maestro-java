function maestroDataTable(element, url, dbColumns) {
    $(element).DataTable({
        columns: dbColumns,
        ajax: {
            url: url,
            dataSrc:  ''
        },
        order: [[ 1, 'desc' ]],
    });
};