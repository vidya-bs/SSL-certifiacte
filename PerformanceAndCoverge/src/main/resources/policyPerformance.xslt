<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:saxon="http://saxon.sf.net/" exclude-result-prefixes="saxon">

    <xsl:template match="/">
			<Debug>
            <xsl:for-each select="Data/Point[@id='Execution']">
                <xsl:for-each select="DebugInfo/Properties">
					<xsl:if test="((Property[@name='stepDefinition-enabled'] ='true') and not(Property[@name='expression']) )or  
								((Property[@name='stepDefinition-enabled'] ='true') and (Property[@name='expression']) and (Property[@name='expressionResult'] ='true'))">
                    
                            <stepName>
								<xsl:for-each select="../../VariableAccess/Set">
								<xsl:if test="matches(@name,'apigee\.metrics\.policy\S+\.timeTaken')">
									<timeTaken><xsl:value-of select="format-number(@value div 10000000,'#0.###')"/></timeTaken>
								</xsl:if>
								</xsl:for-each>
									<value><xsl:value-of select="current()/Property[@name='stepDefinition-name']"/></value>
									<stepType><xsl:value-of select="current()/Property[@name='type']"/></stepType>
                            </stepName>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
			</Debug>
    </xsl:template>
</xsl:stylesheet>