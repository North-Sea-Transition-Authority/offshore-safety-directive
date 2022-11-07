<#import "../../fds/components/header/header.ftl" as fdsHeader>

<#-- @ftlvariable name="serviceName" type="String" -->
<#-- @ftlvariable name="customerMnemonic" type="String" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->
<#-- @ftlvariable name="signOutUrl" type="String" -->
<#-- @ftlvariable name="signedInUser" type="String" -->
<#-- @ftlvariable name="signOutButtonText" type="String" -->

<#macro header serviceName customerMnemonic serviceHomeUrl signOutUrl signedInUserName="" signOutButtonText="Sign out">
  <@fdsHeader.header
    homePageUrl=serviceHomeUrl
    serviceUrl=serviceHomeUrl
    logoProductText=customerMnemonic
    headerNav=true
    serviceName=serviceName
    headerLogo="GOV_CREST"
  >
    <#if signedInUserName?has_content>
      <@fdsHeader.headerNavigation>
        <@fdsHeader.headerNavigationItem
          itemText=signedInUserName
          itemActive=false
        />
        <@fdsHeader.headerNavigationSignOutButton formUrl=signOutUrl buttonText=signOutButtonText/>
      </@fdsHeader.headerNavigation>
    </#if>
  </@fdsHeader.header>
</#macro>