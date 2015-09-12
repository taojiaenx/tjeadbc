package org.adbcj.h2.decoding;

import io.netty.channel.Channel;
import org.adbcj.Field;
import org.adbcj.ResultHandler;
import org.adbcj.h2.H2Connection;
import org.adbcj.h2.H2DbException;
import org.adbcj.support.DefaultDbFuture;
import org.adbcj.support.DefaultField;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.adbcj.h2.decoding.IoUtils.*;

/**
 * @author roman.stoffel@gamlor.info
 */
public class ColumnDecoder<T>  implements DecoderState {
    private final ResultHandler<T> eventHandler;
    private final T accumulator;
    private final DefaultDbFuture<T> resultFuture;
    private final H2Connection connection;
    private final int rows;
    private final int columnsAvailable;
    private final List<Field> columnsBuildUp;

    public ColumnDecoder(ResultHandler<T> eventHandler,
                          T accumulator,
                          DefaultDbFuture<T> resultFuture,
                          H2Connection connection,
                          int rows,
                          int columnsAvailable){
        this(eventHandler, accumulator, resultFuture,connection, rows, columnsAvailable, new ArrayList<Field>());
    }

    private ColumnDecoder(ResultHandler<T> eventHandler,
                         T accumulator,
                         DefaultDbFuture<T> resultFuture,
                         H2Connection connection,
                         int rows,
                         int columnsAvailable,
                         List<Field> columnsBuildUp) {
        this.eventHandler = eventHandler;
        this.accumulator = accumulator;
        this.resultFuture = resultFuture;
        this.connection = connection;
        this.rows = rows;
        this.columnsAvailable = columnsAvailable;
        this.columnsBuildUp = columnsBuildUp;
    }

    @Override
    public ResultAndState decode(DataInputStream stream, Channel channel) throws IOException {
        ResultOrWait<String> alias = tryReadNextString(stream, ResultOrWait.Start);
        ResultOrWait<String> schemaName = tryReadNextString(stream, alias);
        ResultOrWait<String> tableName = tryReadNextString(stream, schemaName);
        ResultOrWait<String> columnName = tryReadNextString(stream, tableName);
        ResultOrWait<Integer> columnType = tryReadNextInt(stream, columnName);
        ResultOrWait<Long> precision = tryReadNextLong(stream, columnType);
        ResultOrWait<Integer> scale = tryReadNextInt(stream, precision);
        ResultOrWait<Integer> displaySize = tryReadNextInt(stream, scale);
        ResultOrWait<Boolean> autoIncrement = tryReadNextBoolean(stream, displaySize);
        ResultOrWait<Integer> nullable = tryReadNextInt(stream, autoIncrement);

        if(nullable.couldReadResult){
            final DefaultField field = new DefaultField(columnsBuildUp.size(),
                    "",
                    schemaName.result,
                    tableName.result,
                    columnName.result,
                    H2Types.typeCodeToType(columnType.result).getType(),
                    alias.result,
                    columnName.result,
                    precision.result.intValue(),
                    scale.result,
                    autoIncrement.result,
                    false,
                    false,
                    1 == nullable.result,
                    true, true, "");

            eventHandler.field(field,accumulator );
            columnsBuildUp.add(field);
            if((columnsBuildUp.size())==columnsAvailable){
                eventHandler.endFields(accumulator);
                return goToRowParsing();
            } else{
                return ResultAndState.newState(
                        new ColumnDecoder<T>(eventHandler,
                                accumulator,
                                resultFuture,
                                connection,
                                rows,
                                columnsAvailable,
                                columnsBuildUp));
            }
        } else{
            return ResultAndState.waitForMoreInput(this);
        }
    }

    @Override
    public ResultAndState handleException(H2DbException exception) {
        resultFuture.trySetException(exception);
        return ResultAndState.newState(new AnswerNextRequest(connection));
    }

    private ResultAndState goToRowParsing() {
        if(rows==0){
            eventHandler.startResults(accumulator);
            eventHandler.endResults(accumulator);
            resultFuture.trySetResult(accumulator);
            return ResultAndState.newState(new AnswerNextRequest(connection));
        } else{
            return ResultAndState.newState(
                    new RowDecoder<T>(eventHandler,
                            accumulator,
                            resultFuture,
                            connection,
                            columnsBuildUp,
                            rows)
            );

        }
    }
}
