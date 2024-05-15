<#import 'timelineitems/_appointmentTimelineItem.ftl' as _appointmentTimelineItem>
<#import 'timelineitems/_terminationTimelineItem.ftl' as _terminationTimelineItem>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro timelineItem timelineItemView>
  <#if timelineItemView.timelineEventType().name() == "APPOINTMENT">
    <@_appointmentTimelineItem.appointmentTimelineItem timelineItemView/>
  </#if>
<#if timelineItemView.timelineEventType().name() == "TERMINATION">
    <@_terminationTimelineItem.terminationTimelineItem timelineItemView/>
</#if>
</#macro>