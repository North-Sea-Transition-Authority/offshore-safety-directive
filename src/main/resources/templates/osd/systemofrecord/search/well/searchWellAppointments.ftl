<#include '../../../layout/layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->

<#assign pageTitle = "View appointments for wells" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=[]
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>
  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    hasAddedFilter=hasAddedFilter
  />
</@defaultPage>