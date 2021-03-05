<?xml version="1.0" encoding="UTF-8"?>

<#assign htmlToText = "org.niord.core.script.directive.HtmlToTextDirective"?new()>
<#assign id='aton.uk.' + (atonUID)?lower_case/>
<#assign mrn='urn:mrn:iho:aton:uk:' + (atonUID)?lower_case/>
<#assign geomId=0>
<#setting time_zone="UTC">

<S125:DataSet xmlns:S125="http://www.iho.int/S125/gml/1.0"
              xsi:schemaLocation="http://www.iho.int/S125/gml/1.0 S125.xsd"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xmlns:gml="http://www.opengis.net/gml/3.2"
              xmlns:S100="http://www.iho.int/s100gml/1.0"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              gml:id="${id}">

    <#if bbox??>
        <gml:boundedBy>
            <gml:Envelope srsName="EPSG:4326">
                <gml:lowerCorner>${bbox[1]} ${bbox[0]}</gml:lowerCorner>
                <gml:upperCorner>${bbox[3]} ${bbox[2]}</gml:upperCorner>
            </gml:Envelope>
        </gml:boundedBy>
    </#if>

    <member>
        <S125:S125_NavAidStructure>
             <@generateNavAidStructure aton=aton></@generateNavAidStructure>
        </S125:S125_NavAidStructure>
    </member>

</S125:DataSet>


<#function descForLang entity lang=language >
    <#if entity.descs?has_content>
        <#list entity.descs as desc>
            <#if desc.lang?? && desc.lang == lang>
                <#return desc />
            </#if>
        </#list>
    </#if>
    <#if entity.descs?has_content>
        <#return entity.descs[0] />
    </#if>
</#function>


<#function lang lang=language!'en'>
    <#switch lang>
        <#case "de">
            <#return 'deu' />
            <#break>
        <#case "es">
            <#return 'spa' />
            <#break>
        <#case "pt">
            <#return 'por' />
            <#break>
        <#case "fr">
            <#return 'fra' />
            <#break>
        <#case "ru">
            <#return 'rus' />
            <#break>
        <#case "hi">
            <#return 'hin' />
            <#break>
        <#case "zn">
            <#return 'zho' />
            <#break>
        <#default>
            <#return 'eng' />
            <#break>
    </#switch>
</#function>


<#function getTag aton tagName>
    <#if aton.tags?map(t -> t.k)?seq_contains(tagName)>
        <#return aton.tags?filter(t -> t.k == tagName)?map(t -> t.v)?first!" " />
    <#else>
        <#return null />
    </#if>
</#function>


<#function nextGeomId>
    <#assign geomId=geomId + 1>
    <#return 'G.${id}.${geomId?c}' />
</#function>


<#macro generateNavAidStructure aton>
    <@generateFeatureName aton=aton />
    <@generateGeometry g=geometry />
    <@generateDateRange aton=aton />
    <@generateAtonType aton=aton />
    <@generateTypeOfVDEMessage aton=aton />
    <@generateTypeOfEPDF aton=aton />
    <@generateRAIMFlag aton=aton />
    <@generateVAtoNFlag aton=aton />
</#macro>


<#macro generateFeatureName aton>
    <#if aton?? && aton.tags?? && aton.tags?has_content>
        <#assign atonName=getTag(aton, 'seamark:name')!>
        <featureName>
            <displayName>true</displayName>
            <language>${lang('en')}</language>
            <name>${atonName}</name>
        </featureName>
    </#if>
</#macro>


<#macro generateGeometry g>
    <#switch g.type!''>
        <#case "Point">
            <@generatePoint coords=g.coordinates></@generatePoint>
            <#break>
        <#case "MultiPoint">
            <#list g.coordinates as coords>
                <@generatePoint coords=coords></@generatePoint>
            </#list>
            <#break>
        <#case "LineString">
            <@generateCurve coords=g.coordinates></@generateCurve>
            <#break>
        <#case "MultiLineString">
            <#list g.coordinates as coords>
                <@generateCurve coords=coords></@generateCurve>
            </#list>
            <#break>
        <#case "Polygon">
            <@generateSurface coords=g.coordinates></@generateSurface>
            <#break>
        <#case "MultiPolygon">
            <#list g.coordinates as coords>
                <@generateSurface coords=coords></@generateSurface>
            </#list>
            <#break>
        <#case "GeometryCollection">
            <#list g.geometries as geom>
                <@generateGeometry g=geom></@generateGeometry>
            </#list>
            <#break>
    </#switch>
</#macro>


<#macro generateMRN aton>
    <#if mrn??>
        <maritimeResourceName>${mrn}</maritimeResourceName>
    </#if>
</#macro>


