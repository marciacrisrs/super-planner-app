package com.superplanner.app.data

import android.content.Context
import com.superplanner.app.domain.port.WeeklyPlanOverride
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyPlanOverrideStore @Inject constructor(
    @ApplicationContext context: Context,
) : WeeklyPlanOverrideRepository {
    private val prefs = context.getSharedPreferences("weekly_plan_overrides", Context.MODE_PRIVATE)
    private val overrides = MutableStateFlow(readPersisted())

    override suspend fun save(items: List<WeeklyPlanOverride>) {
        val json = JSONArray().apply {
            items.forEach { item ->
                put(JSONObject().apply {
                    put("activityId", item.activityId)
                    put("date", item.date)
                    put("start", item.start.toString())
                    put("end", item.end.toString())
                })
            }
        }
        prefs.edit().putString("overrides", json.toString()).apply()
        overrides.value = items
    }

    override fun observe(): Flow<List<WeeklyPlanOverride>> = overrides.asStateFlow()

    private fun readPersisted(): List<WeeklyPlanOverride> {
        val raw = prefs.getString("overrides", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            buildList {
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
        }.getOrDefault(emptyList())
    }
}
