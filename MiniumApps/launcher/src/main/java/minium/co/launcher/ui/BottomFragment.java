package minium.co.launcher.ui;


import android.support.v4.app.Fragment;
import android.widget.Button;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import minium.co.core.util.UIUtils;
import minium.co.launcher.R;
import minium.co.launcher.helper.ActivityHelper;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_bottom)
public class BottomFragment extends Fragment {

    @ViewById
    Button btn1;

    @ViewById
    Button btn2;

    @ViewById
    Button btn3;


    public BottomFragment() {
        // Required empty public constructor
    }

    @AfterViews
    void afterViews() {
        btn1.setText("CALL");
        btn2.setText("SELECT");
        btn3.setText("MESSAGES");
    }

    @Click
    void btn1() {
        // opening dialer app
        if (!new ActivityHelper(getContext()).openDialerApp())
            UIUtils.alert(getContext(), getString(R.string.msg_not_yet_implemented));
    }

    @Click
    void btn2() {
        UIUtils.alert(getContext(), getString(R.string.msg_not_yet_implemented));
    }

    @Click
    void btn3() {
        // opening messages app
        if (!new ActivityHelper(getContext()).openMessagingApp())
            UIUtils.alert(getContext(), getString(R.string.msg_not_yet_implemented));
    }
}