import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class One {
    static class Word {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        int total = 0;
        String s = "";

        @Override
        public String toString() {
            return s;
        }
    }

    static class CompA implements Comparator<Word> {
        public int compare(Word a, Word b) {
            return (2 * b.a - b.total) - (2 * a.a - a.total);
        }
    }

    static class CompB implements Comparator<Word> {
        public int compare(Word a, Word b) {
            return (2 * b.b - b.total) - (2 * a.b - a.total);
        }
    }

    static class CompC implements Comparator<Word> {
        public int compare(Word a, Word b) {
            return (2 * b.c - b.total) - (2 * a.c - a.total);
        }
    }

    static class CompD implements Comparator<Word> {
        public int compare(Word a, Word b) {
            return (2 * b.d - b.total) - (2 * a.d - a.total);
        }
    }

    static class CompE implements Comparator<Word> {
        public int compare(Word a, Word b) {
            return (2 * b.e - b.total) - (2 * a.e - a.total);
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int t = scan.nextInt();

        while (t != 0) {
            t--;
            int n = scan.nextInt();
            Word[] words = new Word[n];

            for (int i = 0 ; i < n ; i++) {
                String s = scan.next();
                Word word = new Word();
                word.s = s;

                for (int j = 0 ; j < s.length() ; j++) {
                    if (s.charAt(j) == 'a') {
                        word.a++;
                    } else if (s.charAt(j) == 'b') {
                        word.b++;
                    } else if (s.charAt(j) == 'c') {
                        word.c++;
                    } else if (s.charAt(j) == 'd') {
                        word.d++;
                    } else if (s.charAt(j) == 'e') {
                        word.e++;
                    }

                    word.total++;
                }

                words[i] = word;
            }

            int answer = 0;
            int main = 0;
            int other = 0;
            int count = 0;
            Arrays.sort(words, new CompA());

            for (int i = 0 ; i < n ; i++) {
                main += words[i].a;
                other += words[i].total - words[i].a;

                if (main > other) {
                    count++;
                } else {
                    break;
                }
            }

            answer = Math.max(answer, count);
            main = 0;
            other = 0;
            count = 0;
            Arrays.sort(words, new CompB());

            for (int i = 0 ; i < n ; i++) {
                main += words[i].b;
                other += words[i].total - words[i].b;

                if (main > other) {
                    count++;
                } else {
                    break;
                }
            }

            answer = Math.max(answer, count);
            main = 0;
            other = 0;
            count = 0;
            Arrays.sort(words, new CompC());

            for (int i = 0 ; i < n ; i++) {
                main += words[i].c;
                other += words[i].total - words[i].c;

                if (main > other) {
                    count++;
                } else {
                    break;
                }
            }

            answer = Math.max(answer, count);
            main = 0;
            other = 0;
            count = 0;
            Arrays.sort(words, new CompD());

            for (int i = 0 ; i < n ; i++) {
                main += words[i].d;
                other += words[i].total - words[i].d;

                if (main > other) {
                    count++;
                } else {
                    break;
                }
            }

            answer = Math.max(answer, count);
            main = 0;
            other = 0;
            count = 0;
            Arrays.sort(words, new CompE());

            for (int i = 0 ; i < n ; i++) {
                main += words[i].e;
                other += words[i].total - words[i].e;

                if (main > other) {
                    count++;
                } else {
                    break;
                }
            }

            answer = Math.max(answer, count);
            System.out.println(answer);
        }

        scan.close();
    }
}
