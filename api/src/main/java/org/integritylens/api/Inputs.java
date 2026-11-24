package org.integritylens.api;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

final class Inputs {
    private static final Pattern UUID_FORMAT =
            Pattern.compile("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

    static UUID uuid(Object value) {
        if (!(value instanceof String text) || !UUID_FORMAT.matcher(text).matches()) {
            throw ApiException.invalid("Некорректный идентификатор.");
        }
        return UUID.fromString(text);
    }

    static List<UUID> uuids(Object value) {
        if (!(value instanceof List<?> list)) {
            throw ApiException.invalid("Укажите список идентификаторов источников.");
        }
        var ids = new ArrayList<UUID>(list.size());
        var unique = new HashSet<UUID>();
        for (Object element : list) {
            UUID id = uuid(element);
            if (!unique.add(id)) {
                throw ApiException.invalid("Источники не должны повторяться.");
            }
            ids.add(id);
        }
        return ids;
    }

    static Map<String, Object> fields(Map<String, Object> body, String... allowed) {
        if (body == null || !Set.of(allowed).containsAll(body.keySet())) {
            throw ApiException.invalid("Некорректные поля запроса.");
        }
        return body;
    }

    static String title(Object value) {
        if (value instanceof String text && text.indexOf('\0') < 0) {
            String title = strip(text);
            if (!title.isEmpty() && length(title) <= 200) {
                return title;
            }
        }
        throw ApiException.invalid("Название должно содержать от 1 до 200 символов.");
    }

    static String strip(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && space(text.codePointAt(start))) {
            start += Character.charCount(text.codePointAt(start));
        }
        while (start < end && space(text.codePointBefore(end))) {
            end -= Character.charCount(text.codePointBefore(end));
        }
        return text.substring(start, end);
    }

    private static boolean space(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint) || codePoint == 0x85;
    }

    static int length(String text) {
        return text.codePointCount(0, text.length());
    }

    static Instant instant(ResultSet row, String column) throws SQLException {
        return row.getObject(column, OffsetDateTime.class).toInstant();
    }
}
