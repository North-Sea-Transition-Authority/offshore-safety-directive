<#include '../layout/layout.ftl'>

<#macro standardTaskList taskListSections>
  <@fdsTaskList.taskList>
    <#list taskListSections as section>
      <@fdsTaskList.taskListSection
        sectionNumber="${section?index + 1}"
        sectionHeadingText=section.sectionName()
        warningText=section.sectionWarningText()
      >
        <#list section.taskListItemViews() as item>
          <@fdsTaskList.taskListItem
            itemText=item.displayName
            itemUrl=springUrl(item.actionUrl)
            showTag=item.showTaskListLabels()
            completed=item.isItemValid()
            tagClass=(item.customTaskListLabel.labelType().cssClassName)!""
            tagText=(item.customTaskListLabel.labelText())!""
            useNotCompletedLabels=item.showNotCompletedLabel()
          />
        </#list>
      </@fdsTaskList.taskListSection>
    </#list>
  </@fdsTaskList.taskList>
</#macro>