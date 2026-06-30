package com.example.xplorenow.payment;

import android.os.Handler;
import android.os.Looper;

import com.example.xplorenow.data.model.PaymentRequest;
import com.example.xplorenow.data.model.PaymentResult;

public class MockPaymentService {

    public interface Callback {
        void onResult(PaymentResult result);
    }

    public void processPayment(PaymentRequest request, Callback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String cardNumber = PaymentUtils.normalizeCardNumber(request.getCard().getCardNumber());
            char lastDigit = cardNumber.isEmpty() ? '1' : cardNumber.charAt(cardNumber.length() - 1);
            boolean approved = Character.isDigit(lastDigit) && ((lastDigit - '0') % 2 == 0);

            if (approved) {
                callback.onResult(new PaymentResult(
                        true,
                        "APPROVED",
                        "Pago aprobado correctamente.",
                        PaymentUtils.maskCard(cardNumber)
                ));
            } else {
                callback.onResult(new PaymentResult(
                        false,
                        "REJECTED",
                        "La tarjeta fue rechazada por la API ficticia de pago.",
                        PaymentUtils.maskCard(cardNumber)
                ));
            }
        }, 1200);
    }
}
