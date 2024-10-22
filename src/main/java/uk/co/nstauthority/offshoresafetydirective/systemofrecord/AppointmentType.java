package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;

public enum AppointmentType implements DisplayableEnumOption {

  DEEMED("Deemed appointment", 20),
  FORWARD_APPROVED("Forward approved appointment", 30, List.of(PortalAssetType.WELLBORE)),
  PARENT_WELLBORE("Based on parent wellbore appointment", 10, List.of(PortalAssetType.WELLBORE)),
  OFFLINE_NOMINATION("Offline nomination", 40),
  ONLINE_NOMINATION("Online nomination", 50);

  private final String displayName;
  private final int displayOrder;
  private final List<PortalAssetType> portalAssetTypeList;

  AppointmentType(String displayName, int displayOrder, List<PortalAssetType> portalAssetTypeList) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.portalAssetTypeList = portalAssetTypeList;
  }

  AppointmentType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.portalAssetTypeList = Arrays.stream(PortalAssetType.values()).toList();
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return displayName;
  }

  @Override
  public String getFormValue() {
    return name();
  }

  public static Map<String, String> getDisplayableOptions(PortalAssetType portalAssetType) {
    return Arrays.stream(AppointmentType.class.getEnumConstants())
        .sorted(Comparator.comparingInt(DisplayableEnumOption::getDisplayOrder))
        .filter(appointmentType -> appointmentType.portalAssetTypeList.contains(portalAssetType))
        .collect(StreamUtil.toLinkedHashMap(
            DisplayableEnumOption::getFormValue,
            DisplayableEnumOption::getScreenDisplayText)
        );
  }

  public static boolean isValidForAssetType(PortalAssetType portalAssetType, AppointmentType appointmentType) {
    return appointmentType.portalAssetTypeList.contains(portalAssetType);
  }
}
