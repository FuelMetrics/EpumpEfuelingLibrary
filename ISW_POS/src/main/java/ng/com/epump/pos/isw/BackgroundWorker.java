package ng.com.epump.pos.isw;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;

public class BackgroundWorker extends Worker {
    public static IswTxnHandler iswTxnHandler;
    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            return doTheWork();
        }
        catch (Exception exception){
            return Result.failure();
        }
    }

    private Result doTheWork(){
        iswTxnHandler.downloadTmKimParam(new Continuation<AllTerminalInfo>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return null;
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                Log.i("TAG", "resumeWith: ");
            }
        });
        return Result.success();
    }
}
