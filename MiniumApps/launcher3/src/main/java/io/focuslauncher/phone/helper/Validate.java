package io.focuslauncher.phone.helper;

import android.annotation.SuppressLint;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Pattern;

import io.focuslauncher.R;


/**
 * Simple validation methods. Designed for jsoup internal use
 */
public final class Validate {

    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PASSWORD_REGEX = "^[0-9a-zA-Z@#$%]{8,}$";
    @SuppressLint("StaticFieldLeak")
    public static View errorView;

    private Validate() {
    }

    /**
     * Validates that the object is not null
     *
     * @param obj object to test
     */
    public static void notNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Object must not be null");
    }

    /**
     * Validates that the object is not null
     *
     * @param obj object to test
     * @param msg message to output if validation fails
     */
    public static void notNull(Object obj, String msg) {
        if (obj == null)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the value is true
     *
     * @param val object to test
     */
    public static void isTrue(boolean val) {
        if (!val)
            throw new IllegalArgumentException("Must be true");
    }

    /**
     * Validates that the value is true
     *
     * @param val object to test
     * @param msg message to output if validation fails
     */
    public static void isTrue(boolean val, String msg) {
        if (!val)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the value is false
     *
     * @param val object to test
     */
    public static void isFalse(boolean val) {
        if (val)
            throw new IllegalArgumentException("Must be false");
    }

    /**
     * Validates that the value is false
     *
     * @param val object to test
     * @param msg message to output if validation fails
     */
    public static void isFalse(boolean val, String msg) {
        if (val)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the array contains no null elements
     *
     * @param objects the array to test
     */
    public static void noNullElements(Object[] objects) {
        noNullElements(objects, "Array must not contain any null objects");
    }

    /**
     * Validates that the array contains no null elements
     *
     * @param objects the array to test
     * @param msg     message to output if validation fails
     */
    public static void noNullElements(Object[] objects, String msg) {
        for (Object obj : objects)
            if (obj == null)
                throw new IllegalArgumentException(msg);
    }

    /**
     * Validates that the string is not empty
     *
     * @param string the string to test
     */
    public static void notEmpty(String string) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException("String must not be empty");
    }

    /**
     * Validates that the string is not empty
     *
     * @param string the string to test
     * @param msg    message to output if validation fails
     */
    public static void notEmpty(String string, String msg) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException(msg);
    }

    /**
     * Cause a failure.
     *
     * @param msg message to output.
     */
    public static void fail(String msg) {
        throw new IllegalArgumentException(msg);
    }

    private static boolean isAvailable(TextInputLayout editTextLayout) {
        editTextLayout.setErrorEnabled(false);
        EditText editText = editTextLayout.getEditText();
        notNull(editText);
        return editText.isEnabled();
    }

    private static boolean checkRegex(String pattern, String text) {
        return Pattern.matches(pattern, text);
    }

    private static void setError(TextInputLayout editTextLayout, int resError) {
        editTextLayout.setError(editTextLayout.getContext().getString(resError));
        notNull(editTextLayout.getEditText());
        editTextLayout.getEditText().requestFocus();
        errorView = editTextLayout.getEditText();
    }

    /**
     * @param editTextLayout
     * @return true, if validation passed
     */
    public static boolean checkRequiredField(TextInputLayout editTextLayout) {
        if (isAvailable(editTextLayout)) {
            notNull(editTextLayout.getEditText());
            if (editTextLayout.getEditText().getText().length() == 0) {
                setError(editTextLayout, R.string.error_requiredField);
                return false;
            }
        }
        return true;
    }

    public static boolean isValidEmail(TextInputLayout editTextLayout) {
        if (isAvailable(editTextLayout)) {
            notNull(editTextLayout.getEditText());
            if (!checkRegex(EMAIL_REGEX, editTextLayout.getEditText().getText().toString())) {
                setError(editTextLayout, R.string.error_invalidEmail);
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPassword(TextInputLayout editTextLayout) {
        if (isAvailable(editTextLayout)) {
            notNull(editTextLayout.getEditText());
            if (!checkRegex(PASSWORD_REGEX, editTextLayout.getEditText().getText().toString())) {
                setError(editTextLayout, R.string.error_invalidPassword);
                return false;
            }
        }
        return true;
    }

    public static boolean isPasswordMismatch(TextInputLayout editTextLayout, TextInputLayout follower) {
        if (isAvailable(editTextLayout) && isAvailable(follower)) {
            notNull(editTextLayout.getEditText());
            notNull(follower.getEditText());
            if (!editTextLayout.getEditText().getText().toString().equals(follower.getEditText().getText().toString())) {
                setError(follower, R.string.error_mismatchPassword);
                return false;
            }
        }
        return true;
    }
}
