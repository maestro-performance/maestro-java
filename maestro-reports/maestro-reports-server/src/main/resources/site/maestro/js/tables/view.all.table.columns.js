var indexDetailedDbColumns = [
    {
        data: "testId",
        visible: false
    },
    {
        data: "testDate",
        render: simpleDateRender
    },
    {
        data: "testId",
        render: renderTestFull
    },
    {
        data: "reportId",
        render: renderReportIdFull
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
    { data: "testResultMessage" },
    { data: "testComments" },
    { data: "testDescription" },

];