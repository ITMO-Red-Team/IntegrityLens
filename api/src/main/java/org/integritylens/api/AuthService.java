package org.integritylens.api;

import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.integritylens.api.Models.User;

@Service
class AuthService {
    static final String COOKIE = "session";
    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_-]{43}");
    private static final Set<String> ROLES = Set.of("teacher", "student");
    private static final Set<String> REGISTER_FIELDS = Set.of("fullName", "email", "password", "role");
    private static final Set<String> LOGIN_FIELDS = Set.of("email", "password");
    private final JdbcTemplate jdbc;
    private final Validator validator;
    private final String dummyHash = Secrets.passwordHash(Secrets.token());

    AuthService(JdbcTemplate jdbc, Validator validator) {
        this.jdbc = jdbc;
        this.validator = validator;
    }

    User register(Map<String, Object> body) {
        var fields = new AuthFields(body, REGISTER_FIELDS, "fullName");
        String name = fields.string("fullName", 1, 200, false);
        if (name != null && Inputs.strip(name).isEmpty()) {
            fields.add("fullName", "REQUIRED", "Заполните поле.");
        }
        String email = email(fields);
        String password = fields.string("password", 8, 128, false);
        String role = fields.string("role", 1, Integer.MAX_VALUE, false);
        if (role != null && !ROLES.contains(role)) {
            fields.add("role", "INVALID_VALUE", "Выберите роль преподавателя или студента.");
        }
        fields.check();

        UUID id = UUID.randomUUID();
        int inserted = jdbc.update("""
                insert into users(id, full_name, email, password_hash, role)
                values (?, ?, ?, ?, ?) on conflict (email) do nothing
                """, id, name, email, Secrets.passwordHash(password), role);
        if (inserted == 0) {
            throw new ApiException(409, "EMAIL_ALREADY_REGISTERED", "Этот email уже зарегистрирован.");
        }
        return new User(id, name, role);
    }

    @Transactional
    public Login login(Map<String, Object> body, String previousToken) {
        var fields = new AuthFields(body, LOGIN_FIELDS, "email");
        String email = email(fields);
        String password = fields.string("password", 8, 128, false);
        fields.check();

        Account account = jdbc.query("""
                select id, full_name, role, password_hash from users where email = ?
                """, (rs, index) -> new Account(user(rs), rs.getString("password_hash")), email)
                .stream().findFirst().orElse(null);
        boolean matches = Secrets.passwordMatches(password, account == null ? dummyHash : account.passwordHash());
        if (!matches || account == null) {
            throw new ApiException(401, "INVALID_CREDENTIALS", "Неверный email или пароль");
        }

        String token = Secrets.token();
        logout(previousToken);
        jdbc.update("""
                insert into sessions(token_hash, user_id, expires_at)
                values (?, ?, now() + interval '24 hours')
                """, Secrets.tokenHash(token), account.user().id());
        return new Login(account.user(), token);
    }

    User require(String token, String role) {
        User user = null;
        if (validToken(token)) {
            user = jdbc.query("""
                    select u.id, u.full_name, u.role from sessions s join users u on u.id = s.user_id
                    where s.token_hash = ? and s.expires_at > now()
                    """, (rs, index) -> user(rs), Secrets.tokenHash(token)).stream().findFirst().orElse(null);
        }
        if (user == null) {
            throw new ApiException(401, "AUTHENTICATION_REQUIRED", "Требуется вход в систему.");
        }
        if (role != null && !role.equals(user.role())) {
            throw new ApiException(403, "FORBIDDEN", "Недостаточно прав для этой операции.");
        }
        return user;
    }

    void logout(String token) {
        if (validToken(token)) {
            jdbc.update("delete from sessions where token_hash = ?", Secrets.tokenHash(token));
        }
    }

    private static boolean validToken(String token) {
        return token != null && TOKEN.matcher(token).matches();
    }

    private static User user(ResultSet row) throws SQLException {
        return new User(row.getObject("id", UUID.class), row.getString("full_name"), row.getString("role"));
    }

    private String email(AuthFields fields) {
        String value = fields.string("email", 1, Integer.MAX_VALUE, true);
        if (value == null) {
            return null;
        }
        String email = value.toLowerCase(Locale.ROOT);
        if (Inputs.length(email) > 320) {
            fields.add("email", "TOO_LONG", "Email должен содержать не более 320 символов.");
        } else if (!validator.validateValue(EmailField.class, "email", email).isEmpty()) {
            fields.add("email", "INVALID_FORMAT", "Введите корректный email.");
        }
        return email;
    }

    record Login(User user, String token) {}

    record Account(User user, String passwordHash) {}

    private static class EmailField {
        @Email
        String email;
    }

    static final class AuthFields {
        private final Map<String, Object> input;
        private final List<Map<String, String>> errors = new ArrayList<>();

        AuthFields(Map<String, Object> input, Set<String> allowed, String errorField) {
            this.input = input == null ? Map.of() : input;
            if (!allowed.containsAll(this.input.keySet())) {
                add(errorField, "INVALID_VALUE", "Запрос содержит неизвестное поле.");
            }
        }

        String string(String field, int min, int max, boolean trim) {
            Object value = input.get(field);
            if (value == null || "".equals(value)) {
                add(field, "REQUIRED", "Заполните поле.");
                return null;
            }
            if (!(value instanceof String text) || (!field.equals("password") && text.indexOf('\0') >= 0)) {
                add(field, "INVALID_VALUE", "Введите корректное значение.");
                return null;
            }
            String result = trim ? Inputs.strip(text) : text;
            int length = Inputs.length(result);
            if (length == 0) {
                add(field, "REQUIRED", "Заполните поле.");
            } else if (length < min) {
                add(field, "TOO_SHORT", "Минимальная длина — " + min + " символов.");
            } else if (length > max) {
                add(field, "TOO_LONG", "Максимальная длина — " + max + " символов.");
            } else {
                return result;
            }
            return null;
        }

        void add(String field, String code, String message) {
            errors.add(Map.of("field", field, "code", code, "message", message));
        }

        void check() {
            if (!errors.isEmpty()) {
                throw new ApiException(422, "VALIDATION_ERROR", "Проверьте заполнение полей.",
                        Map.of("fields", errors));
            }
        }
    }
}
