<#-- @ftlvariable name="teamGroupMap" type="java.util.Map<uk.co.nstauthority.offshoresafetydirective.teams.TeamType, java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.TeamView>>" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#include '../layout/layout.ftl'>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=pageTitle
    errorItems=errorList
    pageSize=PageSize.FULL_COLUMN
>

    <#list teamGroupMap as teamType,teams>
        <h2 class="govuk-heading-l">${teamType.displayText}</h2>
        <div class="govuk-!-margin-bottom-6">
            <@fdsLinkList.linkList>
                <#list teams as team>
                    <@fdsLinkList.linkListItem
                        linkName=team.displayName()
                        linkUrl=team.teamUrl()
                    />
                </#list>
            </@fdsLinkList.linkList>
        </div>
    </#list>

</@defaultPage>