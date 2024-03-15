package dbexecutors.ch;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseNodes;
import com.clickhouse.client.ClickHouseRequest;
import com.clickhouse.data.ClickHouseFormat;
import dbexecutors.patterns.Channels;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class ChannelsExecutor {
    static ClickHouseNodes servers = ClickHouseNodes.of(
            "http://localhost:8123?compress=0");

    public static class Messages {
        public static void postNewTextMessage (
                @NotNull ClickHouseClient connection,
                @NotNull Long channel,
                @NotNull Long id,
                @NotNull Timestamp timestamp,
                @NotNull Long author,
                @NotNull String data,
                @NotNull Long[] files,
                @NotNull Long[][] media,
                @NotNull Channels.Messages.ForwardBy forward) throws ClickHouseException {
            connection.read(servers)
                    .format(ClickHouseFormat.RowBinary)
                    .query("""
                            INSERT INTO
                                channels.messages (`channel`, `id`, `version`, `timestamp`, `author`, `type`, `data`, `files`, `media`, `is forward`, `forward type`, `forward by`, `is deleted`, `deleted reason`)
                            VALUES
                                (:channel, :message, 1, :timestamp, :author, 'text', :message_text, :message_attachments_files, :message_attachments_media, FALSE, 'no', :forward_by, FALSE, '')
                            """)
                    .params(channel, id, timestamp.toString( ), author, data, files, media, new Long[]{ })
                    .executeAndWait();
        }
    }
}
