package com.gpsdavida.app.data

import android.content.Context
import com.gpsdavida.app.domain.model.Plan
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject

class PlanVersionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("plan_versions", Context.MODE_PRIVATE)

    fun saveSnapshot(plan: Plan) {
        val snapshots = prefs.getStringSet(plan.id, emptySet()).orEmpty().toMutableSet()
        snapshots += JSONObject().apply {
            put("version", plan.version)
            put("name", plan.name)
            put("objective", plan.objective)
            put("type", plan.type.name)
            put("origin", plan.origin ?: JSONObject.NULL)
            put("status", plan.status.name)
            put("validFrom", plan.validFrom?.toString() ?: JSONObject.NULL)
            put("validUntil", plan.validUntil?.toString() ?: JSONObject.NULL)
            put("sourceDocument", plan.sourceDocument ?: JSONObject.NULL)
        }.toString()
        prefs.edit().putStringSet(plan.id, snapshots).apply()
    }

    fun count(planId: String): Int = prefs.getStringSet(planId, emptySet()).orEmpty().size
}
