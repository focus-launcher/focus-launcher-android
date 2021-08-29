package io.focuslauncher.phone.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.FavoritesSelectionActivity;
import io.focuslauncher.phone.adapters.FavoritesPaneAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.customviews.ItemOffsetDecoration;
import io.focuslauncher.phone.event.NotifyFavortieView;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.util.AppUtils;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;


public class FavoritePaneFragment extends CoreFragment {

    private View view;
    private RecyclerView recyclerView;
    private ArrayList<MainListItem> items = new ArrayList<>();
    private FavoritesPaneAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemOffsetDecoration itemDecoration;
    private LinearLayout linSelectFavouriteFood;
    private Button btnSelect;

    public FavoritePaneFragment() {
        // Required empty public constructor
    }

    public static FavoritePaneFragment newInstance() {
        return new FavoritePaneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite_pane, container, false);
        initView();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && getActivity() != null){

            AppUtils.notificationBarManaged(getActivity(), null);
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void onEvent(NotifyFavortieView notifyFavortieView) {
        if (notifyFavortieView != null && notifyFavortieView.isNotify()) {
            items = CoreApplication.getInstance().getFavoriteItemsList();
            mAdapter.setMainListItemList(items, CoreApplication.getInstance().isHideIconBranding());
            checkSize();
            EventBus.getDefault().removeStickyEvent(notifyFavortieView);
        }
    }


    private void initView() {
        if (getActivity() != null && view != null) {
            items = CoreApplication.getInstance().getFavoriteItemsList();
            recyclerView = view.findViewById(R.id.recyclerView);
            btnSelect = view.findViewById(R.id.btnSelect);
            btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FavoritesSelectionActivity
                            .class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                }
            });
            linSelectFavouriteFood = view.findViewById(R.id.linSelectFavouriteFood);
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            recyclerView.setLayoutManager(mLayoutManager);
            if (itemDecoration != null) {
                recyclerView.removeItemDecoration(itemDecoration);
            }
            itemDecoration = new ItemOffsetDecoration(context, R.dimen.dp_10);
            recyclerView.addItemDecoration(itemDecoration);
            mAdapter = new FavoritesPaneAdapter(getActivity(), CoreApplication.getInstance().isHideIconBranding(), false, items);
            recyclerView.setAdapter(mAdapter);

            checkSize();

        }
    }

    private void checkSize() {
        if (items.size() > 0) {
            boolean containsFavourites = false;
            for (MainListItem item : items) {
                if (!TextUtils.isEmpty(item.getTitle())) {
                    containsFavourites = true;
                    break;
                }
            }
            if (containsFavourites) {
                linSelectFavouriteFood.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

            } else {
                linSelectFavouriteFood.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }


        } else {
            linSelectFavouriteFood.setVisibility(View.VISIBLE);
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


}
