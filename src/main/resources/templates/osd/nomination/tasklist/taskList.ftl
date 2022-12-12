<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->

<#assign pageTitle = "${serviceBranding.mnemonic()} operator nomination" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>
  <@fdsAction.link
    linkText="Delete nomination"
    linkClass="govuk-button govuk-button--secondary"
    linkUrl=springUrl(deleteNominationUrl)
  />
  <@taskList.standardTaskList taskListSections=taskListSections />
</@defaultPage>