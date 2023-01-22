package com.activityartapp.data.remote.responses

import com.activityartapp.domain.models.Athlete
import com.google.gson.annotations.SerializedName

data class AthleteResponse(
    @SerializedName("id")
    override val id: Long,
) : Athlete