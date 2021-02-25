<#include "common.ftl"/>

<#function computeRadioType statusParam >
    <#if statusParam.ais_status??>
        <#return 'AIS' />
    </#if>
    <#return 'RACON' />
</#function>

<#macro renderStatus statusParam format='long' lang='en'>
    <#assign type=computeRadioType(statusParam)/>
    <#if type == 'AIS'>
        <#assign status=statusParam.ais_status! />
    <#else>
        <#assign status=statusParam.racon_status! />
    </#if>
    <#if status??>
        <#assign desc=descForLang(status, lang)!>
        <#if desc?? && desc.longValue?has_content && format == 'long'>
        ${desc.longValue}
        <#elseif desc?? && desc.value?has_content>
        ${desc.value}
        </#if>
    </#if>
</#macro>

<field-template field="part.getDesc('da').subject" format="text">
    <#list params.positions![] as pos>
        ${computeRadioType(pos)}
        <@renderStatus statusParam=pos format="normal" lang="da"/>.
    </#list>
</field-template>

<field-template field="part.getDesc('en').subject" format="text">
    <#list params.positions![] as pos>
        ${computeRadioType(pos)}
        <@renderStatus statusParam=pos format="normal" lang="en"/>.
    </#list>
</field-template>

<field-template field="part.getDesc('da').details" format="html">
    <#list params.positions![] as pos>
        ${computeRadioType(pos)} på
        <@renderAtonType atonParams=pos defaultName="afmærkning" format="long" lang="da"/>
        <@renderPositionList geomParam=pos lang="da"/>
        <@renderStatus statusParam=pos format="long" lang="da"/>.<br>
    </#list>
</field-template>

<field-template field="part.getDesc('en').details" format="html">
    <#list params.positions![] as pos>
        ${computeRadioType(pos)} on
        <@renderAtonType atonParams=pos defaultName="AtoN" format="long" lang="en"/>
        <@renderPositionList geomParam=pos lang="en"/>
        <@renderStatus statusParam=pos format="long" lang="en"/>.<br>
    </#list>
</field-template>

<#if promulgate('audio')>
    <field-template field="message.promulgation('audio').text" update="append">
        <#list params.positions![] as pos>
            <@line>
                ${computeRadioType(pos)} på
                <@renderAtonType atonParams=pos defaultName="afmærkning" format="long" lang="da"/>
                <@renderPositionList geomParam=pos format="audio" lang="da"/>
                <@renderStatus statusParam=pos format="long" lang="da"/>.
            </@line>
        </#list>
    </field-template>
</#if>

<#if promulgate('navtex')>
    <field-template field="message.promulgation('navtex').text" update="append">
        <#list params.positions![] as pos>
            <@line format="navtex">
                ${computeRadioType(pos)} on
                <@renderAtonType atonParams=pos defaultName="AtoN" format="short" lang="en"/>
                <@renderPositionList geomParam=pos format="navtex" lang="en"/>
                <@renderStatus statusParam=pos format="normal" lang="en"/>.
            </@line>
        </#list>
    </field-template>
</#if>
