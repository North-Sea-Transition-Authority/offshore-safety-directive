<#import "../../../fds/components/dataItems/dataItems.ftl" as fdsDataItems/>

<#macro caseProcessingHeader headerInformation>
    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues
            key="Nominated operator"
            value=headerInformation.nominatedOrganisationUnitView().name().name()
        />
        <@fdsDataItems.dataValues
            key="Nomination is for"
            value=headerInformation.nominationDisplayType().displayText
        />
        <@fdsDataItems.dataValues
            key="Status"
            value=headerInformation.nominationStatus().screenDisplayText
        />
        <@fdsDataItems.dataValues
            key="Decision"
            value=(headerInformation.nominationDecision().displayText)!""
        />
    </@fdsDataItems.dataItem>
</#macro>