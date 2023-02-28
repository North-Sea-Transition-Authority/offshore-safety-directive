<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentView>" -->
<#-- @ftlvariable name="assetTypeDisplayNameSentenceCase" type="String" -->

<@defaultPage
  htmlTitle=assetName
  pageHeading=assetName
  pageHeadingCaption=assetTypeDisplayName
  errorItems=[]
  pageSize=PageSize.FULL_WIDTH
  backLinkWithBrowserBack=true
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>
  <#if appointments?has_content>
    <@fdsTimeline.timeline>
      <@fdsTimeline.timelineSection>
        <#list appointments as appointment>
          <@fdsTimeline.timelineTimeStamp
            timeStampHeading=appointment.appointedOperatorName()
            nodeNumber=""
          >
            <@fdsTimeline.timelineEvent>
              <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="From" value=appointment.appointmentFromDate().formattedValue()/>
                <@fdsDataItems.dataValues key="To" value=_appointmentToDate(appointment)/>
              </@fdsDataItems.dataItem>
            </@fdsTimeline.timelineEvent>
          </@fdsTimeline.timelineTimeStamp>
        </#list>
      </@fdsTimeline.timelineSection>
    </@fdsTimeline.timeline>
  <#else>
    <@fdsInsetText.insetText insetTextClass="govuk-inset-text--blue">
      No operator history is available for this ${assetTypeDisplayNameSentenceCase}
    </@fdsInsetText.insetText>
  </#if>
</@defaultPage>

<#function _appointmentToDate appointment>
  <#assign appointmentToDate>
    <#if appointment.appointmentToDate()?has_content && appointment.appointmentToDate().formattedValue()?has_content>
      ${appointment.appointmentToDate().formattedValue()}
    <#else>
      ${"Present"}
    </#if>
  </#assign>
  <#return appointmentToDate/>
</#function>