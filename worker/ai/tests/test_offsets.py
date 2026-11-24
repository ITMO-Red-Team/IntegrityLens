import tempfile
import unittest
from collections import namedtuple
from pathlib import Path
from types import SimpleNamespace

from worker.ai import real
from worker.ai.real import byte_fragments, decode_source, normalize_newlines

Span = namedtuple("Span", "start_char end_char confidence")

RUSSIAN = "// комментарий\nint x = 1;\n"


class DecodeSourceTest(unittest.TestCase):
    def test_plain(self):
        self.assertEqual(decode_source(b"class A {}"), ("class A {}", 0))

    def test_bom_is_stripped_and_counted(self):
        text, bom = decode_source(b"\xef\xbb\xbfclass A {}")
        self.assertEqual((text, bom), ("class A {}", 3))

    def test_invalid_utf8(self):
        with self.assertRaises(UnicodeDecodeError):
            decode_source(b"\xff\xfe")


class ByteFragmentsTest(unittest.TestCase):
    def test_ascii_offsets_are_bytes(self):
        fragments = byte_fragments([Span(0, 5, 0.9)], "class A {}", 0)
        self.assertEqual(fragments, [{"startIndex": 0, "endIndex": 5, "confidence": 0.9}])

    def test_multibyte_offsets_count_bytes(self):
        # "// комментарий" — 3 однобайтовых и 11 двухбайтовых символов: 25 байтов.
        fragments = byte_fragments([Span(0, len("// комментарий"), 0.9)], RUSSIAN, 0)
        self.assertEqual(fragments, [{"startIndex": 0, "endIndex": 25, "confidence": 0.9}])

    def test_bom_shifts_offsets(self):
        fragments = byte_fragments([Span(0, 1, 0.9)], "x", bom=3)
        self.assertEqual(fragments, [{"startIndex": 3, "endIndex": 4, "confidence": 0.9}])

    def test_boundaries_stay_on_characters(self):
        for span in (Span(0, 3, 0.5), Span(3, 14, 0.5)):
            with self.subTest(span=span):
                fragments = byte_fragments([span], RUSSIAN, 0)
                (RUSSIAN.encode("utf-8")[fragments[0]["startIndex"]:fragments[0]["endIndex"]]).decode("utf-8")

    def test_overlaps_merge_with_max_confidence(self):
        fragments = byte_fragments([Span(0, 5, 0.7), Span(3, 8, 0.9), Span(9, 11, 0.2)], "a" * 12, 0)
        self.assertEqual(fragments, [{"startIndex": 0, "endIndex": 8, "confidence": 0.9},
                                     {"startIndex": 9, "endIndex": 11, "confidence": 0.2}])

    def test_touching_spans_merge(self):
        fragments = byte_fragments([Span(0, 3, 0.5), Span(3, 6, 0.4)], "a" * 6, 0)
        self.assertEqual(fragments, [{"startIndex": 0, "endIndex": 6, "confidence": 0.5}])

    def test_empty_spans_are_dropped(self):
        self.assertEqual(byte_fragments([Span(4, 4, 0.5)], "abcd", 0), [])


class NormalizeNewlinesTest(unittest.TestCase):
    def test_text_without_carriage_returns_is_unchanged(self):
        self.assertEqual(normalize_newlines("a\nb\u2028c"), ("a\nb\u2028c", None))

    def test_crlf_and_lone_cr_become_lf_and_keep_their_positions(self):
        # a=0 \r=1 \n=2 b=3 \r=4 c=5
        self.assertEqual(normalize_newlines("a\r\nb\rc"), ("a\nb\nc", [0, 1, 3, 4, 5, 6]))

    def test_matches_the_way_the_tool_reads_a_file(self):
        # aicode читает файл через read_text: текстовый режим с теми же переводами строк.
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "Main.java"
            path.write_bytes("class A {\r\n    int x;\r}\r\n// конец\n".encode("utf-8"))
            text, origin = normalize_newlines(path.read_bytes().decode("utf-8"))
            self.assertEqual(text, path.read_text(encoding="utf-8-sig"))
            self.assertEqual(len(origin), len(text) + 1)


class NormalizedByteFragmentsTest(unittest.TestCase):
    def setUp(self):
        # файл: a=0 \r=1 \n=2 b=3 b=4 \r=5 \n=6; текст модели: a=0 \n=1 b=2 b=3 \n=4
        self.file = "a\r\nbb\r\n"
        self.model_text, self.origin = normalize_newlines(self.file)
        self.assertEqual(self.model_text, "a\nbb\n")

    def test_offsets_of_the_model_text_map_back_to_the_file(self):
        fragments = [{"startIndex": 1, "endIndex": 5, "confidence": 0.5}]  # "\r\nbb"
        self.assertEqual(byte_fragments([Span(1, 4, 0.5)], self.file, origin=self.origin), fragments)
        self.assertEqual(byte_fragments([Span(1, 4, 0.5)], self.file, bom=3, origin=self.origin),
                         [{"startIndex": 4, "endIndex": 8, "confidence": 0.5}])

    def test_whole_model_text_is_the_whole_file(self):
        self.assertEqual(byte_fragments([Span(0, 5, 0.5)], self.file, origin=self.origin),
                         [{"startIndex": 0, "endIndex": 7, "confidence": 0.5}])

    def test_boundaries_never_split_a_newline_pair(self):
        data = self.file.encode("utf-8")
        for start in range(len(self.model_text)):
            for end in range(start + 1, len(self.model_text) + 1):
                fragment = byte_fragments([Span(start, end, 0.5)], self.file, origin=self.origin)[0]
                piece = data[fragment["startIndex"]:fragment["endIndex"]]
                self.assertTrue(piece)  # непустой фрагмент текста модели остаётся непустым
                piece.decode("utf-8")


class AnalyzeOneTest(unittest.TestCase):
    def test_model_gets_lf_text_and_fragments_come_back_in_file_bytes(self):
        data = "class A {\r\n    int x = 1;\r\n}\r\n".encode("utf-8")
        seen = {}

        def analyze_text(path, text, model, tokenizer, torch, threshold):
            seen["text"] = text
            return SimpleNamespace(spans=(Span(0, 10, 0.9),))  # в тексте модели: "class A {\n"

        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "Main.java"
            path.write_bytes(data)
            file = real._analyze_one({"submissionId": "s", "path": "input/Main.java"}, path, None, None, None,
                                     analyze_text, 0.05)
        self.assertEqual(seen["text"], "class A {\n    int x = 1;\n}\n")
        self.assertEqual(file, {"submissionId": "s", "status": "ok", "score": 11 / len(data), "suspicious": [
            {"startIndex": 0, "endIndex": 11, "confidence": 0.9}]})
        self.assertEqual(data[:11], b"class A {\r\n")


if __name__ == "__main__":
    unittest.main()
