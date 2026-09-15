package com.superplanner.app.domain.ai

import com.superplanner.app.BuildConfig
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class OrganizeWeekGatewayClient @Inject constructor() {
    suspend fun organize(request: OrganizeWeekRequest): OrganizeWeekResponse = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.AI_GATEWAY_URL.trim().trimEnd('/')
        require(endpoint.isNotEmpty()) { "AI gateway is not configured" }
        require(request.weekStart.isNotBlank()) { "weekStart must not be blank" }
        require(request.timezone.isNotBlank()) { "timezone must not be blank" }
        val requestId = UUID.randomUUID().toString()
        val response = postAiGateway(
            path = "/v1/ai/organize-week", endpoint = endpoint, requestId = requestId,
            timeoutMs = 30_000, body = OrganizeWeekGatewayProtocol.buildRequestBody(request).toString(),
        )
        if (response.status !in 200..299) error("AI gateway returned HTTP ${response.status} [requestId=$requestId]: ${response.body}")
        require(response.body.isNotBlank()) { "AI gateway returned an empty organize-week response [requestId=$requestId]" }
        OrganizeWeekGatewayProtocol.parseResponse(JSONObject(response.body))
    }
}

internal object OrganizeWeekGatewayProtocol {
    fun buildRequestBody(request: OrganizeWeekRequest): JSONObject = JSONObject().apply {
        put("weekStart", request.weekStart); put("timezone", request.timezone)
        put("existingPlan", request.existingPlan.toJsonArray(::planItemJson)); put("fixedCommitments", request.fixedCommitments.toJsonArray(::planItemJson)); put("desires", request.desires.toJsonArray(::planItemJson))
        put("logistics", request.logistics.toJsonArray(::logisticJson)); put("preferences", request.preferences.toJsonArray(::preferenceJson)); put("aiTips", JSONArray(request.aiTips)); request.capacity?.let { put("capacity", capacityJson(it)) }
    }
    fun parseResponse(json: JSONObject): OrganizeWeekResponse {
        val summary = json.getJSONObject("summary")
        val proposedItems = json.getJSONArray("proposedItems").toObjectList { item -> OrganizeWeekProposedItem(item.getString("id"), item.getString("title"), item.getString("date"), item.getString("startTime"), item.getString("endTime"), item.getString("source"), item.optBoolean("fixed", false), item.optString("reason").takeIf(String::isNotBlank)) }
        val conflicts = json.getJSONArray("conflicts").toObjectList { item -> OrganizeWeekConflict(item.getString("id"), item.getString("title"), item.getJSONArray("affectedItemIds").toStringList(), item.getString("reason"), item.getString("severity")) }
        val opportunities = json.getJSONArray("opportunities").toObjectList { item -> OrganizeWeekOpportunity(item.getString("id"), item.getString("title"), item.getString("reason")) }
        val explanations = json.getJSONArray("explanations").toObjectList { item -> OrganizeWeekExplanation(item.optString("itemId").takeIf(String::isNotBlank), item.getString("message")) }
        return OrganizeWeekResponse(
            summary = OrganizeWeekSummary(summary.getInt("fixedCommitmentsConsidered"), summary.getInt("desiresConsidered"), summary.getInt("commuteMinutesConsidered"), summary.getInt("preparationMinutesConsidered"), summary.getInt("aiSuggestionsConsidered"), summary.getInt("conflictsFound"), summary.getInt("opportunitiesFound")),
            proposedItems = proposedItems, conflicts = conflicts, opportunities = opportunities, explanations = explanations, model = json.optString("model"),
        )
    }
    private fun planItemJson(item: OrganizeWeekPlanItem) = JSONObject().apply { put("id", item.id); put("title", item.title); put("date", item.date); putOptional("startTime", item.startTime); putOptional("endTime", item.endTime); putOptional("durationMinutes", item.durationMinutes); putOptional("priority", item.priority); putOptional("kind", item.kind); put("required", item.required) }
    private fun capacityJson(capacity: OrganizeWeekCapacity) = JSONObject().apply { put("load", capacity.load); put("totalCapacityMinutes", capacity.totalCapacityMinutes); put("totalDesiredMinutes", capacity.totalDesiredMinutes); put("totalRemainingMinutes", capacity.totalRemainingMinutes); put("days", capacity.days.toJsonArray { day -> JSONObject().apply { put("date", day.date); put("load", day.load); put("schedulableMinutes", day.schedulableMinutes); put("desiredMinutes", day.desiredMinutes); put("remainingMinutes", day.remainingMinutes) } }); put("reasons", JSONArray(capacity.reasons)) }
    private fun logisticJson(item: OrganizeWeekLogisticConstraint) = JSONObject().apply { put("type", item.type); put("minutes", item.minutes); putOptional("beforeItemId", item.beforeItemId); putOptional("afterItemId", item.afterItemId); putOptional("origin", item.origin); putOptional("destination", item.destination); put("required", item.required) }
    private fun preferenceJson(item: OrganizeWeekPreference) = JSONObject().apply { put("key", item.key); put("value", item.value) }
    private fun JSONObject.putOptional(key: String, value: Any?) { if (value != null) put(key, value) }
    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject) = JSONArray().also { array -> forEach { array.put(mapper(it)) } }
    private fun <T> JSONArray.toObjectList(mapper: (JSONObject) -> T): List<T> = buildList { for (index in 0 until length()) add(mapper(getJSONObject(index))) }
    private fun JSONArray.toStringList(): List<String> = buildList { for (index in 0 until length()) add(getString(index)) }
}
