/*
*  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
*/
package com.wiadvance.sip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wiadvance.sip.office365.AuthenticationManager;
import com.wiadvance.sip.office365.Constants;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RESTHelper {



    /**
     * Returns a retrofit rest adaptor class. The adaptor is created in calling code.
     *
     * @return A new RestAdapter instance.
     */
    public RestAdapter getRestAdapter() {
        //This method catches outgoing REST calls and injects the Authorization and host headers before
        //sending to REST endpoint
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                final String token = AuthenticationManager.getInstance().getAccessToken();
                if (null != token) {
                    request.addHeader("Authorization", "Bearer " + token);
                }
            }
        };

        Gson gson = new GsonBuilder().create();

        //Sets required properties in rest adaptor class before it is created.
        return new RestAdapter.Builder()
                .setEndpoint(Constants.MICROSOFT_GRAPH_API_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(requestInterceptor)
                .build();
    }

}// *********************************************************
//
// O365-Android-Microsoft-Graph-Connect, https://github.com/OfficeDev/O365-Android-Microsoft-Graph-Connect
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************
