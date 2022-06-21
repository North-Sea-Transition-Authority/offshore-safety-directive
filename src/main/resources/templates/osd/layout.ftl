<#include '../fds/layout.ftl'>

<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->

<#macro defaultPage
  htmlTitle
  pageHeading=""
  phaseBanner=true
>
  <#local serviceName = serviceBranding.name() />
  <#local customerMnemonic = customerBranding.mnemonic() />
  <#local serviceHomeUrl = springUrl('/work-area') />

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    pageHeading=pageHeading
    headerLogo="GOV_CREST"
    logoProductText=customerMnemonic
    phaseBanner=phaseBanner
    serviceUrl=serviceHomeUrl
    homePageUrl=serviceHomeUrl
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>