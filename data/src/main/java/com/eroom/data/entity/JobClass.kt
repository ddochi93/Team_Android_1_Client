package com.eroom.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class JobClass(
    @JsonProperty("id") var id: Long,
    @JsonProperty("name") var name: String,
    @JsonProperty("jobGroupId") var jobGroupId: Long
)