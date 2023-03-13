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
  pageSize=PageSize.FULL_COLUMN
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
                <@fdsDataItems.dataValues key="Created by" value=_appointmentCreatedByReference(appointment)/>
              </@fdsDataItems.dataItem>
              <@fdsDataItems.dataItem>
                <@fdsDataItems.dataValues key="Phases" value=_appointmentPhases(appointment)/>
                <@fdsDataItems.dataValues key="" value=""/>
                <@fdsDataItems.dataValues key="" value=""/>
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

<#function _appointmentPhases appointment>
  <#assign appointmentPhases>
    <#if appointment.phases()?has_content && appointment.phases()?size != 1>
      <ol class="govuk-list">
        <#list appointment.phases() as phase>
          <li class="govuk-list__item">${phase.value()}</li>
        </#list>
      </ol>
    <#else>
      <#list appointment.phases() as phase>
        ${phase.value()}
      </#list>
    </#if>
  </#assign>
  <#return appointmentPhases/>
</#function>

<#function _appointmentCreatedByReference appointment>
  <#assign createdByReference>
    <#if appointment.nominationUrl()?has_content>
      <@fdsAction.link
        linkText=appointment.createdByReference()
        linkUrl=springUrl(appointment.nominationUrl())
        openInNewTab=true
        linkClass="govuk-link govuk-link--no-visited-state"
      />
    <#else>
       ${appointment.createdByReference()}
    </#if>
  </#assign>
  <#return createdByReference/>
</#function>