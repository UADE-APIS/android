package com.example.xplorenow.payment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public final class PaymentUtils {

    private PaymentUtils() {
    }

    public static String formatAmount(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "AR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
        return decimalFormat.format(amount);
    }

    public static String normalizeCardNumber(String raw) {
        return raw == null ? "" : raw.replaceAll("\\s+", "");
    }

    public static String maskCard(String raw) {
        String number = normalizeCardNumber(raw);
        if (number.length() < 4) return "****";
        return "**** **** **** " + number.substring(number.length() - 4);
    }

    public static boolean isValidExpiryDate(String raw) {
        if (raw == null || !raw.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }

        int month = Integer.parseInt(raw.substring(0, 2));
        int year = Integer.parseInt(raw.substring(3, 5));

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR) % 100;
        int currentMonth = now.get(Calendar.MONTH) + 1;

        return year > currentYear || (year == currentYear && month >= currentMonth);
    }
}
