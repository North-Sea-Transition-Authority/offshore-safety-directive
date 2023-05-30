<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="assetTypeDisplayName" type="String" -->

<@defaultPage
  pageHeading="Update appointment"
  pageHeadingCaption="${assetName} - ${assetTypeDisplayName}"
  errorItems=[]
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkWithBrowserBack=true
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>

</@defaultPage>