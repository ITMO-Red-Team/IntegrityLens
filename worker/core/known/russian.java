public class Main {
    static final String[] MONTHS = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь"};

    public static void main(String[] args) {
        String prompt = "Введите номер месяца и нажмите клавишу ввода:";
        System.out.println(prompt);
        int total = 0;
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].length() > 4) {
                total += MONTHS[i].length();
            }
        }
        System.out.println(total);
    }
}
