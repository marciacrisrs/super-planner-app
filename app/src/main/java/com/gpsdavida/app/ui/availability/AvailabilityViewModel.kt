package com.superplanner.app.ui.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.port.AvailabilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val repository: AvailabilityRepository,
) : ViewModel() {
    val items: StateFlow<List<Availability>> = repository.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun save(day: DayOfWeek, startText: String, endText: String, kind: AvailabilityKind): String? {
        val start = parseTime(startText) ?: return "invalid_start"
        val end = parseTime(endText) ?: return "invalid_end"
        if (!start.isBefore(end)) return "invalid_range"

        viewModelScope.launch {
            repository.save(
                Availability(
                    id = AvailabilityId(UUID.randomUUID().toString()),
                    dayOfWeek = day,
                    window = LocalTimeWindow(start, end),
                    kind = kind,
                ),
            )
        }
        return null
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(AvailabilityId(id)) }
    }

    private fun parseTime(value: String): LocalTime? = runCatching {
        LocalTime.parse(value.trim().let { if (it.length == 5) it else "" })
    }.getOrNull()
}
