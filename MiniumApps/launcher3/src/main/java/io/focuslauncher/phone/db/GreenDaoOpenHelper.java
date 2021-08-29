package io.focuslauncher.phone.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;


public class GreenDaoOpenHelper extends io.focuslauncher.phone.db.DaoMaster.OpenHelper {
    public GreenDaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // MigrationHelper.migrate(db,TableNotificationSmsDao.class);
        switch (oldVersion) {
            case 1:
                // no statement
            case 2:
//                MigrationHelper.migrate(db, StatusBarNotificationStorageDao.class);
            case 3:
                MigrationHelper.migrate(db, TableNotificationSmsDao.class);
                break;
            case 4:
                break;
        }

    }
}