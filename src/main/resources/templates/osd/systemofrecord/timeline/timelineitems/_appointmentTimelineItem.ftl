<#include '../../../util/url.ftl'>
<#import '../../../../fds/components/dataItems/dataItems.ftl' as fdsDataItems>
<#import '../../../../fds/components/timeline/timeline.ftl' as fdsTimeline>
<#import '../../../../fds/components/button/button.ftl' as fdsAction>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro appointmentTimelineItem timelineItemView>
    <#assign modelProperties = timelineItemView.assetTimelineModelProperties().modelProperties/>
    <@fdsTimeline.timelineTimeStamp
        timeStampHeading=timelineItemView.title()
        nodeNumber=""
        timelineActionContent=_timelineActions(modelProperties)
    >
        <@fdsTimeline.timelineEvent>
            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="From" value=modelProperties["appointmentFromDate"].formattedValue()/>
                <@fdsDataItems.dataValues key="To" value=_appointmentToDate(modelProperties)/>
                <@fdsDataItems.dataValues key="Created by" value=_appointmentCreatedByReference(modelProperties)/>
            </@fdsDataItems.dataItem>
            <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="Phases" value=_appointmentPhases(modelProperties)/>
                <@fdsDataItems.dataValues key="" value=""/>
                <@fdsDataItems.dataValues key="" value=""/>
            </@fdsDataItems.dataItem>
        </@fdsTimeline.timelineEvent>
    </@fdsTimeline.timelineTimeStamp>
</#macro>

<#function _appointmentToDate modelProperties>
    <#assign appointmentToDate>
        <#if modelProperties["appointmentToDate"]?has_content && modelProperties["appointmentToDate"].formattedValue()?has_content>
            ${modelProperties["appointmentToDate"].formattedValue()}
        <#else>
            ${"Present"}
        </#if>
    </#assign>
    <#return appointmentToDate/>
</#function>

<#function _appointmentPhases modelProperties>
    <#assign appointmentPhases>
        <#if modelProperties["phases"]?has_content && modelProperties["phases"]?size != 1>
            <ol class="govuk-list govuk-!-margin-bottom-0">
                <#list modelProperties["phases"] as phase>
                    <li class="govuk-list__item">${phase.value()}</li>
                </#list>
            </ol>
        <#else>
            <#list modelProperties["phases"] as phase>
                ${phase.value()}
            </#list>
        </#if>
    </#assign>
    <#return appointmentPhases/>
</#function>

<#function _appointmentCreatedByReference modelProperties>
    <#assign createdByReference>
        <#if modelProperties["nominationUrl"]?has_content>
            <@fdsAction.link
            linkText=modelProperties["createdByReference"]
            linkUrl=springUrl(modelProperties["nominationUrl"])
            openInNewTab=true
            linkClass="govuk-link govuk-link--no-visited-state"
            />
        <#else>
            ${modelProperties["createdByReference"]}
        </#if>
    </#assign>
    <#return createdByReference/>
</#function>

<#function _timelineActions modelProperties>
    <#assign display>
        <#if modelProperties["updateUrl"]?has_content>
            <@fdsAction.link linkText="Update appointment" linkUrl=springUrl(modelProperties["updateUrl"])/>
        </#if>
    </#assign>
    <#return display/>
</#function>