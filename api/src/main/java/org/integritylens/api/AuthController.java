package org.integritylens.api;

import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.integritylens.api.Models.User;

@RestController
@RequestMapping("/api/auth")
class AuthController {
    private final AuthService auth;

    AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    User register(@RequestBody(required = false) Map<String, Object> body) {
        return auth.register(body);
    }

    @PostMapping("/login")
    ResponseEntity<User> login(@RequestBody(required = false) Map<String, Object> body,
                               @CookieValue(name = AuthService.COOKIE, required = false) String token) {
        var result = auth.login(body, token);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie(result.token(), Duration.ofHours(24)))
                .body(result.user());
    }

    @GetMapping("/me")
    User me(@CookieValue(name = AuthService.COOKIE, required = false) String token) {
        return auth.require(token, null);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = AuthService.COOKIE, required = false) String token) {
        auth.logout(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO)).build();
    }

    private static String cookie(String token, Duration maxAge) {
        return ResponseCookie.from(AuthService.COOKIE, token).path("/").maxAge(maxAge)
                .httpOnly(true).secure(true).sameSite("Lax").build().toString();
    }
}
