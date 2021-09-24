package io.focuslauncher.phone.service;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.NotifyBottomView;
import io.focuslauncher.phone.event.NotifyToolView;
import io.focuslauncher.phone.main.MainListItemLoader;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.utils.PackageUtil;
import de.greenrobot.event.EventBus;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class LoadToolPane extends AsyncTask<String, String, ArrayList<MainListItem>> {

    ArrayList<MainListItem> bottomDockList;

    public LoadToolPane() {
        bottomDockList = new ArrayList<>();
    }

    @Override
    protected ArrayList<MainListItem> doInBackground(String... strings) {
        ArrayList<MainListItem> items = new ArrayList<>();
        ArrayList<MainListItem> items1 = new ArrayList<>();

        try {
            new MainListItemLoader().loadItemsDefaultApp(items);
            items = PackageUtil.getToolsMenuData(items);
            Set<Integer> list = new HashSet<>();

            if (null != CoreApplication.Companion.getInstance() && null != CoreApplication.Companion.getInstance().getToolsSettings()) {
                for (Map.Entry<Integer, AppMenu> entry : CoreApplication.Companion.getInstance().getToolsSettings().entrySet()) {
                    if (entry.getValue().isBottomDoc()) {
                        list.add(entry.getKey());
                    }
                }
            }

            for (MainListItem mainListItem : items) {
                if (list.contains(mainListItem.getId())) {
                    bottomDockList.add(mainListItem);
                } else {
//                    if (items1.size() < 12) {
                        items1.add(mainListItem);
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return items1;
    }

    @Override
    protected void onPostExecute(ArrayList<MainListItem> s) {
        super.onPostExecute(s);


        try {
//            sortingMenu(s);

            CoreApplication.Companion.getInstance().setToolItemsList(s);
            CoreApplication.Companion.getInstance().setToolBottomItemsList(bottomDockList);
            EventBus.getDefault().postSticky(new NotifyBottomView(true));
            EventBus.getDefault().postSticky(new NotifyToolView(true));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void sortingMenu(ArrayList<MainListItem> s) {
//        if (TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo
//                        .SORTED_MENU,
//                ""))) {
//
//            ArrayList<Long> sortedId = new ArrayList<>();
//            for (MainListItem mainListItem : s) {
//                sortedId.add((long) mainListItem.getId());
//
//            }
//            for (MainListItem mainListItem : bottomDockList) {
//                sortedId.add((long) mainListItem.getId());
//            }
//
//            Gson gson = new Gson();
//            String jsonListOfSortedCustomerIds = gson.toJson(sortedId);
//            PrefSiempo.getInstance(context).write(PrefSiempo.SORTED_MENU,
//                    jsonListOfSortedCustomerIds);
//
//
//            HashMap<Integer, AppMenu> integerAppMenuHashMap = CoreApplication
//                    .getInstance().getToolsSettings();
//
//            Iterator it = integerAppMenuHashMap.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry) it.next();
//                int id = (int) pair.getKey();
//                if (!sortedId.contains((long) id)) {
//                    ((AppMenu) pair.getValue()).setVisible(false);
//                }
//            }
//
//            String hashMapToolSettings = new Gson().toJson(integerAppMenuHashMap);
//            PrefSiempo.getInstance(context).write(PrefSiempo.TOOLS_SETTING,
//                    hashMapToolSettings);
//
//
//        } else {
//
//
//            String jsonListOfSortedToolsId = PrefSiempo.getInstance(context).read
//                    (PrefSiempo.SORTED_MENU, "");
//            //check for null
//            if (!jsonListOfSortedToolsId.isEmpty()) {
//
//                //loop through added ids
//
//                //convert onNoteListChangedJSON array into a List<Long>
//                Gson gson = new GsonBuilder()
//                        .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
//                List<Long> listOfSortedCustomersId = gson.fromJson(jsonListOfSortedToolsId, new TypeToken<List<Long>>() {
//                }.getType());
//
//
//                if (listOfSortedCustomersId.size() > 16) {
//
//                    List<Long> listOfToolsId = new ArrayList<>();
//                    List<Long> listOfRemoveId = new ArrayList<>();
//                    ArrayList<MainListItem> listItems = new ArrayList<>();
//                    listItems.addAll(s);
//                    listItems.addAll(bottomDockList);
//
//
//                    for (MainListItem listItem : listItems) {
//
//                        listOfToolsId.add((long) listItem.getId());
//                    }
//
//                    listOfRemoveId.addAll(listOfSortedCustomersId);
//                    listOfRemoveId.removeAll(listOfToolsId);
//
//                    listOfSortedCustomersId.removeAll(listOfRemoveId);
//
//
//                    String jsonListOfSortedCustomerIds = gson.toJson
//                            (listOfSortedCustomersId);
//                    PrefSiempo.getInstance(context).write(PrefSiempo.SORTED_MENU,
//                            jsonListOfSortedCustomerIds);
//
//
//                    HashMap<Integer, AppMenu> integerAppMenuHashMap = CoreApplication
//                            .getInstance().getToolsSettings();
//                    for (Long aLong : listOfRemoveId) {
//                        int id = aLong.intValue();
//                        integerAppMenuHashMap.get(id).setVisible
//                                (false);
//                    }
//
//
//                    String hashMapToolSettings = new Gson().toJson(integerAppMenuHashMap);
//                    PrefSiempo.getInstance(context).write(PrefSiempo.TOOLS_SETTING,
//                            hashMapToolSettings);
//
//
//
//                }
//
//
//            }
//
//
//        }
//    }


}
