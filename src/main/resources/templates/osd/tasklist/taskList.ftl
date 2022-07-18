<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView>" -->
<#include '../layout/layout.ftl'>

<#assign pageTitle = "Example task list" />

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=PageSize.TWO_THIRDS_COLUMN
>

    <@fdsTaskList.taskList>
        <#list taskListSections as section>
            <@fdsTaskList.taskListSection
            sectionNumber="${section?index + 1}"
            sectionHeadingText=section.sectionName()>
                <#list section.taskListItemViews() as item>
                    <@fdsTaskList.taskListItem itemText=item.name() itemUrl=springUrl(item.actionUrl()) />
                </#list>
            </@fdsTaskList.taskListSection>
        </#list>
    </@fdsTaskList.taskList>

</@defaultPage>