// Copyright (c) 2015, Intel Corporation
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// 1. Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// 3. Neither the name of the copyright holder nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package org.moe.samples.currencyconverter.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Currency {

    public static String getCurrencySymbol(String currencyIdentifier) {
        return getCurrencySymbol(currencyIdentifier, Locale.UK);
    }

    public static String getCurrencySymbol(String currencyIdentifier, Locale countryLocale) {
        Locale.setDefault(countryLocale);
        return java.util.Currency.getInstance(currencyIdentifier).getSymbol();
    }

    public static double getCurrentRate(String currencyIdentifierFrom, String currencyIdentifierTo) {
        String rate;

        if (currencyIdentifierFrom.equals(currencyIdentifierTo)) {
            return 1.0;
        }

        try {
            String sUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/" +
                    currencyIdentifierFrom.toLowerCase() + "/"+ currencyIdentifierTo.toLowerCase() +".json";
            rate = getRateFromURL(sUrl);
            if (rate.equals("N/A")) {
                return 0.0;
            }
            return Double.parseDouble(rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private static String getRateFromURL(String myURL) {
        System.out.println("Requested URL:" + myURL);
        StringBuilder sb = new StringBuilder();
        InputStreamReader in = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(myURL);
            URLConnection urlConn = url.openConnection();
            if (urlConn != null) {
                urlConn.setReadTimeout(2000);
                //urlConn.setConnectTimeout(5000);
            }
            if (urlConn != null) {
                inputStream = urlConn.getInputStream();
                if (inputStream != null) {
                    in = new InputStreamReader(inputStream,
                            Charset.defaultCharset());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                    in.close();
                }
            }
        } catch (Exception e) {
            //throw new RuntimeException("Exception while calling URL:"+ myURL, e);
            //Kill bufferedReader by timeout
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Pattern p = Pattern.compile(": (\\d+\\.\\d+)");
        Matcher m = p.matcher(sb.toString());
        if (!m.find()) {
            return "N/A";
        }
        String rate = m.group(1);
        double _rate = Double.parseDouble(rate);    // sanity check
        return rate;
    }

    public static double[] convert(double number, String currencyIdentifierFrom, String currencyIdentifierTo) {
        double rate = getCurrentRate(currencyIdentifierFrom, currencyIdentifierTo);
        if (Math.abs(rate) < 0.000001)
            return new double[]{0.0, 0.0};
        return new double[]{rate, number * rate};
    }
}
