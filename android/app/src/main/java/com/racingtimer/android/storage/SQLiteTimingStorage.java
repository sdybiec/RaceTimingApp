package com.racingtimer.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.storage.StorageCallback;
import com.racingtimer.storage.TimingStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SQLite implementation of TimingStorage for Android.
 */
public class SQLiteTimingStorage extends SQLiteOpenHelper implements TimingStorage {

    private static final String DATABASE_NAME = "race_timer.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_TIMING_ENTRIES = "timing_entries";
    private static final String TABLE_RACES = "races";
    private static final String TABLE_CREWS = "crews";

    // Common columns
    private static final String COLUMN_ID = "id";

    // Timing entry columns
    private static final String COLUMN_SEQUENCE = "sequence_number";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_BIB_NUMBER = "bib_number";
    private static final String COLUMN_CREW_ID = "crew_id";
    private static final String COLUMN_CREW_NAME = "crew_name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_RACE_ID = "race_id";
    private static final String COLUMN_RACE_CLOCK_MILLIS = "race_clock_millis";
    private static final String COLUMN_SYNCED = "synced";

    // Race columns
    private static final String COLUMN_RACE_NAME = "race_name";
    private static final String COLUMN_SCHEDULED_START = "scheduled_start";
    private static final String COLUMN_ACTUAL_START = "actual_start";
    private static final String COLUMN_STATUS = "status";

    // Crew columns
    private static final String COLUMN_BIB = "bib_number";
    private static final String COLUMN_NAME = "name";

    public SQLiteTimingStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create timing_entries table
        db.execSQL("CREATE TABLE " + TABLE_TIMING_ENTRIES + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_SEQUENCE + " INTEGER," +
                COLUMN_TIMESTAMP + " INTEGER," +
                COLUMN_BIB_NUMBER + " INTEGER," +
                COLUMN_CREW_ID + " TEXT," +
                COLUMN_CREW_NAME + " TEXT," +
                COLUMN_CATEGORY + " TEXT," +
                COLUMN_RACE_ID + " TEXT," +
                COLUMN_RACE_CLOCK_MILLIS + " INTEGER," +
                COLUMN_SYNCED + " INTEGER DEFAULT 0)");

        // Create races table
        db.execSQL("CREATE TABLE " + TABLE_RACES + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_RACE_NAME + " TEXT," +
                COLUMN_SCHEDULED_START + " INTEGER," +
                COLUMN_ACTUAL_START + " INTEGER," +
                COLUMN_STATUS + " TEXT)");

