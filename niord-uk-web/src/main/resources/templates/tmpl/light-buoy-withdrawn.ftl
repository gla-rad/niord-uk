<#include "aton-common.ftl"/>

<#assign durationDa=(params.duration??)?then(getListValue(params.duration, '', 'normal', 'da'), '')/>
<#assign durationEn=(params.duration??)?then(getListValue(params.duration, '', 'normal', 'en'), '')/>

<@aton
    daDefaultName="Lystønden"
    daDetails="er ${durationDa} inddraget"
    enDefaultName="The light buoy"
    enDetails="has been ${durationEn} withdrawn"
    enNavtex="${durationEn} WITHDRAWN"
    />
