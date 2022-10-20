<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamRoles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole>" -->
<#-- @ftlvariable name="teamMembers" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#include '../layout/layout.ftl'>
<#import '_teamMembers.ftl' as teamMembersMacro>
<#import '_roleDescriptions.ftl' as roleDescriptions>

<#assign pageTitle=teamName/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.FULL_WIDTH
>

  <@roleDescriptions.roleDescriptions roles=teamRoles/>

  <#if teamMembers?has_content>
      <@teamMembersMacro.teamMembers name=teamName members=teamMembers/>
  <#else>
    <@fdsInsetText.insetText>
      ${teamName} has no members.
    </@fdsInsetText.insetText>
  </#if>

</@defaultPage>