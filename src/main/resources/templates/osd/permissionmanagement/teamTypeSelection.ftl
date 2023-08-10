<#-- @ftlvariable name="teamTypeRouteMap" type="java.util.Map<uk.co.nstauthority.offshoresafetydirective.teams.TeamType, java.lang.String>" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#include '../layout/layout.ftl'>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=pageTitle
    errorItems=errorList
    pageSize=PageSize.TWO_THIRDS_COLUMN
>

    <@fdsResultList.resultList>
        <#list teamTypeRouteMap as teamType, route>
            <@fdsResultList.resultListItem
                linkHeadingUrl=springUrl(route)
                linkHeadingText=teamType.getDisplayText()/>
        </#list>
    </@fdsResultList.resultList>

</@defaultPage>