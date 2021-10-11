package com.tiquionophist.io

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Wraps the top-level save file XML contents. This contains some metadata on the save file (ignored here) and the main
 * [data] blob, which is stored as gzipped and base64-encoded XML.
 */
data class SaveFile(
    @JsonProperty("Data")
    val data: String,
)
