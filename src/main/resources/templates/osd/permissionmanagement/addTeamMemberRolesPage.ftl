<#include '../layout/layout.ftl'>
<#import '_teamMemberRoles.ftl' as teamMemberRoles>

<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="roles" type="java.util.Map<java.lang.String, java.lang.String>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=""
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm>
    <@teamMemberRoles.teamMemberRoles
      rolesFormPath="form.roles"
      roleCheckBoxItems=roles
      fieldsetHeadingText=pageTitle
    />
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>