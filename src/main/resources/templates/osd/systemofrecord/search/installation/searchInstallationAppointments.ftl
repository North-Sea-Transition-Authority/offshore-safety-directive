<#include '../../../layout/layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->
<#-- @ftlvariable name="searchForm" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchForm" -->
<#-- @ftlvariable name="installationRestUrl" type="String" -->
<#-- @ftlvariable name="filteredInstallation" type="uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto" -->

<#assign pageTitle = "View appointments for installations" />

<@defaultPage
  pageHeading=pageTitle
  errorItems=[]
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>
  <#assign searchFilterContent>
    <@fdsSearch.searchFilterList filterButtonItemText="appointments">
      <@fdsSearch.searchFilterItem itemName="Installation" expanded=searchForm.installationId?has_content>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.installationId"
          restUrl=springUrl(installationRestUrl)
          labelText=""
          selectorMinInputLength=3
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=(filteredInstallation?has_content)?then(
            {filteredInstallation.id()?long?c : filteredInstallation.name()},
            {}
          )
        />
      </@fdsSearch.searchFilterItem>
    </@fdsSearch.searchFilterList>
  </#assign>
  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    hasAddedFilter=hasAddedFilter
    searchFilterContent=searchFilterContent
  />
</@defaultPage>