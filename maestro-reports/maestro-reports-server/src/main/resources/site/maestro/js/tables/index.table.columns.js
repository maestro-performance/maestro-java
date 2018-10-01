var dbColumns = [
    {
        data: "reportId",
        render: renderTestFull
    },
    {
        data: "testId",
        render: renderTestFull
    },
    { data: "testNumber" },
    { data: "testName" },
    { data: "testScript" },
    { data: "testHost" },
    { data: "testHostRole" },
    {
        data: "testResult",
        render: resultRender
    },
    { data: "location" },
];