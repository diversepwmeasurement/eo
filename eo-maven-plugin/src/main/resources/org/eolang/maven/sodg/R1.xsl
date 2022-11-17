<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2016-2022 Objectionary.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eo="https://www.eolang.org" xmlns:xs="http://www.w3.org/2001/XMLSchema" id="R1.1" version="2.0">
  <!--
  Here we find all objects that have @base attributes that don't
  start with a dot. Then we make sure their bases exist in the graph
  and are bound to the root.
  -->
  <xsl:import href="/org/eolang/maven/sodg/_macros.xsl"/>
  <xsl:output encoding="UTF-8" method="xml"/>
  <xsl:template match="/program/sodg">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
      <xsl:apply-templates select="/program/objects//o" mode="sodg"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="o[@base and not(starts-with(@base, '.'))]" mode="sodg" priority="1">
    <xsl:variable name="o" select="."/>
    <xsl:variable name="fqn" select="eo:fqn(/program, @base)"/>
    <xsl:for-each select="tokenize($fqn, '\.')">
      <xsl:variable name="k" select="."/>
      <xsl:variable name="p" select="position()"/>
      <xsl:variable name="locator">
        <xsl:for-each select="tokenize($fqn, '\.')">
          <xsl:if test="position() &lt;= $p">
            <xsl:if test="position() &gt; 1">
              <xsl:text>.</xsl:text>
            </xsl:if>
            <xsl:value-of select="."/>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="not($o/preceding::o[@base = $locator or starts-with(@base, concat($locator, '.'))])">
        <xsl:variable name="parent">
          <xsl:for-each select="tokenize($fqn, '\.')">
            <xsl:if test="position() &lt; $p">
              <xsl:if test="position() &gt; 1">
                <xsl:text>.</xsl:text>
              </xsl:if>
              <xsl:value-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        <xsl:call-template name="i">
          <xsl:with-param name="name" select="'ADD'"/>
          <xsl:with-param name="args" as="item()*">
            <xsl:sequence>
              <xsl:value-of select="$locator"/>
            </xsl:sequence>
          </xsl:with-param>
          <xsl:with-param name="comment">
            <xsl:value-of select="position()"/>
            <xsl:text>th part of the '</xsl:text>
            <xsl:value-of select="$fqn"/>
            <xsl:text>' FQN, because '</xsl:text>
            <xsl:value-of select="$locator"/>
            <xsl:text>' has not been seen yet</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="i">
          <xsl:with-param name="name" select="'BIND'"/>
          <xsl:with-param name="args" as="item()*">
            <xsl:sequence>
              <xsl:choose>
                <xsl:when test="$parent = ''">
                  <xsl:text>ν0</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$parent"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:sequence>
            <xsl:sequence>
              <xsl:value-of select="$locator"/>
            </xsl:sequence>
            <xsl:sequence>
              <xsl:value-of select="$k"/>
            </xsl:sequence>
          </xsl:with-param>
          <xsl:with-param name="comment">
            <xsl:text>Link to the </xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text>th part of the '</xsl:text>
            <xsl:value-of select="$fqn"/>
            <xsl:text>' FQN</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="o" mode="sodg">
    <!-- ignore it -->
  </xsl:template>
  <xsl:template match="node()|@*" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="#current"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
