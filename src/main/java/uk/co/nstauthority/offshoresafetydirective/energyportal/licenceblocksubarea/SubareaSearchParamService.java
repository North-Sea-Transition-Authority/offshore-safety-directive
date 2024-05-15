package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

@Service
class SubareaSearchParamService {

  private static final Pattern OFFSHORE_LICENCE_REF_PATTERN = Pattern.compile("\\b([Pp]\\d+)\\b");
  private static final Pattern OFFSHORE_BLOCK_REF_PATTERN = Pattern.compile("\\b(\\d+/?\\d*[A-Za-z]*)\\b");

  LicenceSubareaSearchParams parseSearchTerm(String searchTerm) {

    if (NumberUtils.isDigits(searchTerm)) {
      return LicenceSubareaSearchParams.anyCanMatch(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm));
    }

    var offshoreLicence = regexMatcher(OFFSHORE_LICENCE_REF_PATTERN, searchTerm);

    if (offshoreLicence.isPresent()) {
      searchTerm = searchTerm.replace(offshoreLicence.get(), "").trim();
    }

    var offshoreBlock = regexMatcher(OFFSHORE_BLOCK_REF_PATTERN, searchTerm);

    if (offshoreLicence.isEmpty() && offshoreBlock.isEmpty()) {
      return LicenceSubareaSearchParams.anyCanMatch(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm));
    }

    var subareaName = searchTerm;

    if (offshoreLicence.isPresent()) {
      subareaName = subareaName.replace(offshoreLicence.get(), "").trim();
    }

    if (offshoreBlock.isPresent()) {
      subareaName = subareaName.replace(offshoreBlock.get(), "").trim();
    }

    String block = offshoreBlock.orElse(null);

    return LicenceSubareaSearchParams.allMustMatch(
        offshoreLicence,
        Optional.ofNullable(block),
        Optional.ofNullable(subareaName.isBlank() ? null : subareaName)
    );
  }

  private Optional<String> regexMatcher(Pattern pattern, String searchTerm) {
    var patternMatcher = pattern.matcher(searchTerm);
    return patternMatcher.find() ? Optional.of(patternMatcher.group()) : Optional.empty();
  }

  record LicenceSubareaSearchParams(Optional<String> licenceRef,
                                    Optional<String> blockRef,
                                    Optional<String> subareaName,
                                    SearchMode searchMode) {
    static LicenceSubareaSearchParams anyCanMatch(Optional<String> licenceRef,
                                                  Optional<String> blockRef,
                                                  Optional<String> subareaName) {
      return new LicenceSubareaSearchParams(licenceRef, blockRef, subareaName, SearchMode.OR);
    }

    static LicenceSubareaSearchParams allMustMatch(Optional<String> licenceRef,
                                                   Optional<String> blockRef,
                                                   Optional<String> subareaName) {
      return new LicenceSubareaSearchParams(licenceRef, blockRef, subareaName, SearchMode.AND);
    }
  }

  enum SearchMode {
    AND,
    OR
  }
}
