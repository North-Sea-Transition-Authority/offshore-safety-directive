<#include '../../fds/layout.ftl'>
<#include '../../fds/objects/layouts/leftSubNavLayout.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/mailTo.ftl' as mailTo>
<#import '../macros/taskList.ftl' as taskList>
<#import '_header.ftl' as pageHeader>

<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->
<#-- @ftlvariable name="singleErrorMessage" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->

<#assign SERVICE_NAME = serviceBranding.name() />
<#assign CUSTOMER_MNEMONIC = customerBranding.mnemonic() />
<#assign SERVICE_HOME_URL = springUrl(serviceHomeUrl) />

<#macro defaultPage
  pageHeading
  htmlTitle=pageHeading
  errorItems=[]
  phaseBanner=true
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=""
  breadcrumbsList={}
  singleErrorMessage=""
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

  <#assign notificationBannerContent>
    <#if flash?has_content>

        <#local bannerContent>
            <#if flash.heading?has_content>
              <@fdsNotificationBanner.notificationBannerContent headingText=flash.heading moreContent=flash.content/>
            <#else>
                <p class="govuk-body">
                  ${flash.content}
                </p>
            </#if>
        </#local>

        <#if flash.type.name() == "INFO">
          <@fdsNotificationBanner.notificationBannerInfo bannerTitleText=flash.title>
              ${bannerContent}
          </@fdsNotificationBanner.notificationBannerInfo>
        <#elseif flash.type.name() == "SUCCESS">
          <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText=flash.title>
              ${bannerContent}
          </@fdsNotificationBanner.notificationBannerSuccess>
        </#if>
    </#if>
  </#assign>

  <#assign serviceHeader>
    <@_serviceHeader/>
  </#assign>

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    htmlAppTitle=SERVICE_NAME
    pageHeading=pageHeading
    phaseBanner=phaseBanner
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
    notificationBannerContent=notificationBannerContent
    singleErrorMessage=singleErrorMessage
    headerContent=serviceHeader
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigation
  pageHeading
  htmlTitle=pageHeading
  phaseBanner=true
>
  <#assign serviceHeader>
    <@_serviceHeader/>
  </#assign>

  <@fdsLeftSubNavPageTemplate
    htmlTitle=htmlTitle
    htmlAppTitle=pageHeading
    phaseBanner=phaseBanner
    topNavigation=true
    logoProductText=CUSTOMER_MNEMONIC
    headerContent=serviceHeader
  >
    <#nested />
  </@fdsLeftSubNavPageTemplate>
</#macro>

<#macro _serviceHeader>
  <@pageHeader.header
    serviceName=SERVICE_NAME
    customerMnemonic=CUSTOMER_MNEMONIC
    serviceHomeUrl=SERVICE_HOME_URL
    signedInUserName=(loggedInUser?has_content)?then(loggedInUser.displayName(), "")
    signOutUrl=springUrl("/logout")
  />
</#macro>