package io.focuslauncher.phone.screenfilter;

import android.content.Intent;

import io.focuslauncher.phone.service.ScreenFilterService;

public class FilterCommandParser {

    /**
     * Retrieves the command in an intent sent to {@link ScreenFilterService}.
     *
     * @param intent that was constructed by {@link FilterCommandFactory}.
     * @return one of {@link ScreenFilterService#COMMAND_OFF}, {@link ScreenFilterService#COMMAND_ON},
     *         {@link ScreenFilterService#COMMAND_PAUSE}, or -1 if {@code intent} doesn't contain a
     *         valid command.
     */
    public int parseCommandFlag(Intent intent) {
        int errorCode = -1;

        if (intent == null) {
            return errorCode;
        }

        int commandFlag = intent.getIntExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, errorCode);
        return commandFlag;
    }
}
