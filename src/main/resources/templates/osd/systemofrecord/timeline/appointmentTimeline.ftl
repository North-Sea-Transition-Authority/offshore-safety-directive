<#include '../layout.ftl'>
<#import '_timelineItem.ftl' as _timelineItem>

<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="timelineItemViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView>" -->
<#-- @ftlvariable name="assetTypeDisplayNameSentenceCase" type="String" -->

<@systemOfRecordPage
  pageTitle=assetName
  pageSize=PageSize.FULL_COLUMN
  backLinkWithBrowserBack=true
>
  <#if newAppointmentUrl?has_content>
    <@fdsAction.link
      linkText="Add appointment"
      linkClass="govuk-button"
      linkUrl=springUrl(newAppointmentUrl)
    />
  </#if>
  <#if timelineItemViews?has_content>
    <@fdsTimeline.timeline>
      <@fdsTimeline.timelineSection>
        <#list timelineItemViews as timelineItemView>
          <@_timelineItem.timelineItem timelineItemView/>
        </#list>
      </@fdsTimeline.timelineSection>
    </@fdsTimeline.timeline>
  <#else>
    <@fdsInsetText.insetText insetTextClass="govuk-inset-text--blue">
      No operator history is available for this ${assetTypeDisplayNameSentenceCase}
    </@fdsInsetText.insetText>
  </#if>
</@systemOfRecordPage>