<#include '../../fds/layout.ftl'>
<#include '../../fds/objects/layouts/leftSubNavLayout.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/mailTo.ftl' as mailTo>
<#import '../macros/taskList.ftl' as taskList>

<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->

<#assign serviceName = serviceBranding.name() />
<#assign customerMnemonic = customerBranding.mnemonic() />
<#assign serviceHomeUrl = springUrl(serviceHomeUrl) />

<#macro defaultPage
  pageHeading
  htmlTitle=pageHeading
  errorItems=[]
  phaseBanner=true
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=""
  breadcrumbsList={}
>

  <#assign fullWidthColumn=false />
  <#assign oneHalfColumn=false />
  <#assign oneThirdColumn=false />
  <#assign twoThirdsColumn=false />
  <#assign twoThirdsOneThirdColumn=false />
  <#assign oneQuarterColumn=false />

  <#if pageSize == PageSize.FULL_WIDTH>
    <#assign fullWidthColumn=true/>
  <#elseif pageSize == PageSize.ONE_HALF_COLUMN>
    <#assign oneHalfColumn=true/>
  <#elseif pageSize == PageSize.ONE_THIRD_COLUMN>
    <#assign oneThirdColumn=true/>
  <#elseif pageSize == PageSize.TWO_THIRDS_ONE_THIRD_COLUMN>
    <#assign twoThirdsOneThirdColumn=true/>
  <#elseif pageSize == PageSize.ONE_QUARTER>
    <#assign oneQuarterColumn=true/>
  <#else>
    <#assign twoThirdsColumn=true/>
  </#if>

  <#assign useBreadCrumbs=false>
  <#if breadcrumbsList?has_content>
    <#assign useBreadCrumbs=true>
  </#if>

  <#assign backLink = false>
  <#if backLinkUrl?has_content && useBreadCrumbs==false>
    <#assign backLink=true/>
  </#if>

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
    fullWidthColumn=fullWidthColumn
    oneHalfColumn=oneHalfColumn
    oneThirdColumn=oneThirdColumn
    twoThirdsColumn=twoThirdsColumn
    twoThirdsOneThirdColumn=twoThirdsOneThirdColumn
    oneQuarterColumn=oneQuarterColumn
    topNavigation=true
    errorItems=errorItems
    backLink=backLink
    backLinkUrl=backLinkUrl
    breadcrumbs=useBreadCrumbs
    breadcrumbsList=breadcrumbsList
    singleErrorMessage=singleErrorMessage
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigation
  pageHeading
  htmlTitle=pageHeading
  phaseBanner=true
>
    <@fdsLeftSubNavPageTemplate
      htmlTitle=htmlTitle
      htmlAppTitle=pageHeading
      phaseBanner=phaseBanner
      homePageUrl=serviceHomeUrl
      serviceUrl=serviceHomeUrl
      topNavigation=true
      logoProductText=customerMnemonic
      phaseBannerContent=""
    >
      <#nested />
    </@fdsLeftSubNavPageTemplate>
</#macro>