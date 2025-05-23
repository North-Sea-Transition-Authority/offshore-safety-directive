<#include '../layout/layout.ftl'>
<#-- @ftlvariable name="workAreaItem" type="uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaItem" -->

<#macro nominationWorkAreaItem workAreaItem>

    <#assign modelProperties = workAreaItem.modelProperties().properties />

    <#assign updateRequestedTag>
        <#if modelProperties["hasUpdateRequest"]>
            <@fdsResultList.resultListTag tagText="Update requested" tagClass="govuk-tag--blue"/>
        </#if>
    </#assign>

    <@fdsResultList.resultListItem
      captionHeadingText=workAreaItem.captionText()!""
      linkHeadingUrl=springUrl(workAreaItem.actionUrl())
      linkHeadingText=workAreaItem.headingText()
      itemTag=updateRequestedTag!""
    >

        <#assign defaultText = "Not provided"/>

        <@fdsResultList.resultListDataItem>
            <@fdsResultList.resultListDataValue key="Status" value=modelProperties["status"]/>
            <@fdsResultList.resultListDataValue key="Applicant reference" value=modelProperties["applicantReference"]/>
            <@fdsResultList.resultListDataValue key="Nomination for" value=modelProperties["nominationType"]/>
            <@_addRowIfPresentOrEmptyRow
                modelProperties=modelProperties
                modelPropertyKey="plannedAppointmentDate"
                dataItemPrompt="Planned appointment date"
            />
        </@fdsResultList.resultListDataItem>

        <@fdsResultList.resultListDataItem>
            <@fdsResultList.resultListDataValue key="Applicant" value=modelProperties["applicantOrganisation"]/>
            <@fdsResultList.resultListDataValue key="Nominee" value=modelProperties["nominationOrganisation"]/>
            <@_pearsReferences modelProperties=modelProperties/>
            <@_addRowIfPresentOrEmptyRow
                modelProperties=modelProperties
                modelPropertyKey="nominationFirstSubmittedOn"
                dataItemPrompt="First submitted on"
            />
        </@fdsResultList.resultListDataItem>

    </@fdsResultList.resultListItem>
</#macro>

<#macro _pearsReferences modelProperties>
    <#if modelProperties?keys?seq_contains("pearsReferences")>
        <div class="fds-data-items-list__container">
            <dt class="fds-data-items-list__key">PEARS references</dt>
            <dd class="fds-data-items-list__value">
                ${modelProperties["pearsReferences"]?has_content?then(modelProperties["pearsReferences"], "Not provided")}
                <#if modelProperties["pearsReferencesAbbreviated"]!false>
                    <span class="govuk-visually-hidden">(references truncated)</span>
                </#if>
            </dd>
        </div>
    <#else>
        <@fdsResultList.resultListDataValue key="" value=""/>
    </#if>
</#macro>

<#macro _addRowIfPresentOrEmptyRow modelProperties modelPropertyKey dataItemPrompt>
    <#if modelProperties?keys?seq_contains(modelPropertyKey)>
        <@fdsResultList.resultListDataValue key=dataItemPrompt value=modelProperties[modelPropertyKey]/>
    <#else>
        <@fdsResultList.resultListDataValue key="" value=""/>
    </#if>
</#macro>