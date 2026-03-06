<#include '../layout/layout.ftl'>
<#import 'roleDescriptions.ftl' as roleDescriptions>

<#-- @ftlvariable name="teamMemberView" type="uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamMemberView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<@defaultPage
  htmlTitle="Edit member roles"
  pageHeading=""
  pageSize=PageSize.TWO_THIRDS_COLUMN
  errorItems=errorList
  backLinkUrl=springUrl(backLinkUrl)
>
    <@fdsForm.htmlForm>

        <#assign warning>
            <#if !userHasAllowedEmail>
                <@fdsWarning.warning>
                  This user's email is not from an approved domain for this team.
                </@fdsWarning.warning>
            </#if>
        </#assign>

        <@fdsCheckbox.checkboxes
          fieldsetHeadingText="What actions does ${teamMemberView.getDisplayName()} perform?"
          hintText=warning
          fieldsetHeadingSize="h1"
          fieldsetHeadingClass="govuk-fieldset__legend--l"
          path="form.roles"
          checkboxes=rolesNamesMap
        />

        <@roleDescriptions.roleDescriptions roles=rolesInTeam/>

        <@fdsAction.submitButtons
          primaryButtonText="Save and continue"
          secondaryLinkText="Cancel"
          linkSecondaryAction=true
          linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>
</@defaultPage>