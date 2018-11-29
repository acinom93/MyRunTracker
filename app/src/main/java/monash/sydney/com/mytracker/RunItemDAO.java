package monash.sydney.com.mytracker;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RunItemDAO {

    @Query("SELECT * FROM runitems ORDER BY date DESC")
    List<RunItem> listAll();

    @Insert
    void insert(RunItem toDoItem);
}
