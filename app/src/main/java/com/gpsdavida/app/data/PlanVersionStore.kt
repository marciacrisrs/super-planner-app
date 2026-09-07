package com.gpsdavida.app.data

import android.content.Context
import com.gpsdavida.app.domain.model.Plan
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject

class PlanVersionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy { context.getSharedPreferences("plan_versions", Context.MODE_PRIVATE) }

    fun saveSnapshot(plan: Plan) {
        val key = plan.id
        val values = prefs.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        values += JSONObject().apply {
            put("version", plan.version)
            put("name", plan.name)
            put("objective", plan.objective)
            put("type", plan.type.name)
            put("status", plan.status.name)
            put("origin", plan.origin ?: JSONObject.NULL)
            put("validFrom", plan.validFrom?.toString() ?: JSONObject.NULL)
            put("validUntil", plan.validUntil?.toString() ?: JSONObject.NULL)
            put("sourceDocument", plan.sourceDocument ?: JSONObject.NULL)
        }.toString()
        prefs.edit().putStringSet(key, values).apply()
    }

    fun count(planId: String): Int = prefs.getStringSet(planId, emptySet())?.size ?: 0
}
