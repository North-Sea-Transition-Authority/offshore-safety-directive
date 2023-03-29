<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="htmlTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="teamMember" type="uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView" -->
<#-- @ftlvariable name="removeUrl" type="java.lang.String" -->
<#-- @ftlvariable name="canRemoveTeamMember" type="java.lang.Boolean" -->
<#-- @ftlvariable name="singleErrorMessage" type="java.lang.String" -->

<@defaultPage
    htmlTitle=htmlTitle
    pageHeading=pageTitle
    errorItems=[]
    pageSize=PageSize.TWO_THIRDS_COLUMN
    backLinkUrl=springUrl(backLinkUrl)
    singleErrorMessage=singleErrorMessage!""
>

    <#if !canRemoveTeamMember>
      <@fdsWarning.warning>
          <p class="govuk-body">
            You cannot remove ${teamMember.displayName} as they are the last access manager within their team.
          </p>
          <@fdsDetails.details
              detailsTitle="How can I remove this user?"
              detailsText="If you wish to remove this user, you must give another team member the Access Manager role."
          />
      </@fdsWarning.warning>
    </#if>

    <@fdsSummaryList.summaryList>
        <#if teamMember.contactEmail()?has_content>
          <@fdsSummaryList.summaryListRowNoAction keyText="Email address">${teamMember.contactEmail()}</@fdsSummaryList.summaryListRowNoAction>
        </#if>
        <#if teamMember.contactNumber()?has_content>
            <@fdsSummaryList.summaryListRowNoAction keyText="Telephone number">${teamMember.contactNumber()}</@fdsSummaryList.summaryListRowNoAction>
        </#if>
        <@fdsSummaryList.summaryListRowNoAction keyText="Roles">
          <ul class="govuk-list govuk-!-margin-bottom-0">
              <#list teamMember.teamRoles() as role>
                <li>${role.screenDisplayText}</li>
              </#list>
          </ul>
        </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryList>

    <@fdsForm.htmlForm actionUrl=springUrl(removeUrl)>
        <@fdsAction.buttonGroup>
            <#if canRemoveTeamMember>
              <@fdsAction.button buttonText="Remove" buttonClass="govuk-button govuk-button--warning"/>
              <@fdsAction.link linkText="Cancel" linkUrl=springUrl(backLinkUrl) linkClass="fds-link-button"/>
            <#else>
              <@fdsAction.link linkText="Back to ${teamName}" linkUrl=springUrl(backLinkUrl) linkClass="fds-link-button"/>
            </#if>
        </@fdsAction.buttonGroup>
    </@fdsForm.htmlForm>

</@defaultPage>
