package io.focuslauncher.phone.service;

import org.androidannotations.annotations.EBean;


/**
 * Created by Shahab on 1/10/2017.
 */

@EBean
public class ApiClient extends CoreAPIClient {

    @Override
    protected String getAppName() {
        return "siempo";
    }
}