<#macro generateDateRange aton>
    <#if aton.dateRange?? && aton.dateRange?has_content>
        <#list aton.dateRange as date>
            <#assign allDay=date.allDay?? && date.allDay == true />
            <fixedDateRange>
                <#if date.fromDate?? && !allDay>
                    <timeOfDayStart>${date.fromDate?string["HH:mm:ss"]}Z</timeOfDayStart>
                </#if>
                <#if date.toDate?? && !allDay>
                    <timeOfDayEnd>${date.toDate?string["HH:mm:ss"]}Z</timeOfDayEnd>
                </#if>
                <#if date.fromDate??>
                    <dateStart>
                        <date>${date.fromDate?string["yyyy-MM-dd"]}</date>
                    </dateStart>
                </#if>
                <#if date.toDate??>
                    <dateEnd>
                        <date>${date.toDate?string["yyyy-MM-dd"]}</date>
                    </dateEnd>
                </#if>
            </fixedDateRange>
        </#list>
    </#if>
</#macro>


<#macro generateAtonType aton>
    <#if aton?? && aton.tags?? && aton.tags?has_content>
        <#assign atonType=getTag(aton, "seamark:type")!>
        <#switch atonType>
            <#case "beacon_cardinal">
                <#assign atonCategory=getTag(aton, "seamark:beacon_cardinal:category")!>
                <#switch atonCategory>
                    <#case "north">
                        <atonType>Beacon, Cardinal N</atonType>
                        <#break>
                    <#case "south">
                        <atonType>Beacon, Cardinal S</atonType>
                        <#break>
                    <#case "east">
                        <atonType>Beacon, Cardinal E</atonType>
                        <#break>
                    <#case "west">
                        <atonType>Beacon, Cardinal S</atonType>
                        <#break>
                </#switch>
                <#break>
            <#case "beacon_isolated_danger">
                <atonType>Beacon, Isolated danger</atonType>
                <#break>
            <#case "beacon_lateral">
                <#assign atonCategory=getTag(aton, "seamark:beacon_lateral:category")!>
                <#switch atonCategory>
                    <#case "port">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "starboard">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "preferred_channel_port">
                        <atonType>Preferred Channel Port hand</atonType>
                        <#break>
                    <#case "preferred_channel_starboard">
                        <atonType>Preferred Channel Starboard hand</atonType>
                        <#break>
                    <#case "channel_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "waterway_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "danger_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "harbour_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "channel_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "waterway_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "danger_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "harbour_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "channel_separation">
                        <atonType>Reference Point</atonType>
                        <#break>
                    <#case "waterway_separation">
                        <atonType>Reference Point</atonType>
                        <#break>
                    <#case "bridge_pier">
                        <atonType>Fixed Off Shore</atonType>
                        <#break>
                </#switch>
                <#break>
            <#case "beacon_safe_water">
                <atonType>Beacon, Safe water</atonType>
                <#break>
            <#case "beacon_special_purpose">
                <atonType>Beacon, Special mark</atonType>
                <#break>
            <#case "buoy_cardinal">
                <#assign atonCategory=getTag(aton, "seamark:buoy_cardinal:category")!>
                <#switch atonCategory>
                    <#case "north">
                        <atonType>Cardinal Mark N</atonType>
                        <#break>
                    <#case "south">
                        <atonType>Cardinal Mark S</atonType>
                        <#break>
                    <#case "east">
                        <atonType>Cardinal Mark E</atonType>
                        <#break>
                    <#case "west">
                        <atonType>Cardinal Mark W</atonType>
                        <#break>
                </#switch>
                <#break>
            <#case "buoy_installation">
                <atonType>Special Mark</atonType>
                <#break>
            <#case "buoy_lateral">
                <#assign atonCategory=getTag(aton, "seamark:buoy_lateral:category")!>
                <#switch atonCategory>
                    <#case "port">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "starboard">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "preferred_channel_port">
                        <atonType>Preferred Channel Port hand</atonType>
                        <#break>
                    <#case "preferred_channel_starboard">
                        <atonType>Preferred Channel Starboard hand</atonType>
                        <#break>
                    <#case "channel_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "waterway_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "danger_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "harbour_left">
                        <atonType>Port hand Mark</atonType>
                        <#break>
                    <#case "channel_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "waterway_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "danger_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "harbour_right">
                        <atonType>Starboard hand Mark</atonType>
                        <#break>
                    <#case "channel_separation">
                        <atonType>Reference Point</atonType>
                        <#break>
                    <#case "waterway_separation">
                        <atonType>Reference Point</atonType>
                        <#break>
                    <#case "bridge_pier">
                        <atonType>Fixed Off Shore</atonType>
                        <#break>
                </#switch>
                <#break>
            <#case "buoy_isolated_danger">
                <atonType>Isolated danger</atonType>
                <#break>
            <#case "buoy_safe_water">
                <atonType>Safe Water</atonType>
                <#break>
            <#case "buoy_special_purpose">
                <atonType>Special Mark</atonType>
                <#break>
            <#case "light">
                <#assign atonCategory=getTag(aton, "seamark:light:category")!>
                <#switch atonCategory>
                    <#case "rear">
                        <atonType>Leading Light Front</atonType>
                        <#break>
                    <#case "leading">
                        <atonType>Leading Light Rear</atonType>
                        <#break>
                    <#case "bearing">
                        <atonType>Light, With sectors</atonType>
                        <#break>
                    <#case "directional">
                        <atonType>Light, With sectors</atonType>
                        <#break>
                    <#default>
                        <atonType>Light, Without sectors</atonType>
                </#switch>
                <#break>
            <#case "light_vessel">
                <atonType>Light Vessel/LANBY/Rigs</atonType>
                <#break>
            <#case "virtual_aton">
                <#assign atonCategory=getTag(aton, "seamark:virtual_aton:category")!>
                <#switch atonCategory>
                    <#case "north_cardinal">
                        <atonType>Cardinal Mark N</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "east_cardinal">
                        <atonType>Cardinal Mark E</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "south_cardinal">
                        <atonType>Cardinal Mark S</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "west_cardinal">
                        <atonType>Cardinal Mark W</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "port_lateral">
                        <atonType>Port hand Mark</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "starboard_lateral">
                        <atonType>Starboard hand Mark</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "preferred_port">
                        <atonType>Preferred Channel Port hand</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "preferred_starboard">
                        <atonType>Preferred Channel Starboard hand</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "isolated_danger">
                        <atonType>Isolated danger</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "safe_water">
                        <atonType>Safe Water"</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "special_purpose">
                        <atonType>Special Mark</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#case "wreck">
                        <atonType>Emergency Wreck Marking Buoy</atonType>
                        <deploymentType>Mobile</deploymentType>
                        <#break>
                    <#default>
                        <atonType>Default</atonType>
                        <deploymentType>Mobile</deploymentType>
                </#switch>
                <#break>
            <#default>
                <atonType>Default</atonType>
        </#switch>
    </#if>
