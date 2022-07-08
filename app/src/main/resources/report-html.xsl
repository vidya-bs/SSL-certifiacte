<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
    <xsl:output method="html" indent="yes"/>
    <xsl:template match="/">
            <html>
                <style>
                    table tr td, table tr th 
                    {
                        border-bottom: 1px solid black;
                        border-right: 1px solid black;
                    }
                    table tr th
                    {
                        border-top: 1px solid black;
                    }
                    table tr :first-child
                    {
                        border-left: 1px solid black;
                    }
                    #myTable
                    { 
                        width: 100%; 
                        table-layout: fixed;

                    }
                    td{
                        word-wrap: break-word;
                    }
                    .center 
                    {
                        display: block;
                        margin-left: auto;
                        margin-right: auto;
                        width: 50%;
                    }
                    image-container{
                        width: 100%;
                    }

                </style>
                    
                <body>
                <center><h1>TESTSUITE REPORT</h1></center>



                    





                    <xsl:for-each select="ObjectNode/testSuite">
                    <h1><xsl:value-of select="name" /></h1>
                     <h5>EXECUTION DURATION: <xsl:value-of select="duration" /></h5>
                      <h5>ACTIVE: <xsl:value-of select="isActive" /></h5>
                       <h5>SUCCESSRATE: <xsl:value-of select="successRate" /></h5>
                        <h5>DATE: <xsl:value-of select="date" /></h5>

                            <table id="myTable" cellspacing="0" >
                                <tr>
                                    <th>Scenario Name</th>                                           
                                    <th>TestCase Name</th>
                                    <th>Status</th>
                                    <th>Stats</th>
                                </tr>
                            <xsl:for-each select="scenarios">
                                <tr>
                                    <td>
                                        <xsl:number format="1. "/><xsl:value-of select="name" />
                                    </td>    
                                    <td>
                                        <xsl:for-each select="testCases">
                                            <xsl:value-of select="name" />
                                        </xsl:for-each>
                                    </td>      
                                    <td>
                                        <xsl:value-of select="status" />
                                    </td>  
                                            
                                    <td>
                                        <xsl:variable name="headers"  select="count(testCases/response/assertions/headers)"/>
                                        <xsl:variable name="status"  select="count(testCases/response/assertions/status)"/>
                                        <xsl:variable name="body"  select="count(testCases/response/assertions/body)"/>

                                        <xsl:variable name="body1" select="count(testCases/response/assertions/status[status='PASS'])"/>
                                        <xsl:variable name="headers1" select="count(testCases/response/assertions/headers[status='PASS'])"/>
                                        <xsl:variable name="status1" select="count(testCases/response/assertions/body[status='PASS'])"/>
                                     

                                        <xsl:value-of select="$body1+$headers1+$status1" />/<xsl:value-of select="$body+$headers+$status" />
                                    </td>                                                               
                                </tr>                                           
                            </xsl:for-each>
                            </table>



                        <xsl:for-each select="scenarios">
                        
                            <h2><xsl:number format="1. "/><xsl:value-of select="name" /></h2> 
                            <xsl:for-each select="testCases">
                                <h3><xsl:value-of select="name" /></h3> 
                                <h5>PATH: <xsl:value-of select="path" /></h5>
                                <h5>VERB: <xsl:value-of select="verb" /></h5>


                                <h4>REQUEST</h4>
                                <xsl:for-each select="request">
                                    <table id="myTable" cellspacing="0" >
                                        <h5>HEADERS</h5>
                                        <tr>
                                            <th>Name</th>
                                            
                                            <th>Value</th>
                                            <th>Description</th>
                                            <th>IsEncryption</th>
                                        </tr>
                                        <xsl:for-each select="headers">
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="name" />
                                                </td>

                                                <td>
                                                    <xsl:value-of select="value" />
                                                </td>
                                                <td width="100%">
                                                    <xsl:value-of select="description" />
                                                </td>  
                                                <td>
                                                    <xsl:value-of select="isEncryption" />
                                                </td>                                                                                
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                        <xsl:for-each select="body">
                                        <table id="myTable" cellspacing="0">
                                        <h5>BODY</h5>
                                        <h6>Type:<xsl:value-of select="type" /></h6>
                                        <tr>

                                            <th>Data</th>
                                        </tr>
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="data" />
                                                </td>                                     
                                            </tr>
                                                        </table>
                                        </xsl:for-each>
                                </xsl:for-each>







                    
                                <h4>RESPONSE</h4>
                                <xsl:for-each select="response">
                               

                                    <table id="myTable"  cellspacing="0">
                                        <h5>HEADERS</h5>
                                        <tr>
                                            <th>Name</th>
                                            <th>Value</th>
                                        </tr>
                                            <xsl:for-each select="headers">
                                            <xsl:for-each select="./*">
                                                <tr>
                                                <td> 
                                                <xsl:value-of select="name()"/>

                                                </td>
                                                <td> 
                                                <xsl:value-of select="."/>
                                                </td>
                                                </tr>
                                           </xsl:for-each>
                                            </xsl:for-each>
                                        
                                    </table>



                                    <xsl:for-each select="body">
                                        <table id="myTable" cellspacing="0" >
                                            <h5>BODY</h5>
                                            <h6>Type:<xsl:value-of select="type" /></h6>
                                            <tr>
                                                
                                                <th>Data</th>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="data" />
                                                </td>                                     
                                            </tr>
                                        </table>
                                    </xsl:for-each>




                                    <table id="myTable" cellspacing="0">
                                        <h5>VARIABLES</h5>
                                        <tr>
                                            <th>Name</th>
                                            <th>Path</th>
                                            <th>Value</th>
                                        </tr>
                                        <xsl:for-each select="variables">
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="name" />
                                                </td>
                                                <td>
                                                    <xsl:value-of select="value" />
                                                </td>
                                                <td>
                                                    <xsl:value-of select="runTimevalue" />
                                                </td>                                      
                                            </tr>
                                        </xsl:for-each>
                                    </table>




                                    <h4>ASSERTION</h4>

                                    <xsl:for-each select="assertions">
                                        <table id="myTable" cellspacing="0">
                                            <h5>STATUS</h5>
                                            <tr>
                                                <th>Name</th>
                                                <th>Value</th>
                                                <th>Condition</th>
                                                <th>Status</th>
                                            </tr>
                                            <xsl:for-each select="status">
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="name" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="value" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="condition" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="status" />
                                                    </td>                                       
                                                </tr>
                                            </xsl:for-each>
                                        </table>



                                        <table id="myTable" cellspacing="0">
                                            <h5>HEADERS</h5>
                                            <tr>
                                                <th>Name</th>
                                                <th>Value</th>
                                                <th>Condition</th>
                                                <th>Status</th>
                                            </tr>
                                            <xsl:for-each select="headers">
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="name" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="value" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="condition" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="status" />
                                                    </td>                                   
                                                </tr>
                                            </xsl:for-each>
                                        </table>


                                        <table id="myTable" cellspacing="0">
                                            <h5>BODY</h5>
                                            <tr>
                                                <th>Path</th>
                                                <th>Value</th>
                                                <th>Condition</th>
                                                <th>Status</th>


                                            </tr>
                                            <xsl:for-each select="body">
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="path" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="value" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="condition" />
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="status" />
                                                    </td>

                                                    </tr>
                                            </xsl:for-each>
                                        </table>
                                         
                                    </xsl:for-each>
                                </xsl:for-each>






                            </xsl:for-each>
                    </xsl:for-each>
                    </xsl:for-each>
                </body>
            </html>

    </xsl:template>
</xsl:stylesheet>