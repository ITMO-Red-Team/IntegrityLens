"""Стирание шаблонных функций: только Python, инструменты методов не нужны."""
import random
import shutil
import tempfile
import unittest
import unittest.mock
from pathlib import Path

from worker.core import template_filter
from worker.core.template_filter import _methods, _shape, _tokens, default_index, split_fragment, strip

BINARY_SEARCH = """static int find(int[] data, int key) {
        int lo = 0;
        int hi = data.length - 1;
        while (lo <= hi) {
            int m = lo + (hi - lo) / 2;
            if (data[m] == key) {
                return m;
            } else if (data[m] < key) {
                lo = m + 1;
            } else {
                hi = m - 1;
            }
        }
        return -1;
    }"""
OWN_CODE = """static long score(String[] rows, long bonus) {
        long total = bonus;
        for (String row : rows) {
            if (row.startsWith("#") || row.isBlank()) continue;
            total ^= row.chars().filter(Character::isDigit).count() << row.length();
            switch (row.charAt(0)) {
                case 'a': total <<= 1; break;
                default: total--;
            }
        }
        return total;
    }"""


def methods_of(source):
    source = source.encode()
    tokens = _tokens(source)
    return [source[tokens[first][2]:tokens[last][3]].decode() for first, last in _methods(tokens)]


class MethodsTest(unittest.TestCase):
    def test_finds_methods_at_any_class_depth(self):
        source = """
        package a.b;
        import java.util.*;
        public class Main {
            static int top() { return 1; }
            static class Inner {
                Inner() { }
                class Deep { void deep() throws Exception { } }
            }
            interface Named { default String name() { return "x"; } int size(); }
        }
        class Other { void other() { } }
        """
        self.assertEqual(methods_of(source), ["static int top() { return 1; }", "Inner() { }",
                                              "void deep() throws Exception { }",
                                              'default String name() { return "x"; }', "void other() { }"])

    def test_annotations_and_generics_belong_to_the_method(self):
        source = "class A { @Override public <T extends Comparable<T>> T max(List<T> a) { return a.get(0); } }"
        self.assertEqual(methods_of(source), ["@Override public <T extends Comparable<T>> T max(List<T> a) { return a.get(0); }"])

    def test_field_initializers_and_blocks_are_not_methods(self):
        source = """class A {
            int[] table = {1, 2, 3};
            Comparator<Integer> order = new Comparator<>() { public int compare(Integer a, Integer b) { return 0; } };
            Runnable task = () -> { work(); };
            static { init(); }
            { init(); }
            void real() { }
        }"""
        self.assertEqual(methods_of(source), ["void real() { }"])

    def test_inner_classes_and_lambdas_stay_inside_their_method(self):
        source = "class A { void f() { new Object() { void g() { } }; run(() -> { }); class L { void h() { } } } }"
        self.assertEqual(len(methods_of(source)), 1)

    def test_braces_in_strings_and_comments_do_not_count(self):
        source = r"""class A {
            // void comment() {
            /* } } */
            char open = '{';
            String text = "}\"{";
            String block = \"\"\"
                }
            \"\"\";
            void real() { char c = '}'; String s = "{"; /* { */ }
        }""".replace('\\"\\"\\"', '"""')
        self.assertEqual(methods_of(source), ["""void real() { char c = '}'; String s = "{"; /* { */ }"""])

    def test_annotation_arguments_belong_to_the_method(self):
        source = """class A {
            @SuppressWarnings({"a", "b"}) void first() { }
            @Handles(value = Foo.class) void second() { }
            @Handles(Foo.class) void third() { }
        }"""
        self.assertEqual(methods_of(source), ['@SuppressWarnings({"a", "b"}) void first() { }',
                                              "@Handles(value = Foo.class) void second() { }",
                                              "@Handles(Foo.class) void third() { }"])

    def test_enum_constant_with_a_body_is_not_erased(self):
        # Последняя константа тоже: за ней `;`, а не запятая.
        for source in ("enum E { A(1) { }, B(2) { }; E(int x) { } }", "enum E { A(1), B(2) { void own() { } }; E(int x) { } }",
                       "enum E { A(1) { } ; E(int x) { } }"):
            self.assertEqual(methods_of(source), ["E(int x) { }"])

    def test_enum_inside_a_class_does_not_hide_later_methods(self):
        source = "class A { enum E { X(1) { }; E(int x) { } } A(int x) { } enum F { Y, Z } void after() { } }"
        self.assertEqual(methods_of(source), ["E(int x) { }", "A(int x) { }", "void after() { }"])

    def test_records_are_classes(self):
        source = """class A {
            record Point(int x, int y) { Point { check(x); } int sum() { return x + y; } }
            record Box<T>(T value) implements Shape { static <T> Box<T> of(T value) { return new Box<>(value); } }
            void record(int record) { }
            int after() { return 1; }
        }
        record Main(String[] args) { public static void main(String[] args) { } }"""
        self.assertEqual(methods_of(source), ["int sum() { return x + y; }",
                                              "static <T> Box<T> of(T value) { return new Box<>(value); }",
                                              "void record(int record) { }", "int after() { return 1; }",
                                              "public static void main(String[] args) { }"])

    def test_enums_are_classes(self):
        source = "enum E { A, B { void own() { } }, C(1); E() { } E(int x) { } int value() { return 1; } }"
        self.assertEqual(methods_of(source), ["E() { }", "E(int x) { }", "int value() { return 1; }"])

    def test_broken_code_finds_what_it_can(self):
        self.assertEqual(methods_of("class A { void good() { } void bad() { "), ["void good() { }"])
        for source in ("", "{", "}", "}}}{{{", "/* never closed", '"never closed', "class", "void f()", "class A { void"):
            self.assertEqual(methods_of(source), [])

    def test_hostile_input_stays_linear(self):
        # На каждом из этих файлов квадратичный разбор занял бы часы.
        for source in (b"/* " * 300_000, b'"\\"' * 300_000, b'"""' + b"x" * 900_000, b"{}" * 400_000,
                       b"x = {} " * 150_000, b"{" * 900_000, b"'" * 900_000):
            strip(source, default_index())


