<#import '../../../fds/components/timeline/timeline.ftl' as fdsTimeline>

<#macro correctionHistory correctionHistoryViews timelineSectionHeading="Correction history">
  <#if correctionHistoryViews?size gte 1>
    <@fdsTimeline.timeline>
      <@fdsTimeline.timelineSection sectionHeading=timelineSectionHeading>
        <#list correctionHistoryViews as historyItem>

          <#assign timelineClasses>
            <#if !(historyItem?has_next)>
              ${" fds-timeline__time-stamp--no-border govuk-!-padding-bottom-0 "}
            </#if>
          </#assign>

          <@fdsTimeline.timelineTimeStamp
            timeStampHeading=historyItem.formattedCreatedDatetime()
            nodeNumber=""
            timeStampHeadingHint=historyItem.createdBy()
            timeStampClass=timelineClasses
          >
            <@fdsTimeline.timelineEvent>
              <p class="govuk-body govuk-body__preserve-whitespace">${historyItem.reason()}</p>
            </@fdsTimeline.timelineEvent>
          </@fdsTimeline.timelineTimeStamp>
        </#list>
      </@fdsTimeline.timelineSection>
    </@fdsTimeline.timeline>
  <#else>
    <p class="govuk-body">
      This appointment has not been corrected
    </p>
  </#if>
</#macro>