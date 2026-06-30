package com.example.xplorenow.di;

import android.content.Context;

import com.example.xplorenow.payment.MockPaymentService;
import com.example.xplorenow.payment.PaymentStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class PaymentModule {

    @Provides
    @Singleton
    public PaymentStorage providePaymentStorage(@ApplicationContext Context context) {
        return new PaymentStorage(context);
    }

    @Provides
    @Singleton
    public MockPaymentService provideMockPaymentService() {
        return new MockPaymentService();
    }
}
