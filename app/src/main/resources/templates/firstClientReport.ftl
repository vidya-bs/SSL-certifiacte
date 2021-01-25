<html>
<head>
  <title>First Client SOAPUI Report</title>
</head>
<body>
    <#list testSuiteList as testSuite>
    <p style=\"text-align: left;\">&nbsp;<span style=\"color: #33cccc;\"><strong>${testSuite.testSuiteName}</strong></span></p>
      Total Testcases: ${testSuite.totalTestCases} 
      Total Passed: ${testSuite.totalPassed} </font>
      Total Failed: <font color="red">${testSuite.totalFailed}</font></p>
 <table border="1">
    <thead>
        <tr>
            <td>Test Case Name</td>
            <td>Status</td>
            <td>Failed Reason</td>
            <td>Duration</td>

        </tr>
    </thead>
    <#list testSuite.testCases as testCase>
    <tbody>
        <tr>
                <td> ${testCase.testCaseName}
      </td>
                <td>${testCase.testCaseStatus}
      </td>
                <td>${(testCase.failedReason)!}
      </td>
                <td>${testCase.duration} </td>
                
        </tr>
    </tbody>
     </#list>
</table>
 
<p>&nbsp;</p>
     
    </#list>
    
     <p style=\"text-align: left;\">&nbsp;<span style=\"color: #33cccc;\"><strong>
     Total: ${total} Failed: ${failed} Passed: ${passed} </strong></span></p>
     
</body>
</html>
