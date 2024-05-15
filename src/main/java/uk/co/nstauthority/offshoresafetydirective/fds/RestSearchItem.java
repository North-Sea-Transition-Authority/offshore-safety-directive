package uk.co.nstauthority.offshoresafetydirective.fds;

/**
 * A RestSearchItem is used to produce each item within a search selector.
 * id and text are required fields for the JSON response.
 */
public record RestSearchItem(String id, String text) {

}
