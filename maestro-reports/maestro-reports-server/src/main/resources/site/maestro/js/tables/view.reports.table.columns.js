var dbColumns = [
    {
       data: "role"
    },
    {
       data: "protocol"
    },
    { data: "duration" },
    {
        data: "fcl",
        render: function (data, type, full, meta) {
            if (full.role == "sender") {
                return "N/A"
            }

            return  '<span> ' + data + ' ms</span>'
        }
    },
    { data: "apiName" },
    { data: "apiVersion" },
    { data: "parallelCount" },
    { data: "messageSize" },
    { data: "variableSize" },
    {
       data: "rate",
       render: rateRender
    },
    { data: "limitDestinations" },
    { data: "brokerUri" }
];