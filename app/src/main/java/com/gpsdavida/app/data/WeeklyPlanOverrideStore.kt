package com.superplanner.app.data

import android.content.Context
import com.superplanner.app.domain.port.WeeklyPlanOverride
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class WeeklyPlanOverrideStore @Inject constructor(
    @ApplicationContext context: Context,
) : WeeklyPlanOverrideRepository {
    private val prefs = context.getSharedPreferences("weekly_plan_overrides", Context.MODE_PRIVATE)

    override suspend fun save(overrides: List<WeeklyPlanOverride>) {
        val json = JSONArray().apply {
            overrides.forEach { item ->
                put(JSONObject().apply {
                    put("activityId", item.activityId)
                    put("date", item.date)
                    put("start", item.start.toString())
                    put("end", item.end.toString())
                })
            }
        }
        prefs.edit().putString("overrides", json.toString()).apply()
    }

    override suspend fun observe(): List<WeeklyPlanOverride> {
        val raw = prefs.getString("overrides", null) ?: return emptyList()
        val json = JSONArray(raw)
        return buildList {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                add(
                    WeeklyPlanOverride(
                        activityId = item.getString("activityId"),
                        date = item.getString("date"),
                        start = Instant.parse(item.getString("start")),
                        end = Instant.parse(item.getString("end")),
                    ),
                )
            }
        }
    }
}
