<#include '../../layout/layout.ftl'>
<#import '../_addTeamMember.ftl' as teamMember>

<#-- @ftlvariable name="htmlTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="submitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="registrationUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<@defaultPage
  htmlTitle=htmlTitle
  pageHeading=""
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@teamMember.addTeamMember
    usernameFormPath="form.username"
    registrationUrl=registrationUrl
    postUrl=springUrl(submitUrl)
    cancelUrl=springUrl(backLinkUrl)
  />
</@defaultPage>