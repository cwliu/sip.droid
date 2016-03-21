/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.wiadvance.sipdemo.office365;

import com.wiadvance.sipdemo.RESTHelper;
import com.wiadvance.sipdemo.model.ContactRaw;

import retrofit.Callback;


/**
 * Handles the creation of the message and contacting the
 * mail service to send the message. The app must have
 * connected to Office 365 and discovered the mail service
 * endpoints before using the createDraftMail method.
 */
public class MSGraphAPIController {

    private static MSGraphAPIController INSTANCE;
    private RESTHelper mRESTHelper;
    private MSGraphAPIService mMSGraphAPIService;

    private MSGraphAPIController() {
        mRESTHelper = new RESTHelper();
    }

    public static synchronized MSGraphAPIController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MSGraphAPIController();
        }
        return INSTANCE;
    }


    public void showContacts(Callback<ContactRaw> callback){
        ensureService();
        mMSGraphAPIService.contacts(callback);
    }

    //Creates a Microsoft Graph API endpoint service interface if it does not exist.
    private void ensureService() {
        if (mMSGraphAPIService == null) {
            mMSGraphAPIService = mRESTHelper
                    .getRestAdapter()
                    .create(MSGraphAPIService.class);
        }
    }
}

// *********************************************************
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
