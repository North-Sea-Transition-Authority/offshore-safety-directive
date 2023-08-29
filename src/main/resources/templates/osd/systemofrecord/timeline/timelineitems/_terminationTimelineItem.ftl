<#import '../../../../fds/components/timeline/timeline.ftl' as fdsTimeline>
<#import '../../../../fds/components/dataItems/dataItems.ftl' as fdsDataItems>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro terminationTimelineItem timelineItemView>
    <#assign modelProperties = timelineItemView.assetTimelineModelProperties().modelProperties/>
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
        <@fdsDataItems.dataItem dataItemListClasses="govuk-body__preserve-whitespace">
            <@fdsDataItems.dataValues key="Reason for termination" value=modelProperties["reasonForTermination"]/>
        </@fdsDataItems.dataItem>
      </@fdsTimeline.timelineEvent>
    </@fdsTimeline.timelineTimeStamp>
</#macro>