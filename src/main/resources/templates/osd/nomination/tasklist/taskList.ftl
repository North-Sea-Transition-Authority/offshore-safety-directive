<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="deleteNominationButtonPrompt" type="String" -->

<#assign pageTitle = "${serviceBranding.mnemonic()} operator nomination" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>

  <#if reasonForUpdate?has_content>
    <@fdsDetails.summaryDetails summaryTitle="What information have I been asked to update?">
      <p class="govuk-body govuk-body__preserve-whitespace">${reasonForUpdate}</p>
    </@fdsDetails.summaryDetails>
  </#if>

  <#if deleteNominationUrl?has_content>
    <@fdsAction.link
      linkText=deleteNominationButtonPrompt
      linkClass="govuk-button govuk-button--secondary"
      linkUrl=springUrl(deleteNominationUrl)
    />
  </#if>

  <@taskList.standardTaskList taskListSections=taskListSections />
</@defaultPage>