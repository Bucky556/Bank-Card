package code.uz.bankcard.util;

public class PageUtil {
    public static int getCurrentPage(int page) {
        return page <= 0 ? 0 : page - 1;
    }
}
