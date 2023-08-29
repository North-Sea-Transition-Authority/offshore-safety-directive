<#import '../../../../fds/components/timeline/timeline.ftl' as fdsTimeline>
<#import '../../../../fds/components/dataItems/dataItems.ftl' as fdsDataItems>
<#import '../../../files/fileSummary.ftl' as fileSummary>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro terminationTimelineItem timelineItemView>
    <#assign modelProperties = timelineItemView.assetTimelineModelProperties().modelProperties/>
    <#assign reason>
        <#if modelProperties["reasonForTermination"]?has_content>
            <pre class="govuk-body">${modelProperties["reasonForTermination"]}</pre>
        </#if>
    </#assign>

    <@fdsTimeline.timelineTimeStamp
        timeStampHeading=timelineItemView.title()
        nodeNumber=""
    >
        <@fdsTimeline.timelineEvent>
            <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                <@fdsDataItems.dataValues key="Termination date" value=modelProperties["terminationDate"]/>
                <#if modelProperties["terminatedBy"]?has_content>
                    <@fdsDataItems.dataValues key="Terminated by" value=modelProperties["terminatedBy"]/>
                    <@fdsDataItems.dataValues key="" value=""/>
                </#if>
            </@fdsDataItems.dataItem>
            <#if modelProperties["reasonForTermination"]?has_content>
                <@fdsDataItems.dataItem dataItemListClasses="govuk-body__preserve-whitespace govuk-!-margin-bottom-0">
                    <@fdsDataItems.dataValues key="Reason for termination" value=reason/>
                </@fdsDataItems.dataItem>
            </#if>
            <#if modelProperties["terminationFiles"]?has_content>
                <h4 class="govuk-heading-s">Supporting documents</h4>
                <@fileSummary.fileSummary modelProperties["terminationFiles"]/>
            </#if>
        </@fdsTimeline.timelineEvent>
    </@fdsTimeline.timelineTimeStamp>
</#macro>