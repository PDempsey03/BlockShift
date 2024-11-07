package com.blockshift.model.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile HighScoreDao _highScoreDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `user` (`username` TEXT NOT NULL, `displayName` TEXT NOT NULL, PRIMARY KEY(`username`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `highScore` (`username` TEXT NOT NULL, `levelID` INTEGER NOT NULL, `distance` INTEGER NOT NULL, `time` INTEGER NOT NULL, `moves` INTEGER NOT NULL, PRIMARY KEY(`username`, `levelID`), FOREIGN KEY(`username`) REFERENCES `user`(`username`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '440b3344fdf6c946a1b4c4b4c2d9d8ab')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `user`");
        db.execSQL("DROP TABLE IF EXISTS `highScore`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUser = new HashMap<String, TableInfo.Column>(2);
        _columnsUser.put("username", new TableInfo.Column("username", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUser.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUser = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUser = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUser = new TableInfo("user", _columnsUser, _foreignKeysUser, _indicesUser);
        final TableInfo _existingUser = TableInfo.read(db, "user");
        if (!_infoUser.equals(_existingUser)) {
          return new RoomOpenHelper.ValidationResult(false, "user(com.blockshift.model.db.User).\n"
                  + " Expected:\n" + _infoUser + "\n"
                  + " Found:\n" + _existingUser);
        }
        final HashMap<String, TableInfo.Column> _columnsHighScore = new HashMap<String, TableInfo.Column>(5);
        _columnsHighScore.put("username", new TableInfo.Column("username", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScore.put("levelID", new TableInfo.Column("levelID", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScore.put("distance", new TableInfo.Column("distance", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScore.put("time", new TableInfo.Column("time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScore.put("moves", new TableInfo.Column("moves", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHighScore = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHighScore.add(new TableInfo.ForeignKey("user", "CASCADE", "NO ACTION", Arrays.asList("username"), Arrays.asList("username")));
        final HashSet<TableInfo.Index> _indicesHighScore = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHighScore = new TableInfo("highScore", _columnsHighScore, _foreignKeysHighScore, _indicesHighScore);
        final TableInfo _existingHighScore = TableInfo.read(db, "highScore");
        if (!_infoHighScore.equals(_existingHighScore)) {
          return new RoomOpenHelper.ValidationResult(false, "highScore(com.blockshift.model.db.HighScore).\n"
                  + " Expected:\n" + _infoHighScore + "\n"
                  + " Found:\n" + _existingHighScore);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "440b3344fdf6c946a1b4c4b4c2d9d8ab", "bba1b46dff54d393d4f4630bdc492e75");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "user","highScore");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `user`");
      _db.execSQL("DELETE FROM `highScore`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HighScoreDao.class, HighScoreDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public HighScoreDao highScoreDao() {
    if (_highScoreDao != null) {
      return _highScoreDao;
    } else {
      synchronized(this) {
        if(_highScoreDao == null) {
          _highScoreDao = new HighScoreDao_Impl(this);
        }
        return _highScoreDao;
      }
    }
  }
}
