<#include '../layout/layout.ftl'>
<#-- @ftlvariable name="workAreaItem" type="uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaItem" -->

<#macro nominationWorkAreaItem workAreaItem>

    <#assign modelProperties = workAreaItem.modelProperties().properties />

    <@fdsResultList.resultListItem
      captionHeadingText=workAreaItem.captionText()!""
      linkHeadingUrl=springUrl(workAreaItem.actionUrl())
      linkHeadingText=workAreaItem.headingText()
    >

        <#assign defaultText = "Not provided"/>

        <@fdsResultList.resultListDataItem>
            <@fdsResultList.resultListDataValue
                key="Status"
                value=modelProperties["status"]
            />
            <@fdsResultList.resultListDataValue
                key="Applicant reference"
                value=modelProperties["applicantReference"]
            />
            <@fdsResultList.resultListDataValue
                key="Nomination for"
                value=modelProperties["nominationType"]
            />
        </@fdsResultList.resultListDataItem>

        <@fdsResultList.resultListDataItem>
            <@fdsResultList.resultListDataValue
                key="Applicant"
                value=modelProperties["applicantOrganisation"]
            />
            <@fdsResultList.resultListDataValue
                key="Nominee"
                value=modelProperties["nominationOrganisation"]
            />
            <@fdsResultList.resultListDataValue
                key=""
                value=""
            />
        </@fdsResultList.resultListDataItem>

    </@fdsResultList.resultListItem>
</#macro>