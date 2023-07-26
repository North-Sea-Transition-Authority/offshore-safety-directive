<#import 'timelineitems/_appointmentTimelineItem.ftl' as _appointmentTimelineItem>

<#-- @ftlvariable name="item" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView" -->

<#macro timelineItem timelineItemView>
  <#if timelineItemView.timelineEventType().name() == "APPOINTMENT">
    <@_appointmentTimelineItem.appointmentTimelineItem timelineItemView/>
  </#if>
</#macro>