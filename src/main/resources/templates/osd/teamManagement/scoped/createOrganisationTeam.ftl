<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<#assign pageHeading = "Select an organisation"/>

<@defaultPage pageHeading="" htmlTitle=pageHeading errorItems=errorList>

  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorRest
      path="form.orgGroupId"
      restUrl=springUrl(organisationSearchUrl)
      labelText=pageHeading
      pageHeading=true
    />

    <@fdsDetails.summaryDetails summaryTitle="The organisation I want to create a team for is not listed">
      If the organisation you want to create a team for is not shown in the list
      then you must contact the person responsible for managing organisations on the
      UK Energy Portal.
    </@fdsDetails.summaryDetails>

    <@fdsAction.button buttonText="Create team"/>

</@fdsForm.htmlForm>

</@defaultPage>