class ShapeTest(unittest.TestCase):
    def shape(self, source):
        return _shape(_tokens(source.encode()))

    def test_names_numbers_and_types_do_not_matter(self):
        self.assertEqual(self.shape("long f(long a) { return a + 1; }"), self.shape("int gcd(int x) { return x + 2; }"))

    def test_layout_and_modifiers_do_not_matter(self):
        self.assertEqual(self.shape("public static void f(int a) { if (a > 0) { g(); } }"),
                         self.shape("@Override void f(int a) { if (a > 0) g(); }"))

    def test_structure_matters(self):
        self.assertNotEqual(self.shape("void f() { while (a) g(); }"), self.shape("void f() { if (a) g(); }"))


class StripTest(unittest.TestCase):
    index = default_index()

    def test_removes_a_renamed_template_and_keeps_own_code(self):
        source = f"class Main {{\n    {BINARY_SEARCH}\n\n    {OWN_CODE}\n}}\n".encode()
        clean, lines = strip(source, self.index)
        self.assertEqual(lines, [[2, 16]])
        self.assertNotIn(b"lo <= hi", clean)
        self.assertIn(OWN_CODE.encode(), clean)
        self.assertIn(b"class Main", clean)

    def test_record_keeps_its_own_code_and_main(self):
        main = "public static void main(String[] args) { }"
        source = f"record Main(int[] data) {{\n    {BINARY_SEARCH}\n\n    {OWN_CODE}\n\n    {main}\n}}\n".encode()
        clean, lines = strip(source, self.index)
        self.assertEqual(lines, [[2, 16]])
        for kept in ("record Main(int[] data) {", OWN_CODE, main):
            self.assertIn(kept.encode(), clean)

    def test_enum_constants_survive_a_template_in_the_last_one(self):
        source = f"enum E {{\n    A(1), B(2) {{\n    {BINARY_SEARCH}\n    }};\n    E(int x) {{ }}\n}}\n".encode()
        self.assertEqual(strip(source, self.index), (source, []))

    def test_main_is_never_a_template(self):
        body = BINARY_SEARCH.replace("static int find", "public static void main")
        source = f"class Main {{ {body} }}".encode()
        self.assertEqual(strip(source, self.index), (source, []))

    def test_annotated_main_is_never_a_template_either(self):
        body = BINARY_SEARCH.replace("static int find", '@SuppressWarnings("x") public static void main')
        source = f"class Main {{ {body} }}".encode()
        self.assertEqual(strip(source, self.index), (source, []))

    def test_functions_in_a_row_are_one_stretch(self):
        source = f"class Main {{\n{BINARY_SEARCH}\n// между ними\n\n{BINARY_SEARCH}\nint own;\n{BINARY_SEARCH}\n}}\n".encode()
        _, lines = strip(source, self.index)
        self.assertEqual(lines, [[2, 33], [35, 49]])

    def test_line_numbers_and_size_do_not_change(self):
        source = f"// заголовок\r\nclass Main {{\r\n    {BINARY_SEARCH}\r\n    {OWN_CODE}\r\n}}\r\n".encode()
        clean, lines = strip(source, self.index)
        self.assertTrue(lines)
        self.assertEqual(len(clean), len(source))
        self.assertEqual(clean.count(b"\n"), source.count(b"\n"))
        self.assertEqual(clean.count(b"\r"), source.count(b"\r"))
        self.assertEqual(clean.splitlines()[0], source.splitlines()[0])

    def test_nothing_to_remove_returns_the_source_as_is(self):
        source = f"class Main {{ {OWN_CODE} }}".encode()
        self.assertEqual(strip(source, self.index), (source, []))

    def test_stripping_twice_changes_nothing(self):
        source = f"class Main {{ {BINARY_SEARCH} {OWN_CODE} }}".encode()
        once, lines = strip(source, self.index)
        self.assertTrue(lines)
        self.assertEqual(strip(once, self.index), (once, []))

    def test_bom_and_foreign_bytes_survive(self):
        source = b"\xef\xbb\xbfclass Main { " + BINARY_SEARCH.encode() + b" // \xff\xfe\nString s = \"\xd0\xb6\"; }"
        clean, lines = strip(source, self.index)
        self.assertTrue(lines)
        self.assertTrue(clean.startswith(b"\xef\xbb\xbfclass Main {"))
        self.assertIn(b"\xff\xfe", clean)
        self.assertIn('"ж"'.encode(), clean)

    def test_empty_source(self):
        self.assertEqual(strip(b"", self.index), (b"", []))

    def test_any_input_keeps_size_and_lines(self):
        rng = random.Random(1)
        pieces = [b"class A {", b"void f() {", b"}", b"{", b"(", b")", b"=", b";", b'"', b"'", b"/*", b"*/", b"//", b"\n",
                  b"\xff", BINARY_SEARCH.encode(), b"static", b"enum E {", b",", b"@X(", b".class", b"throws"]
        for _ in range(300):
            source = b" ".join(rng.choices(pieces, k=rng.randint(0, 40)))
            clean, lines = strip(source, self.index)
            self.assertEqual(len(clean), len(source))
            self.assertEqual(clean.count(b"\n"), source.count(b"\n"))
            self.assertTrue(all(first <= last for first, last in lines))

    def test_own_code_is_not_a_template(self):
        self.assertFalse(self.index.is_template(_shape(_tokens(OWN_CODE.encode()))))

    def test_long_method_is_not_compared(self):
        self.assertFalse(self.index.is_template([b"I"] * 1_000_000))


