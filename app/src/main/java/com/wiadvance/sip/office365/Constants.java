package com.wiadvance.sip.office365;

public interface Constants {
    String AUTHORITY_URL = "https://login.microsoftonline.com/common";
    // Update these two constants with the values for your application:
    String CLIENT_ID = "bc90ca19-2bf5-4f7d-abfd-9ae94f1241dc";
    String REDIRECT_URI = "http://localhost/sip";
    String MICROSOFT_GRAPH_API_ENDPOINT = "https://graph.microsoft.com/v1.0/";
    String MICROSOFT_GRAPH_API_ENDPOINT_RESOURCE_ID = "https://graph.microsoft.com/";
    String USER_PHOTO_URL_FORMAT = Constants.MICROSOFT_GRAPH_API_ENDPOINT + "Users('%s')/photo/$value";
    String MY_PHOTO_URL = Constants.MICROSOFT_GRAPH_API_ENDPOINT + "me/photo/$value";
}
