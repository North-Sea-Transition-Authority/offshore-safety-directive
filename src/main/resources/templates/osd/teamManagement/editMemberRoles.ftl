<#include '../layout/layout.ftl'>
<#import 'roleDescriptions.ftl' as roleDescriptions>

<#-- @ftlvariable name="teamMemberView" type="uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamMemberView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<#assign pageHeading = "What actions does ${teamMemberView.getDisplayName()} perform?"/>

<@defaultPage htmlTitle=pageHeading pageHeading="" errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsCheckbox.checkboxes
      fieldsetHeadingText=pageHeading
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