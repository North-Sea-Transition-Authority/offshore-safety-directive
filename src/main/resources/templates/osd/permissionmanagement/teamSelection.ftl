<#-- @ftlvariable name="teamViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.TeamView>" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#include '../layout/layout.ftl'>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=pageTitle
    pageHeadingCaption=teamType.displayText
    errorItems=errorList
    pageSize=PageSize.FULL_COLUMN
>

    <@fdsResultList.resultList resultCount=teamViews?size resultCountSuffix="team">
        <#list teamViews as teamView>
            <@fdsResultList.resultListItem
                linkHeadingUrl=springUrl(teamView.teamUrl())
                linkHeadingText=teamView.displayName()/>
        </#list>
    </@fdsResultList.resultList>

</@defaultPage>