</#macro>


<#macro generateTypeOfVDEMessage aton>
    <#if aton?? && aton.typeOfVdeMessage??>
        <typeOfVDEMessage>${aton.typeOfVdeMessage}</typeOfVDEMessage>
    </#if>
</#macro>


<#macro generateTypeOfEPDF aton>
    <#if aton?? && aton.typeOfEpdf??>
        <typeOfEPDF>${aton.typeOfEpdf}</typeOfEPDF>
    </#if>
</#macro>


<#macro generateRAIMFlag aton>
    <#if aton?? && aton.raim??>
        <raimFlag>true</raimFlag>
    <#else>
        <raimFlag>false</raimFlag>
    </#if>
</#macro>


<#macro generateVAtoNFlag aton>
    <#if aton?? && aton.tags?? && aton.tags?has_content && getTag(aton, "seamark:type") == "virtual_aton">
        <vatonFlag>true</vatonFlag>
    <#else>
        <vatonFlag>false</vatonFlag>
    </#if>
</#macro>


<#macro generatePoint coords>
    <#if coords?? && coords?size gt 1>
        <geometry>
            <S100:pointProperty>
                <S100:Point gml:id="${nextGeomId()}" srsName="EPSG:4326">
                    <gml:pos><@generateCoordinates coords=[coords] /></gml:pos>
                </S100:Point>
            </S100:pointProperty>
        </geometry>
    </#if>
</#macro>


<#macro generateCurve coords>
    <#if coords?? && coords?size gt 1>
        <geometry>
            <S100:curveProperty>
                <S100:Curve gml:id="${nextGeomId()}" srsName="EPSG:4326">
                    <gml:segments>
                        <gml:LineStringSegment>
                            <gml:posList><@generateCoordinates coords=coords /></gml:posList>
                        </gml:LineStringSegment>
                    </gml:segments>
                </S100:Curve>
            </S100:curveProperty>
        </geometry>
    </#if>
</#macro>


<#macro generateSurface coords>
    <#if coords?? && coords?size gt 0>
        <geometry>
            <S100:surfaceProperty>
                <S100:Surface gml:id="${nextGeomId()}" srsName="EPSG:4326">
                    <gml:patches>
                        <gml:PolygonPatch>
                            <#list coords as linearRing>
                                <#if linearRing?is_first>
                                    <gml:exterior>
                                        <gml:LinearRing>
                                            <gml:posList><@generateCoordinates coords=linearRing /></gml:posList>
                                        </gml:LinearRing>
                                    </gml:exterior>
                                <#else>
                                    <gml:interior>
                                        <gml:LinearRing>
                                            <gml:posList><@generateCoordinates coords=linearRing /></gml:posList>
                                        </gml:LinearRing>
                                    </gml:interior>
                                </#if>
                            </#list>
                        </gml:PolygonPatch>
                    </gml:patches>
                </S100:Surface>
            </S100:surfaceProperty>
        </geometry>
    </#if>
</#macro>


<#macro generateCoordinates coords>
    <#list coords as lonLat>
        ${lonLat[1]?trim} ${lonLat[0]?trim}<#t>
    </#list>
</#macro>