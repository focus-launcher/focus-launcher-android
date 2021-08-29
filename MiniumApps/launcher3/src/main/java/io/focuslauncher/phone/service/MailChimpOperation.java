package io.focuslauncher.phone.service;

import android.os.AsyncTask;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class MailChimpOperation extends AsyncTask<String, Void, String> {

    public enum EmailType {
        EMAIL_REG,
        CONTRIBUTOR_EMAIL
    }

    public EmailType emailType;
    private boolean isSubscribed;

    public MailChimpOperation(EmailType emailType) {
        this.emailType = emailType;
        this.isSubscribed = isSubscribed;
    }

    public MailChimpOperation(EmailType emailType, boolean isSubscribed) {
        this.emailType = emailType;
        this.isSubscribed = isSubscribed;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            OkHttpClient client = new OkHttpClient();
            String URL = "https://us14.api.mailchimp.com/3.0/lists/08cf9f0fe2/members/";
            String key = "YW55c3RyaW5nOmQ1ZmMyZTg1YWJiMThkZWE5ZjlhMTAyMGM4ZWYyODU5";
            String keyValue = "Basic " + key;

            String val_email = strings[0];
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = null;

            switch (emailType) {
                case EMAIL_REG:
                    body = RequestBody.create(mediaType, "{\"email_address\":\"" + val_email + "\",\"status\":\"subscribed\"}");
                    break;
                case CONTRIBUTOR_EMAIL:
                    if(isSubscribed) {
                        body = RequestBody.create(mediaType, "{\"contributor_email_address\":\"" + val_email + "\",\"status\":\"subscribed\"}");
                    } else {
                        body = RequestBody.create(mediaType, "{\"contributor_email_address\":\"" + val_email + "\",\"status\":\"un_subscribed\"}");
                    }
                    break;
            }

            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("authorization", keyValue)
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println("response call success: ");
        } catch (Exception e) {
            System.out.println("Exception storeToMailChimp");
            e.printStackTrace();
        }
        return "execute";
    }

}
