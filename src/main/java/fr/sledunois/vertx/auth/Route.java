package fr.sledunois.vertx.auth;

public enum Route {
  INDEX("/"),
  LOGIN("/login"),
  REGISTER("/register"),
  SIGN_IN("/sign-in"),
  SIGN_OUT("/sign-out"),
  SIGN_UP("/sign-up");

  private final String path;

  Route(String path) {
    this.path = path;
  }

  public String path() {
    return this.path;
  }
}
