<#include '../../fds/layout.ftl'>
<#include '../../fds/objects/layouts/leftSubNavLayout.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/mailTo.ftl' as mailTo>
<#import '../macros/taskList.ftl' as taskList>
<#import '_header.ftl' as pageHeader>

<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->
<#-- @ftlvariable name="feedbackUrl" type="String" -->
<#-- @ftlvariable name="cookiesStatementUrl" type="String" -->
<#-- @ftlvariable name="singleErrorMessage" type="String" -->
<#-- @ftlvariable name="footerItems" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.footer.FooterItem>" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="flash" type="uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner" -->
<#-- @ftlvariable name="analytics" type="uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties" -->

<#assign SERVICE_NAME = serviceBranding.name() />
<#assign CUSTOMER_MNEMONIC = customerBranding.mnemonic() />
<#assign SERVICE_HOME_URL = springUrl(serviceHomeUrl) />
<#assign FEEDBACK_URL = springUrl(feedbackUrl)/>

<#macro defaultPage
  pageHeading
  htmlTitle=pageHeading
  errorItems=[]
  phaseBanner=true
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=""
  backLinkWithBrowserBack=false
  breadcrumbsList={}
  singleErrorMessage=""
  showNavigationItems=true
  allowSearchEngineIndexing=true
  pageHeadingCaption=""
>

  <#assign isFullColumnWidth=false />
  <#assign isOneHalfColumnWidth=false />
  <#assign isOneThirdColumnWidth=false />
  <#assign isTwoThirdsColumnWidth=false />
  <#assign isTwoThirdsOneThirdColumnWidth=false />
  <#assign isOneQuarterColumnWidth=false />
  <#assign isFullPageWidth=false />

  <#if pageSize == PageSize.FULL_COLUMN>
    <#assign isFullColumnWidth=true/>
  <#elseif pageSize == PageSize.ONE_HALF_COLUMN>
    <#assign isOneHalfColumnWidth=true/>
  <#elseif pageSize == PageSize.ONE_THIRD_COLUMN>
    <#assign isOneThirdColumnWidth=true/>
  <#elseif pageSize == PageSize.TWO_THIRDS_ONE_THIRD_COLUMN>
    <#assign isTwoThirdsOneThirdColumnWidth=true/>
  <#elseif pageSize == PageSize.ONE_QUARTER>
    <#assign isOneQuarterColumnWidth=true/>
  <#elseif pageSize == PageSize.FULL_PAGE>
    <#assign isFullPageWidth=true/>
  <#else>
    <#assign isTwoThirdsColumnWidth=true/>
  </#if>

  <#assign useBreadCrumbs=false>
  <#if breadcrumbsList?has_content>
    <#assign useBreadCrumbs=true>
  </#if>

  <#assign showBackLink = false>

  <#if backLinkUrl?has_content && useBreadCrumbs==false>
    <#assign showBackLink=true/>
  <#elseif backLinkWithBrowserBack == true && useBreadCrumbs == false>
    <#assign showBackLink=true/>
    <#assign backLinkUrl = ""/>
  </#if>

  <#assign notificationBannerContent>
    <#if flash?has_content>

      <#local bannerContent>
        <#if flash.heading?has_content>
          <#if flash.content?has_content>
            <@fdsNotificationBanner.notificationBannerContent headingText=flash.heading moreContent=flash.content/>
          <#else>
            <@fdsNotificationBanner.notificationBannerContent>${flash.heading}</@fdsNotificationBanner.notificationBannerContent>
          </#if>
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
    <@_serviceHeader pageSize=pageSize />
  </#assign>

  <#assign footer>
    <@_footer isFullPageWidth=isFullPageWidth/>
  </#assign>

  <#assign analyticsScript>
    <script src="<@spring.url'/assets/javascript/googleAnalyticsEventTracking.js'/>"></script>
  </#assign>

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    htmlAppTitle=SERVICE_NAME
    pageHeading=pageHeading
    phaseBanner=phaseBanner
    phaseBannerLink=FEEDBACK_URL
    fullWidthColumn=isFullColumnWidth
    oneHalfColumn=isOneHalfColumnWidth
    oneThirdColumn=isOneThirdColumnWidth
    twoThirdsColumn=isTwoThirdsColumnWidth
    twoThirdsOneThirdColumn=isTwoThirdsOneThirdColumnWidth
    oneQuarterColumn=isOneQuarterColumnWidth
    topNavigation=showNavigationItems
    errorItems=errorItems
    backLink=showBackLink
    backLinkUrl=backLinkUrl
    breadcrumbs=useBreadCrumbs
    breadcrumbsList=breadcrumbsList
    notificationBannerContent=notificationBannerContent
    singleErrorMessage=singleErrorMessage
    headerContent=serviceHeader
    footerContent=footer
    customScriptContent=analyticsScript
    noIndex=!allowSearchEngineIndexing
    caption=pageHeadingCaption
    wrapperWidth=isFullPageWidth
    cookieBannerMacro=_cookieBanner
  >
    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.serviceAnalyticIdentifier() />
    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.energyPortalAnalyticIdentifier() />
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigation
  pageHeading
  htmlTitle=pageHeading
  phaseBanner=true
  showNavigationItems=true
  allowSearchEngineIndexing=true
