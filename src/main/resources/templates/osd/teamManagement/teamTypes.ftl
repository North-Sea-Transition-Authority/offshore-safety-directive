<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="teamTypeViews" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamTypeView>" -->

<#assign pageTitle="Select a team"/>

<@defaultPage pageHeading=pageTitle>
  <@fdsResultList.resultList>
    <#list teamTypeViews as teamTypeView>
      <@fdsResultList.resultListItem
        linkHeadingUrl=springUrl(teamTypeView.manageUrl())
        linkHeadingText=teamTypeView.teamTypeName()
      />
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>