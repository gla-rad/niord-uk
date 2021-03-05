<#include "common.ftl"/>
<#include "work-vessel.ftl"/>

<#macro obstructionWork daDetails="" enDetails="" daAudio="" enNavtex="">

    <@defaultSubjectFieldTemplates/>

    <field-template field="part.getDesc('da').details" format="html">
        <@renderDateIntervals dateIntervals=part.eventDates lang="da" capFirst=true/>
        ${daDetails}
        <@renderPositionList geomParam=part lang="da"/>.
        <@renderWorkVessel vessel=params.vessel! lang="da" format="details"/>
    </field-template>

    <field-template field="part.getDesc('en').details" format="html">
        <@renderDateIntervals dateIntervals=part.eventDates lang="en" tz="UTC"/>
        ${enDetails}
        <@renderPositionList geomParam=part lang="en"/>.
        <@renderWorkVessel vessel=params.vessel! lang="en" format="details"/>
    </field-template>

    <#if promulgate('audio')>
        <field-template field="message.promulgation('audio').text" update="append">
            <@line>
                <@renderDateIntervals dateIntervals=part.eventDates format="plain" lang="da" capFirst=true/>
                ${daAudio}
                <@renderPositionList geomParam=part lang="da" format="audio"/>.
                <@renderWorkVessel vessel=params.vessel! lang="da" format="audio"/>
            </@line>
        </field-template>
    </#if>

    <#if promulgate('navtex')>
        <field-template field="message.promulgation('navtex').text" update="append">
            <@line format="navtex">
                <@renderDateIntervals dateIntervals=part.eventDates lang="en" format="navtex"/>
                ${enNavtex}
                <@renderPositionList geomParam=part lang="en" format="navtex"/>.
                <@renderWorkVessel vessel=params.vessel! lang="en" format="navtex"/>
            </@line>
        </field-template>
    </#if>

</#macro>