>
  <#assign serviceHeader>
    <@_serviceHeader pageSize=PageSize.TWO_THIRDS_COLUMN />
  </#assign>

  <#assign footer>
    <@_footer isFullPageWidth=false/>
  </#assign>

  <#assign analyticsScript>
    <script src="<@spring.url'/assets/javascript/googleAnalyticsEventTracking.js'/>"></script>
  </#assign>

  <@fdsLeftSubNavPageTemplate
    htmlTitle=htmlTitle
    htmlAppTitle=pageHeading
    phaseBanner=phaseBanner
    topNavigation=showNavigationItems
    logoProductText=CUSTOMER_MNEMONIC
    headerContent=serviceHeader
    noIndex=!allowSearchEngineIndexing
    footerContent=footer
    customScriptContent=analyticsScript
    cookieBannerMacro=_cookieBanner
  >
    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.serviceAnalyticIdentifier() />
    <@fdsGoogleAnalytics.googleAnalytics measurementId=analytics.energyPortalAnalyticIdentifier() />
    <#nested />
  </@fdsLeftSubNavPageTemplate>
</#macro>

<#macro defaultErrorPage pageHeading>
  <@defaultPage
    pageHeading=pageHeading
    phaseBanner=false
    showNavigationItems=false
    allowSearchEngineIndexing=false
    pageSize=PageSize.TWO_THIRDS_COLUMN
  >
    <#nested/>
  </@defaultPage>
</#macro>

<#macro _serviceHeader pageSize>
  <@pageHeader.header
    serviceName=SERVICE_NAME
    customerMnemonic=CUSTOMER_MNEMONIC
    serviceHomeUrl=SERVICE_HOME_URL
    signedInUserName=(loggedInUser?has_content)?then(loggedInUser.displayName(), "")
    signOutUrl=springUrl("/logout")
    pageSize=pageSize
  />
</#macro>

<#macro _footer isFullPageWidth>
  <#local footerMetaContent>
    <@fdsFooter.footerMeta footerMetaHiddenHeading="Support links">
      <#list footerItems as footerItem>
        <@fdsFooter.footerMetaLink linkText=footerItem.prompt() linkUrl=springUrl(footerItem.url())/>
      </#list>
    </@fdsFooter.footerMeta>
  </#local>

  <@fdsNstaFooter.nstaFooter wrapperWidth=isFullPageWidth metaLinks=true footerMetaContent=footerMetaContent/>
</#macro>

<#macro _cookieBanner>
  <@fdsCookieBanner.analyticsCookieBanner
    serviceName=serviceBranding.mnemonic()
    cookieSettingsUrl=springUrl(cookiesStatementUrl)
  />
</#macro>