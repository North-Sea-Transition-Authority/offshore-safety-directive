<#import "../../../fds/components/timeline/timeline.ftl" as fdsTimeline/>
<#import "../../../fds/components/dataItems/dataItems.ftl" as fdsDataItems/>
<#import "../../../fds/components/button/button.ftl" as fdsButton/>
<#import "../../../fds/objects/layouts/generic.ftl" as fdsGeneric/>
<#import '../files/fileSummary.ftl' as fileSummary>

<#-- @ftlvariable name="events" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView>" -->

<#macro eventList events>
    <@fdsTimeline.timeline>
        <@fdsTimeline.timelineSection sectionHeading="Case events">
            <#list events as event>
                <@fdsTimeline.timelineTimeStamp timeStampHeading=event.title nodeNumber="">
                    <@fdsTimeline.timelineEvent>

                        <@fdsDataItems.dataItem dataItemListClasses="govuk-!-margin-top-2">
                            <@fdsDataItems.dataValues
                                key=event.customDatePrompt!"Date added"
                                value=event.formattedCreatedTime
                            />
                            <@fdsDataItems.dataValues
                                key=event.customCreatorPrompt!"Added by"
                                value=event.createdBy
                            />
                            <@fdsDataItems.dataValues
                                key=event.customVersionPrompt!"Nomination version"
                                value="${event.nominationVersion}"
                            />
                        </@fdsDataItems.dataItem>

                        <#if event.body?has_content>
                            <h4 class="govuk-heading-s">${event.customBodyPrompt!"Comments"}</h4>
                            <p class="govuk-body">
                                ${event.body}
                            </p>
                        </#if>

                        <#if event.fileViews?has_content>
                            <h4 class="govuk-heading-s">${event.customFilePrompt!"Documents"}</h4>
                            <@fileSummary.fileSummary event.fileViews/>
                        </#if>

                    </@fdsTimeline.timelineEvent>
                </@fdsTimeline.timelineTimeStamp>
            </#list>
        </@fdsTimeline.timelineSection>
    </@fdsTimeline.timeline>
</#macro>