class StripAllTest(unittest.TestCase):
    def test_copies_keep_the_input_layout(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            source = root / "Main.java"
            source.write_text(f"class Main {{\n{BINARY_SEARCH}\n}}\n")
            other = root / "Other.java"
            other.write_text(f"class Other {{ {OWN_CODE} }}")
            lines = template_filter.strip_all({"a": source, "b": source, "c": other}, root / "copy")
            self.assertEqual(lines, {"a": [[2, 16]], "b": [[2, 16]], "c": []})
            clean = (root / "copy" / "submissions" / "b" / "Main.java").read_text()
            self.assertNotIn("lo <= hi", clean)
            self.assertEqual(clean.count("\n"), source.read_text().count("\n"))
            self.assertEqual((root / "copy" / "submissions" / "c" / "Other.java").read_text(), other.read_text())


class SplitFragmentTest(unittest.TestCase):
    def test_nothing_stripped_keeps_the_fragment(self):
        self.assertEqual(split_fragment([1, 9], [5, 7], [], []), [([1, 9], [5, 7])])
        self.assertEqual(split_fragment([1, 9], [5, 7], [[20, 30]], [[1, 4]]), [([1, 9], [5, 7])])

    def test_same_stretches_on_both_sides_keep_their_order(self):
        self.assertEqual(split_fragment([1, 30], [11, 40], [[10, 20]], [[15, 25]]),
                         [([1, 9], [11, 14]), ([21, 30], [26, 40])])

    def test_edges_are_trimmed(self):
        self.assertEqual(split_fragment([5, 30], [1, 20], [[1, 10], [25, 40]], []), [([11, 24], [1, 20])])

    def test_different_stretches_leave_the_longest_part(self):
        self.assertEqual(split_fragment([1, 30], [1, 30], [[10, 20]], [[3, 4], [10, 12]]), [([21, 30], [13, 30])])

    def test_fully_stripped_side_leaves_nothing(self):
        self.assertEqual(split_fragment([5, 8], [1, 20], [[1, 10]], []), [])


class SetTest(unittest.TestCase):
    def test_every_function_of_the_set_is_recognized_in_its_own_file(self):
        index = default_index()
        files = sorted(template_filter.CODE.glob("*.java"))
        self.assertTrue(files)
        total = 0
        for path in files:
            source = path.read_bytes()
            tokens = _tokens(source)
            methods = _methods(tokens)
            self.assertTrue(methods, path.name)
            for first, last in methods:
                self.assertTrue(index.is_template(_shape(tokens[first:last + 1])), path.name)
            total += len(methods)
        self.assertEqual(len(index.shapes), total)

    def test_version_depends_on_the_set(self):
        version = template_filter.version()
        self.assertRegex(version, "^[0-9a-f]{8}$")
        self.assertEqual(version, template_filter.version())
        with tempfile.TemporaryDirectory() as folder:
            shutil.copytree(template_filter.CODE, folder, dirs_exist_ok=True)
            with unittest.mock.patch.object(template_filter, "CODE", Path(folder)):
                self.assertEqual(version, template_filter.version())
                with (Path(folder) / "Strings.java").open("a") as file:
                    file.write("class Added { void added() { } }\n")
                self.assertNotEqual(version, template_filter.version())


if __name__ == "__main__":
    unittest.main()
