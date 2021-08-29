package io.focuslauncher.phone.token;

import android.content.Context;
import android.os.Build;

import io.focuslauncher.phone.main.MainFragmentMediator;
import io.focuslauncher.phone.util.ContactSmsPermissionHelper;

/**
 * Created by shahab on 2/16/17.
 */
public class TokenParser {

    private TokenRouter router;
    private Context context;
    private MainFragmentMediator mediator;

    public TokenParser(TokenRouter router, Context context,
                       MainFragmentMediator mediator) {
        this.router = router;
        this.context = context;
        this.mediator = mediator;

    }


    public void parse(String str) {

        try {
            if (str.isEmpty()) {
                TokenManager.getInstance().clear();
            } else if (str.equals("@") && !TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ContactSmsPermissionHelper
                            contactSmsPermissionHelper = new
                            ContactSmsPermissionHelper(router,
                            context, mediator, true, null);
                    contactSmsPermissionHelper.checkForContactAndSMSPermission();
                } else {
                    router.setCurrent(new TokenItem(TokenItemType.CONTACT));
                }


            } else {

                for (TokenItem item : TokenManager.getInstance().getItems()) {
                    if (item.getCompleteType() == TokenCompleteType.FULL) {
                        str = str.substring(item.getTitle().length() + 1);
                    }
                }
                if (str.endsWith("@") && TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT)) {
                    router.add(new TokenItem(TokenItemType.CONTACT));
                } else {
                    TokenManager.getInstance().getCurrent().setTitle(str);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
