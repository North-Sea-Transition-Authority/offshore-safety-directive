<#import '../../fds/components/checkboxes/checkboxes.ftl' as fdsCheckbox>

<#macro teamMemberRoles rolesFormPath roleCheckBoxItems fieldsetHeadingText>
  <@fdsCheckbox.checkboxes
    fieldsetHeadingText=fieldsetHeadingText
    fieldsetHeadingSize="h1"
    fieldsetHeadingClass="govuk-fieldset__legend--l"
    path=rolesFormPath
    checkboxes=roleCheckBoxItems
  />
</#macro>