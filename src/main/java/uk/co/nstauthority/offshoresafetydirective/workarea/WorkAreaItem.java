package uk.co.nstauthority.offshoresafetydirective.workarea;

public record WorkAreaItem(
    WorkAreaItemType type,
    String headingText,
    String captionText,
    String actionUrl,
    WorkAreaItemModelProperties modelProperties
) {}