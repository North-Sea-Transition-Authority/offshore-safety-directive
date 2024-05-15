<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->

<#macro systemOfRecordPage pageTitle pageSize backLinkUrl="" backLinkWithBrowserBack=false>
  <@defaultPage
    htmlTitle=pageTitle
    pageHeading=pageTitle
    errorItems=[]
    pageSize=pageSize
    showNavigationItems=(loggedInUser?has_content)
    allowSearchEngineIndexing=true
    phaseBanner=(loggedInUser?has_content)
    backLinkUrl=backLinkUrl
    backLinkWithBrowserBack=backLinkWithBrowserBack
  >
    <#nested/>
  </@defaultPage>
</#macro>