package com.gpsdavida.app.ui.planos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.data.PlanVersionStore
import com.gpsdavida.app.data.RoomPlanRepository
import com.gpsdavida.app.domain.model.Plan
import com.gpsdavida.app.domain.model.PlanItem
import com.gpsdavida.app.domain.model.PlanStatus
import com.gpsdavida.app.domain.usecase.GeneratePlanProgramming
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AdvancedPlansViewModel @Inject constructor(
    private val plans: RoomPlanRepository,
    private val versions: PlanVersionStore,
    private val generateProgramming: GeneratePlanProgramming,
) : ViewModel() {
    private val selectedPlanId = MutableStateFlow<String?>(null)
    val state: StateFlow<AdvancedPlanState> = combine(plans.observeAll(), selectedPlanId) { list, selected ->
        val activeSelected = selected ?: list.firstOrNull()?.id
        AdvancedPlanState(list, activeSelected, versions.count(activeSelected.orEmpty()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdvancedPlanState())

    fun select(id: String) { selectedPlanId.value = id }

    fun attachDocument(plan: Plan, uri: Uri) {
        viewModelScope.launch { plans.save(plan.copy(sourceDocument = uri.toString())) }
    }

    fun addItem(planId: String, title: String, minutes: Int, time: LocalTime?, days: Set<DayOfWeek>) {
        if (title.isBlank() || minutes <= 0) return
        viewModelScope.launch {
            plans.saveItem(PlanItem(UUID.randomUUID().toString(), planId, title.trim(), minutes, days, time))
        }
    }

    fun generate(plan: Plan) {
        viewModelScope.launch { generateProgramming(plan) }
    }

    fun newVersion(plan: Plan) {
        viewModelScope.launch {
            versions.saveSnapshot(plan)
            plans.save(plan.copy(version = plan.version + 1, status = PlanStatus.ACTIVE))
        }
    }

    fun setStatus(plan: Plan, status: PlanStatus) {
        viewModelScope.launch { plans.save(plan.copy(status = status)) }
    }
}

data class AdvancedPlanState(
    val plans: List<Plan> = emptyList(),
    val selectedId: String? = null,
    val versionCount: Int = 0,
)
