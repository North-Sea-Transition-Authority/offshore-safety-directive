<#import '../../../../fds/components/timeline/timeline.ftl' as fdsTimeline>
<#import '../../../../fds/components/dataItems/dataItems.ftl' as fdsDataItems>
<#import '../../../files/fileSummary.ftl' as fileSummary>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro terminationTimelineItem timelineItemView>
    <#assign modelProperties = timelineItemView.assetTimelineModelProperties().modelProperties/>
    <#assign reason>
        <pre class="govuk-body">${modelProperties["reasonForTermination"]}</pre>
    </#assign>

    <@fdsTimeline.timelineTimeStamp
      timeStampHeading=timelineItemView.title()
      nodeNumber=""
    >
      <@fdsTimeline.timelineEvent>
          <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
            <@fdsDataItems.dataValues key="Termination date" value=modelProperties["terminationDate"]/>
            <@fdsDataItems.dataValues key="Terminated by" value=modelProperties["terminatedBy"]/>
            <@fdsDataItems.dataValues key="" value=""/>
          </@fdsDataItems.dataItem>
        <@fdsDataItems.dataItem dataItemListClasses="govuk-body__preserve-whitespace govuk-!-margin-bottom-0">
            <@fdsDataItems.dataValues key="Reason for termination" value=reason/>
        </@fdsDataItems.dataItem>
        <#if modelProperties["terminationFiles"]?has_content>
          <h4 class="govuk-heading-s">Supporting documents</h4>
          <@fileSummary.fileSummary modelProperties["terminationFiles"]/>
        </#if>
      </@fdsTimeline.timelineEvent>
    </@fdsTimeline.timelineTimeStamp>
</#macro>