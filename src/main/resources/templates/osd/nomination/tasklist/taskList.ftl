<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView>" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

<#assign pageTitle = "${serviceBranding.mnemonic()} operator nomination" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <@taskList.standardTaskList taskListSections=taskListSections />
</@defaultPage>