package monash.sydney.com.mytracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {RunItem.class}, version = 1, exportSchema = false)
@TypeConverters({RunItemConverter.class})

public abstract class RunItemDB extends RoomDatabase {

    //variables
    private static final String Name = "runitems";
    private static RunItemDB DBINSTANCE;

    public static RunItemDB getDatabase(Context context) {
        if (DBINSTANCE == null) {
            synchronized (MapsActivity.class) {
                DBINSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        RunItemDB.class, Name).build();
            }
        }
        return DBINSTANCE;
    }

    public static void destroyInstance() {
        DBINSTANCE = null;
    }

    public abstract RunItemDAO runItemDao();
}

