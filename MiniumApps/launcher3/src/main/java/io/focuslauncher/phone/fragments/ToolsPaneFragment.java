package io.focuslauncher.phone.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.ToolsMenuAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.customviews.ItemOffsetDecoration;
import io.focuslauncher.phone.event.NotifyToolView;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.util.AppUtils;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;


public class ToolsPaneFragment extends CoreFragment {

    private View view;
    private RecyclerView recyclerView;
    private ArrayList<MainListItem> items = new ArrayList<>();
    private ToolsMenuAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Parcelable mListState;
    private ItemOffsetDecoration itemDecoration;

    public ToolsPaneFragment() {
        // Required empty public constructor
    }

    public static ToolsPaneFragment newInstance() {
        return new ToolsPaneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tools_pane, container, false);
        Log.d("Test", "T1");
        recyclerView = view.findViewById(R.id.recyclerView);
        initView();
        Log.d("Test", "T2");
        return view;

    }
    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null)
        {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void onEvent(NotifyToolView notifyToolView) {
        if (notifyToolView != null && notifyToolView.isNotify()) {
            items = CoreApplication.getInstance().getToolItemsList();
            mAdapter = new ToolsMenuAdapter(getActivity(), CoreApplication.getInstance().isHideIconBranding(), false, items);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            EventBus.getDefault().removeStickyEvent(notifyToolView);
        }

    }

    private void initView() {
        if (getActivity() != null && recyclerView != null) {
            items = CoreApplication.getInstance().getToolItemsList();
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            recyclerView.setLayoutManager(mLayoutManager);
            if (itemDecoration != null) {
                recyclerView.removeItemDecoration(itemDecoration);
            }
            itemDecoration = new ItemOffsetDecoration(context, R.dimen.dp_10);
            recyclerView.addItemDecoration(itemDecoration);
            mAdapter = new ToolsMenuAdapter(getActivity(), CoreApplication.getInstance().isHideIconBranding(), false, items);
            recyclerView.setAdapter(mAdapter);
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser  && getActivity() != null)
        {
            AppUtils.notificationBarManaged(getActivity(), null);
        }
    }

}
