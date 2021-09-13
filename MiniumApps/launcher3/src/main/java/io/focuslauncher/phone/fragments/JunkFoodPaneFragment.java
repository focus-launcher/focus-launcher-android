package io.focuslauncher.phone.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.JunkFoodPaneAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.customviews.ItemOffsetDecoration;
import io.focuslauncher.phone.event.NotifyJunkFoodView;
import io.focuslauncher.phone.util.AppUtils;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;


public class JunkFoodPaneFragment extends CoreFragment {

    private View view;
    private RecyclerView recyclerView;
    private ArrayList<String> items = new ArrayList<>();
    private JunkFoodPaneAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemOffsetDecoration itemDecoration;
    private Set<String> junkFoodList = new HashSet<>();
    private LinearLayout linSelectJunkFood;
    private Button btnSelect;


    public JunkFoodPaneFragment() {
        // Required empty public constructor
    }

    public static JunkFoodPaneFragment newInstance() {
        return new JunkFoodPaneFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_junkfood_pane, container, false);
        Log.d("Test", "J1");
        initView();
        Log.d("Test", "J2");
        return view;

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
       /* if (CoreApplication.getInstance().isRandomize()) {
            Collections.shuffle(CoreApplication.getInstance().getJunkFoodList());
            items = CoreApplication.getInstance().getJunkFoodList();*/
            if (mAdapter != null) {
                mAdapter.setMainListItemList(items, CoreApplication.getInstance().isHideIconBranding());
            }
        // }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void onEvent(NotifyJunkFoodView junkFoodView) {
        if (junkFoodView != null && junkFoodView.isNotify()) {
            items = CoreApplication.getInstance().getJunkFoodList();
            mAdapter.setMainListItemList(items, CoreApplication.getInstance().isHideIconBranding());
            EventBus.getDefault().removeStickyEvent(junkFoodView);
        }

    }

    private void initView() {
        if (getActivity() != null && view != null) {
            linSelectJunkFood = view.findViewById(R.id.linSelectJunkFood);
            btnSelect = view.findViewById(R.id.btnSelect);
            recyclerView = view.findViewById(R.id.recyclerView);
            junkFoodList = new HashSet<>();
            items = new ArrayList<>(junkFoodList);
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            recyclerView.setLayoutManager(mLayoutManager);
            if (itemDecoration != null) {
                recyclerView.removeItemDecoration(itemDecoration);
            }
            itemDecoration = new ItemOffsetDecoration(context, R.dimen.dp_10);
            recyclerView.addItemDecoration(itemDecoration);
            mAdapter = new JunkFoodPaneAdapter(getActivity(), items, CoreApplication.getInstance().isHideIconBranding());
            recyclerView.setAdapter(mAdapter);


        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            // do something when visible.
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
            if (CoreApplication.getInstance().isRandomize()) {
                Collections.shuffle(CoreApplication.getInstance().getJunkFoodList());
                items = CoreApplication.getInstance().getJunkFoodList();
            }
        }if(getActivity() != null)
        {
            AppUtils.notificationBarManaged(getActivity(), null);
        }

    }
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null)
        {
            mAdapter.notifyDataSetChanged();
        }
    }

}
