<#include '../layout/layout.ftl'>
<#import '_nominationWorkAreaItem.ftl' as _nominationWorkAreaItem>

<#-- @ftlvariable name="startNominationUrl" type="String" -->
<#-- @ftlvariable name="workAreaItems" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaItem>" -->

<#assign pageTitle = "Work area" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <@fdsAction.link
    linkText="Create nomination"
    linkUrl=springUrl(startNominationUrl)
    linkClass="govuk-button"
  />

    <@fdsResultList.resultList resultCount=workAreaItems?size>
        <#list workAreaItems as workAreaItem>
            <#if workAreaItem.type() == "NOMINATION">
                <@_nominationWorkAreaItem.nominationWorkAreaItem workAreaItem=workAreaItem/>
            </#if>
        </#list>
    </@fdsResultList.resultList>
</@defaultPage>