        // Create crews table
        db.execSQL("CREATE TABLE " + TABLE_CREWS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_BIB + " INTEGER," +
                COLUMN_NAME + " TEXT," +
                COLUMN_CATEGORY + " TEXT," +
                COLUMN_RACE_ID + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMING_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREWS);
        onCreate(db);
    }

    @Override
    public void saveTimingEntry(TimingEntry entry, StorageCallback<TimingEntry> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, entry.getId());
            values.put(COLUMN_SEQUENCE, entry.getSequenceNumber());
            values.put(COLUMN_TIMESTAMP, entry.getTimestamp().getTime());
            values.put(COLUMN_BIB_NUMBER, entry.getBibNumber());
            values.put(COLUMN_CREW_ID, entry.getCrewId());
            values.put(COLUMN_CREW_NAME, entry.getCrewName());
            values.put(COLUMN_CATEGORY, entry.getCategory());
            values.put(COLUMN_RACE_ID, entry.getRaceId());
            values.put(COLUMN_RACE_CLOCK_MILLIS, entry.getRaceClockMillis());
            values.put(COLUMN_SYNCED, entry.isSynced() ? 1 : 0);

            db.insertWithOnConflict(TABLE_TIMING_ENTRIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            callback.onSuccess(entry);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getTimingEntries(String raceId, StorageCallback<List<TimingEntry>> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_TIMING_ENTRIES, null,
                    COLUMN_RACE_ID + "=?", new String[]{raceId},
                    null, null, COLUMN_SEQUENCE + " ASC");

            List<TimingEntry> entries = new ArrayList<>();
            while (cursor.moveToNext()) {
                entries.add(cursorToTimingEntry(cursor));
            }
            cursor.close();

            callback.onSuccess(entries);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getUnsyncedTimingEntries(StorageCallback<List<TimingEntry>> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_TIMING_ENTRIES, null,
                    COLUMN_SYNCED + "=0", null,
                    null, null, COLUMN_TIMESTAMP + " ASC");

            List<TimingEntry> entries = new ArrayList<>();
            while (cursor.moveToNext()) {
                entries.add(cursorToTimingEntry(cursor));
            }
            cursor.close();

            callback.onSuccess(entries);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteTimingEntry(String id, StorageCallback<Void> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_TIMING_ENTRIES, COLUMN_ID + "=?", new String[]{id});
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void saveRace(Race race, StorageCallback<Race> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, race.getId());
            values.put(COLUMN_RACE_NAME, race.getName());
            values.put(COLUMN_SCHEDULED_START, race.getScheduledStartTime().getTime());
            if (race.getActualStartTime() != null) {
                values.put(COLUMN_ACTUAL_START, race.getActualStartTime().getTime());
            }
            values.put(COLUMN_STATUS, race.getStatus().name());

            db.insertWithOnConflict(TABLE_RACES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            callback.onSuccess(race);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getRace(String raceId, StorageCallback<Race> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_RACES, null,
                    COLUMN_ID + "=?", new String[]{raceId},
                    null, null, null);

            Race race = null;
            if (cursor.moveToFirst()) {
                race = cursorToRace(cursor);
            }
            cursor.close();

            callback.onSuccess(race);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getAllRaces(StorageCallback<List<Race>> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_RACES, null, null, null,
                    null, null, COLUMN_SCHEDULED_START + " ASC");

            List<Race> races = new ArrayList<>();
            while (cursor.moveToNext()) {
                races.add(cursorToRace(cursor));
            }
            cursor.close();

            callback.onSuccess(races);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteRace(String raceId, StorageCallback<Void> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_RACES, COLUMN_ID + "=?", new String[]{raceId});
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void saveCrew(Crew crew, StorageCallback<Crew> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, crew.getId());
            values.put(COLUMN_BIB, crew.getBibNumber());
            values.put(COLUMN_NAME, crew.getName());
            values.put(COLUMN_CATEGORY, crew.getCategory());
            values.put(COLUMN_RACE_ID, crew.getRaceId());

            db.insertWithOnConflict(TABLE_CREWS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            callback.onSuccess(crew);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrew(String crewId, StorageCallback<Crew> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_CREWS, null,
                    COLUMN_ID + "=?", new String[]{crewId},
                    null, null, null);

            Crew crew = null;
            if (cursor.moveToFirst()) {
                crew = cursorToCrew(cursor);
            }
            cursor.close();

            callback.onSuccess(crew);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrewByBibNumber(int bibNumber, String raceId, StorageCallback<Crew> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_CREWS, null,
                    COLUMN_BIB + "=? AND " + COLUMN_RACE_ID + "=?",
                    new String[]{String.valueOf(bibNumber), raceId},
                    null, null, null);

            Crew crew = null;
            if (cursor.moveToFirst()) {
                crew = cursorToCrew(cursor);
            }
            cursor.close();

            callback.onSuccess(crew);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrewsForRace(String raceId, StorageCallback<List<Crew>> callback) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_CREWS, null,
                    COLUMN_RACE_ID + "=?", new String[]{raceId},
                    null, null, COLUMN_BIB + " ASC");

            List<Crew> crews = new ArrayList<>();
            while (cursor.moveToNext()) {
                crews.add(cursorToCrew(cursor));
            }
            cursor.close();

            callback.onSuccess(crews);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteCrew(String crewId, StorageCallback<Void> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_CREWS, COLUMN_ID + "=?", new String[]{crewId});
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void clear(StorageCallback<Void> callback) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_TIMING_ENTRIES, null, null);
            db.delete(TABLE_RACES, null, null);
            db.delete(TABLE_CREWS, null, null);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Helper methods
    private TimingEntry cursorToTimingEntry(Cursor cursor) {
        TimingEntry entry = new TimingEntry();
        entry.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        entry.setSequenceNumber(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEQUENCE)));
        entry.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))));

        int bibIndex = cursor.getColumnIndexOrThrow(COLUMN_BIB_NUMBER);
        if (!cursor.isNull(bibIndex)) {
            entry.setBibNumber(cursor.getInt(bibIndex));
        }

        entry.setCrewId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREW_ID)));
        entry.setCrewName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREW_NAME)));
        entry.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
        entry.setRaceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RACE_ID)));
        entry.setRaceClockMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RACE_CLOCK_MILLIS)));
        entry.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1);

        return entry;
    }

    private Race cursorToRace(Cursor cursor) {
        Race race = new Race();
        race.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        race.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RACE_NAME)));
        race.setScheduledStartTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULED_START))));

        int actualStartIndex = cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_START);
        if (!cursor.isNull(actualStartIndex)) {
            race.setActualStartTime(new Date(cursor.getLong(actualStartIndex)));
        }

        race.setStatus(Race.RaceStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))));
        return race;
    }

    private Crew cursorToCrew(Cursor cursor) {
        Crew crew = new Crew();
        crew.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        crew.setBibNumber(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BIB)));
        crew.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        crew.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
        crew.setRaceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RACE_ID)));
        return crew;
    }
}
