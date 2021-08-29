package io.focuslauncher.phone.screenfilter;


import android.content.Context;
import android.content.Intent;

import io.focuslauncher.phone.service.ScreenFilterService;

public class FilterCommandFactory {

    private Context mContext;

    public FilterCommandFactory(Context context) {
        mContext = context;
    }

    /**
     *
     * @param screenFilterServiceCommand one of {@link ScreenFilterService#COMMAND_OFF},
     *        {@link ScreenFilterService#COMMAND_ON}, or {@link ScreenFilterService#COMMAND_PAUSE}.
     * @return an Intent containing a command that can be sent to {@link ScreenFilterService} via
     *         {@link FilterCommandSender#send(Intent)}; null if
     *         {@code screenFilterServiceCommand} is invalid.
     */
    public Intent createCommand(int screenFilterServiceCommand) {
        Intent command;

        if (screenFilterServiceCommand < ScreenFilterService.VALID_COMMAND_START ||
                screenFilterServiceCommand > ScreenFilterService.VALID_COMMAND_END) {
            command = null;
        } else {
            command = new Intent(mContext, ScreenFilterService.class);
            command.putExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, screenFilterServiceCommand);
        }

        return command;
    }
}
