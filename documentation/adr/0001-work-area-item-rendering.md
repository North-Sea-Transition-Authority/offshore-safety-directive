# OSDOP-83: Rendering work area items

* Status: APPROVED
* Approved by: Cade Ayres, Chris Tasker, James Barnett

## Context and problem statement

We need a way of rendering the nominations that are relevant to the logged-in user. For WIOS, we only have one type
of item to show in the work area, a nomination. As a result we will be displaying the nominations using the
[search and filter](https://design-system.fivium.co.uk/patterns/search) FDS pattern as opposed to using multiple tabs
as per PWA and IRS implementations.

Consideration should be given to how other service can use a similar approach to rendering work area items if they have
similar requirements.

Fetching the items that a relevant to a specific users and any filters applied will be covered in another spike.

### Create a custom item and pass a map of properties to render similar to `ModelAndView` object

We will create a generic `WorkAreaItem` class which will contain the properties required to render and item on the
work area.

```java
public record WorkAreaItem(
    WorkAreaItemType type,
    String headingText,
    String actionUrl,
    WorkAreaItemModelProperties modelProperties
) {}
```

The `WorkAreItemType` will be an enum of types of items that can be shown in the work area. In this case the type will
be `NOMINATION`. For the simple WIOS case, we could simply ignore the type as we know everything will be a nomination.
I think there is still a benefit if we ever introduce another type of nomination that requires being displayed
differently with minimal rework to the existing template.

The `WorkAreaItemModelProperties` is a value class which is just a wrapper for a `Map` of template properties required
for rending the item. A builder method has been added to allow easy chaining of calls to `addProperty()`. This is
similar to passing in properties to a `ModelAndView`.

```java
public class WorkAreaItemModelProperties {
  
  private final Map<String, Object> properties = new HashMap<>();
  
  WorkAreaItemModelProperties addProperty(String key, Object value) {
    properties.put(key, value);
    return this;
  }
  
  public Map<String, Object> getProperties() {
    return properties;
  }
}
```

A `WorkAreaItemService` will be created which will be responsible for returning a `List<WorkAreaItem>` which will be
passed to the model and view in the work area controller. The example below returns all nominations just as a simple
example.

```java
@Service 
class WorkAreaItemService {
  
  private final NominationDetailService nominationDetailService;

  WorkAreaItemService(NominationDetailService nominationDetailService) {
    this.nominationDetailService = nominationDetailService;
  }
  
  List<WorkAreaItem> getWorkAreaItems() {
    return nominationDetailService.getAllNominations()
      .stream()
      .map(this::convertToWorkAreaItem)
      .toList();
  }
  
  private WorkAreaItem convertToWorkAreaItem(NominationDetail nominationDetail) {
    
    var dataItems = new LinkedHashSet<DataItem>();
    dataItems.add(new DataItem("Name", "Jane Doe"));
    dataItems.add(new DataItem("Status", "Draft"));
    
    var modelProperties = new WorkAreaItemModelProperties()
      .addProperty("operatorName", "My first operator")
      .addProperty("dataItems", dataItems);
    
    return new WorkAreaItem(
      WorkAreaItemType.NOMINATION,
      "WIOS/2022/%s".formatted(nominationDetail.getNomination().getId()),
      "/url",
      modelProperties
    );
  }
}
```

The work area template will loop over all the provided work area items. As we loop over each type of work area item
we check the type and call a utility macro in order to render that specific item.

The generic work area template:

```injectedfreemarker
<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>
  <@fdsResultList.resultList resultCount=workAreaItems?size>
    <#list workAreaItems as workAreaItem>
      <#if workAreaItem.type() == "NOMINATION">
        <@_nominationWorkAreaItem workAreaItem=workAreaItem/>
      </#if>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>
```

The macro to render a nomination work area item:

```injectedfreemarker
<#macro _nominationWorkAreaItem workAreaItem>

  <#assign modelProperties = workAreaItem.modelProperties().properties />
  
  <@fdsResultList.resultListItem
    captionHeadingText=modelProperties.operatorName
    linkHeadingUrl=springUrl(workAreaItem.actionUrl())
    linkHeadingText=workAreaItem.headingText()
  >
    <@fdsResultList.resultListDataItem>
      <#list modelProperties.dataItems as dataItem>
        <@fdsResultList.resultListDataValue 
          key=dataItem.prompt() 
          value=dataItem.value()
        />
      </#list>
    </@fdsResultList.resultListDataItem>
  </@fdsResultList.resultListItem>
 </#macro>
```

As each work area item type will have its own map of model properties via the `WorkAreaItemModelProperties` field,
each work area item macro can just take the work area item and display however it likes through the nested property of
`fdsResultList.resultListItem`.

With this approach other services could copy the generic classes/controller and templates and make any small
modification if needed without need to rework the pattern.

#### Positives
- Each work area item type can be rendered exactly as required and is not tied to any other types
- Work area template remains simple with single loop and if statement switch to call the relevant macro depending on
  the type
- Using a map of model properties means it is easy to add or remove values without needing to change fields, getters or
  constructors
- Easy to support multiple types of nominations if others get added in future phases. The `NOMINATION` type would
  simply be replaced with n nomination specific subtypes.
- Controller test will catch any template issues (assuming at least one work area item per type is provided)

#### Negatives
- If you ended up with lots of different types of nominations the work area template could become cumbersome with
  lots of if statement conditions. This is unlikely as there is nothing in the WIOS roadmap to have other types of
  nominations.
- Passing a map of properties won't provide any warnings or errors in the template if developers reference properties
  that don't exist in the Map. `@ftlvariable` won't help as it has no concept of the content of the map is.
- As each type of work area item can be rendered with completely flexibility you could have items not conforming to the
  result list pattern. This is easily mitigated by PRs and direction from leads/design team ahead of time.

### Other options considered but not chosen

#### Having a work area item builder and a single template for all items

As the work area items will be displayed in a similar way, a heading followed by data items and possibly  some
additional content we could have a builder which different types of nominations could use. A single work area template
would then check each property (caption, data items etc) and if provided would set out the value.

The main reason this was not chosen was that if one type of nomination wanted the data items displayed after some other
component they wouldn't have that flexibility. Additionally, if we wanted to have a detail expander component we would
have to provide all the text in the java which limits the use of links within the text.

```java
class WorkAreaItem {
  
  private final String caption;
  
  private final Set<DataItem> dataItems;
  
  private WorkAreaItem(String caption, Set<DataItem> dataItems) {
    this.caption = caption;
    this.dataItems = dataItems;
  }
  
  static WorkAreaItemBuilder builder() {
    return new WorkAreaItemBuilder();
  }
  
  static class WorkAreaItemBuilder {
  
    private String caption;
    
    private Set<DataItem> dataItems;
    
    WorkAreaItemBuilder withCaption(String caption) {
      this.caption = caption;
      return this;
    }
    
    WorkAreaItemBuilder withDataItems(Set<DataItem> dataItems) {
      this.dataItems = dataItems;
      return this;
    }
    
    WorkAreaItem build() {
      return new WorkAreaItem(caption, dataItems);
    }
  }
}
```

#### Passing raw HTML strings to a generic template

On some previous services we have had a service which renders templates as HTML strings and passes a `List<String>`
into the model and view. These HTML strings are rendered in a way similar to the below:

```html
<ol class="govuk-list dashboard-list">
  <#list workAreaItems as workAreaItem>
    <li class="govuk-list__item dashboard-list__item">
      ${workAreaItem.htmlContent?no_esc}
    </li>
  </#list>
</ol>
```

The main downside to this approach is the inability for controller tests to catch any errors in the templates as the
services getting the HTML strings will be mocked during the test.
