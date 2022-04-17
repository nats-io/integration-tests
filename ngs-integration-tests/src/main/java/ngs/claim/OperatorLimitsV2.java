package ngs.claim;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OperatorLimitsV2 {
    private long subs;            // int64 `json:"subs,omitempty"`      // Max number of subscriptions
    private long conn;            // int64 `json:"conn,omitempty"`      // Max number of active connections
    private long leafNodeConn;    // int64 `json:"leaf,omitempty"`      // Max number of active leaf node connections
    private long imports;         // int64 `json:"imports,omitempty"`   // Max number of imports
    private long exports;         // int64 `json:"exports,omitempty"`   // Max number of exports
    private long data;            // int64 `json:"data,omitempty"`      // Max number of bytes
    private long payload;         // int64 `json:"payload,omitempty"`   // Max message payload
    private boolean wildcards;    // bool  `json:"wildcards,omitempty"` // Are wildcards allowed in exports

    @JsonAlias({ "mem_storage" })
    private long memoryStorage;          // int64 `json:"mem_storage,omitempty"`           // Max number of bytes stored in memory across all streams. (0 means disabled)

    @JsonAlias({ "disk_storage" })
    private long diskStorage;            // int64 `json:"disk_storage,omitempty"`          // Max number of bytes stored on disk across all streams. (0 means disabled)

    private long streams;                // int64 `json:"streams,omitempty"`               // Max number of streams

    private long consumer;               // int64 `json:"consumer,omitempty"`              // Max number of consumers

    @JsonAlias({ "max_ack_pending" })
    private long maxAckPending;          // int64 `json:"max_ack_pending,omitempty"`       // Max ack pending of a Stream

    @JsonAlias({ "mem_max_stream_bytes" })
    private long memoryMaxStreamBytes;   // int64 `json:"mem_max_stream_bytes,omitempty"`  // Max bytes a memory backed stream can have. (0 means disabled/unlimited)

    @JsonAlias({ "disk_max_stream_bytes" })
    private long diskMaxStreamBytes;     // int64 `json:"disk_max_stream_bytes,omitempty"` // Max bytes a disk backed stream can have. (0 means disabled/unlimited)

    @JsonAlias({ "max_bytes_required" })
    private boolean maxBytesRequired;    // bool  `json:"max_bytes_required,omitempty"`    // Max bytes required by all Streams

    public long getSubs() {
        return subs;
    }

    public long getConn() {
        return conn;
    }

    public long getLeafNodeConn() {
        return leafNodeConn;
    }

    public long getImports() {
        return imports;
    }

    public long getExports() {
        return exports;
    }

    public long getData() {
        return data;
    }

    public long getPayload() {
        return payload;
    }

    public boolean isWildcards() {
        return wildcards;
    }

    public boolean areWildcardsAllowed() {
        return wildcards;
    }

    public long getMemoryStorage() {
        return memoryStorage;
    }

    public long getDiskStorage() {
        return diskStorage;
    }

    public long getStreams() {
        return streams;
    }

    public long getConsumer() {
        return consumer;
    }

    public long getMaxAckPending() {
        return maxAckPending;
    }

    public long getMemoryMaxStreamBytes() {
        return memoryMaxStreamBytes;
    }

    public long getDiskMaxStreamBytes() {
        return diskMaxStreamBytes;
    }

    public boolean isMaxBytesRequired() {
        return maxBytesRequired;
    }
}
