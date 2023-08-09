<#include '../../layout/layout.ftl'>
<#import '_timelineItem.ftl' as _timelineItem>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="timelineItemViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView>" -->
<#-- @ftlvariable name="assetTypeDisplayNameSentenceCase" type="String" -->

<@defaultPage
  htmlTitle=assetName
  pageHeading=assetName
  pageHeadingCaption=assetTypeDisplayName
  errorItems=[]
  pageSize=PageSize.FULL_COLUMN
  backLinkWithBrowserBack=true
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>
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
</@defaultPage>