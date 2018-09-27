function percentileRender(data, type, full, meta) {
    if (full.envResourceRole == "sender") {
        return "N/A"
    }

    return  '<span> ' + ( Number(data).toFixed(2) / 1000) + ' ms</span>'
}

function rateRender(data, type, full, meta) {
    return  '<span> ' + data + ' msg/sec</span>'
}

function resultRender(data, type, full, meta) {
    if (data == "success") {
        return '<span class="pficon pficon-ok"> ' + data + '</span>'
    }
    else {
        return '<span class="pficon pficon-error-circle-o"> ' + data + '</span>'
    }
}

function simpleDateRender(data, type, full, meta) {
    return (new Date(data)).toLocaleString();
}


function renderTestId(data, type, full, meta) {
    return '<a href=\"view-single-test-results.html?test-id=' + data + '\">' + data + '</a>';
}

function renderRounded(data, type, full, meta) {
     return Number(data).toFixed(2);
}

function renderRoundedRate(data, type, full, meta) {
    return '<span> ' + renderRounded(data, type, full, meta) + ' msg/sec</span>'
}