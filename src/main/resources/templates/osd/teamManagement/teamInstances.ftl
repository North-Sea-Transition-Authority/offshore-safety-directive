<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="teamViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamView>" -->

<#assign pageTitle="Select a team"/>

<@defaultPage pageHeading=pageTitle>
  <#if createNewInstanceUrl?has_content>
    <@fdsAction.link linkText="Create team" linkUrl=springUrl(createNewInstanceUrl) linkClass="govuk-button"/>
  </#if>

  <@fdsResultList.resultList resultCount=teamViews?size resultCountSuffix="team">
    <#list teamViews as teamView>
      <@fdsResultList.resultListItem
        linkHeadingUrl=springUrl(teamView.manageUrl())
        linkHeadingText=teamView.teamName()
      />
    </#list>
  </@fdsResultList.resultList>

</@defaultPage>