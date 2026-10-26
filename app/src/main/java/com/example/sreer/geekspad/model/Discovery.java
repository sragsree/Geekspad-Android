package com.example.sreer.geekspad.model;

/** Shared recruiter/discovery filtering. Proficiency is a minimum, not an exact match. */
public final class Discovery {
  private Discovery() {}

  public static boolean matches(
      User user, String country, String state, String skill, String minimum) {
    if (country != null && !country.equals(user.getCountry())) return false;
    if (state != null && !state.equals(user.getState())) return false;
    if (skill == null) return true;
    Skill selected = user.getSkill(skill);
    return selected != null && (minimum == null || score(selected.proficency) >= score(minimum));
  }

  public static int score(String value) {
    if (value == null) return 0;
    try {
      return Integer.parseInt(value.replace("%", "").trim());
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }
}
