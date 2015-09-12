package org.adbcj.h2.decoding;

import io.netty.channel.Channel;
import org.adbcj.DbFuture;
import org.adbcj.DbListener;
import org.adbcj.FutureState;
import org.adbcj.Result;
import org.adbcj.h2.H2Connection;
import org.adbcj.h2.H2DbException;
import org.adbcj.h2.H2Result;
import org.adbcj.h2.protocol.StatusCodes;
import org.adbcj.support.DefaultDbFuture;
import org.adbcj.support.DefaultResultEventsHandler;
import org.adbcj.support.DefaultResultSet;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author roman.stoffel@gamlor.info
 */
public class UpdateResult  extends StatusReadingDecoder  {
    private final DefaultDbFuture<Result> resultHandler;

    public UpdateResult(DefaultDbFuture<Result> resultHandler,H2Connection connection) {
        super(connection);
        this.resultHandler = resultHandler;
    }

    @Override
    protected ResultAndState processFurther(DataInputStream stream, Channel channel, int status) throws IOException {
        StatusCodes.STATUS_OK.expectStatusOrThrow(status);

        final ResultOrWait<Integer> affected = IoUtils.tryReadNextInt(stream, ResultOrWait.Start);
        final ResultOrWait<Boolean> autoCommit = IoUtils.tryReadNextBoolean(stream, affected);
        if(autoCommit.couldReadResult){
            DefaultDbFuture<DefaultResultSet> futureForAutoKeys = new DefaultDbFuture<DefaultResultSet>(connection.stackTrachingOptions());
            DefaultResultSet result = new DefaultResultSet();
            DefaultResultEventsHandler handler = new DefaultResultEventsHandler();

            futureForAutoKeys.addListener(new DbListener<DefaultResultSet>() {
                @Override
                public void onCompletion(DbFuture<DefaultResultSet> future) {
                    final FutureState state = future.getState();
                    switch (state){
                        case SUCCESS:
                            resultHandler.trySetResult(new H2Result(future.getResult(),affected.result.longValue(),new ArrayList<String>()));
                            break;
                        case FAILURE:
                            resultHandler.trySetException(future.getException());
                            break;
                        case CANCELLED:
                            resultHandler.cancel(false);
                            break;
                        default:
                            throw new Error("Code which should be unreachable");
                    }

                }
            });

            return ResultAndState.newState(new QueryHeader<DefaultResultSet>(handler,
                    result,
                    futureForAutoKeys,connection));
        } else{
            return ResultAndState.waitForMoreInput(this);
        }
    }

    @Override
    protected void requestFailedContinue(H2DbException exception) {
        resultHandler.trySetException(exception);
    }
}
