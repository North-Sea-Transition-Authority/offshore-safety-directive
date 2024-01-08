<#include '../../../layout/layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="searchForm" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchForm" -->
<#-- @ftlvariable name="appointedOperatorRestUrl" type="String" -->
<#-- @ftlvariable name="filteredAppointedOperator" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->

<#assign pageTitle = "View appointments by operator" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=[]
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
  phaseBanner=(loggedInUser?has_content)
>
  <#assign searchFilterContent>
    <@fdsSearch.searchFilterList filterButtonItemText="appointments">
      <@fdsSearch.searchFilterItem itemName="Appointed operator" expanded=searchForm.appointedOperatorId?has_content>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.appointedOperatorId"
          restUrl=springUrl(appointedOperatorRestUrl)
          labelText=""
          selectorMinInputLength=3
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=filteredAppointedOperator
        />
      </@fdsSearch.searchFilterItem>
    </@fdsSearch.searchFilterList>
  </#assign>

  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    searchFilterContent=searchFilterContent
    hasAddedFilter=hasAddedFilter
  />
</@defaultPage>