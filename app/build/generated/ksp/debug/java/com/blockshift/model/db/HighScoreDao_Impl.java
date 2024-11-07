package com.blockshift.model.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HighScoreDao_Impl implements HighScoreDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HighScore> __insertionAdapterOfHighScore;

  private final EntityDeletionOrUpdateAdapter<HighScore> __deletionAdapterOfHighScore;

  private final EntityDeletionOrUpdateAdapter<HighScore> __updateAdapterOfHighScore;

  public HighScoreDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHighScore = new EntityInsertionAdapter<HighScore>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `highScore` (`username`,`levelID`,`distance`,`time`,`moves`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HighScore entity) {
        statement.bindString(1, entity.getUsername());
        statement.bindLong(2, entity.getLevelID());
        statement.bindLong(3, entity.getDistance());
        statement.bindLong(4, entity.getTime());
        statement.bindLong(5, entity.getMoves());
      }
    };
    this.__deletionAdapterOfHighScore = new EntityDeletionOrUpdateAdapter<HighScore>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `highScore` WHERE `username` = ? AND `levelID` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HighScore entity) {
        statement.bindString(1, entity.getUsername());
        statement.bindLong(2, entity.getLevelID());
      }
    };
    this.__updateAdapterOfHighScore = new EntityDeletionOrUpdateAdapter<HighScore>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `highScore` SET `username` = ?,`levelID` = ?,`distance` = ?,`time` = ?,`moves` = ? WHERE `username` = ? AND `levelID` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HighScore entity) {
        statement.bindString(1, entity.getUsername());
        statement.bindLong(2, entity.getLevelID());
        statement.bindLong(3, entity.getDistance());
        statement.bindLong(4, entity.getTime());
        statement.bindLong(5, entity.getMoves());
        statement.bindString(6, entity.getUsername());
        statement.bindLong(7, entity.getLevelID());
      }
    };
  }

  @Override
  public Object insert(final HighScore highScore, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHighScore.insert(highScore);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final HighScore highScore, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfHighScore.handle(highScore);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final HighScore highScore, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfHighScore.handle(highScore);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<HighScore>> getHighScores(final String username) {
    final String _sql = "SELECT * FROM highScore WHERE username = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, username);
    return __db.getInvalidationTracker().createLiveData(new String[] {"highScore"}, false, new Callable<List<HighScore>>() {
      @Override
      @Nullable
      public List<HighScore> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfLevelID = CursorUtil.getColumnIndexOrThrow(_cursor, "levelID");
          final int _cursorIndexOfDistance = CursorUtil.getColumnIndexOrThrow(_cursor, "distance");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfMoves = CursorUtil.getColumnIndexOrThrow(_cursor, "moves");
          final List<HighScore> _result = new ArrayList<HighScore>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HighScore _item;
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final int _tmpLevelID;
            _tmpLevelID = _cursor.getInt(_cursorIndexOfLevelID);
            final int _tmpDistance;
            _tmpDistance = _cursor.getInt(_cursorIndexOfDistance);
            final int _tmpTime;
            _tmpTime = _cursor.getInt(_cursorIndexOfTime);
            final int _tmpMoves;
            _tmpMoves = _cursor.getInt(_cursorIndexOfMoves);
            _item = new HighScore(_tmpUsername,_tmpLevelID,_tmpDistance,_tmpTime,_tmpMoves);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
