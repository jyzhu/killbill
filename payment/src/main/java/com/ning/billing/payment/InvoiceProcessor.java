/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.payment;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.ning.billing.account.api.IAccount;
import com.ning.billing.account.api.IAccountUserApi;
import com.ning.billing.invoice.model.Invoice;
import com.ning.billing.payment.provider.PaymentProviderPluginRegistry;
import com.ning.billing.util.eventbus.IEventBus.EventBusException;

public class InvoiceProcessor {
    public static final String PAYMENT_PROVIDER_KEY = "paymentProvider";
    private final IAccountUserApi accountUserApi;
    private final PaymentProviderPluginRegistry pluginRegistry;

    @Inject
    public InvoiceProcessor(IAccountUserApi accountUserApi, PaymentProviderPluginRegistry pluginRegistry) {
        this.accountUserApi = accountUserApi;
        this.pluginRegistry = pluginRegistry;
    }

    @Subscribe
    public void receiveInvoice(Invoice invoice) throws EventBusException {
        final IAccount account = accountUserApi.getAccountFromId(invoice.getAccountId());
        final String paymentProviderName = account.getFieldValue(PAYMENT_PROVIDER_KEY);

        pluginRegistry.getPlugin(paymentProviderName).processInvoice(account, invoice);
    }
}
