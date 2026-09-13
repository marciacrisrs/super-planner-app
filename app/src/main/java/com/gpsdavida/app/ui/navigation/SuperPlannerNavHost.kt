@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.superplanner.app.ui.navigation

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.superplanner.app.R
import com.superplanner.app.ui.availability.AvailabilityScreen
import com.superplanner.app.ui.contexto.LifeAreaScreen
import com.superplanner.app.ui.dia.DailyCheckpointScreen
import com.superplanner.app.ui.events.EventFormScreen
import com.superplanner.app.ui.events.EventsListScreen
import com.superplanner.app.ui.financas.FinanceScreen
import com.superplanner.app.ui.habits.HabitFormScreen
import com.superplanner.app.ui.habits.HabitsListScreen
import com.superplanner.app.ui.home.HomeScreen
import com.superplanner.app.ui.horizontes.HorizonsScreen
import com.superplanner.app.ui.horizontes.WeeklyReviewScreen
import com.superplanner.app.ui.meudia.MeuDiaScreen
import com.superplanner.app.ui.planos.AdvancedPlansScreen
import com.superplanner.app.ui.planos.PlanningScreen
import com.superplanner.app.ui.routines.RoutineFormScreen
import com.superplanner.app.ui.routines.RoutinesListScreen
import com.superplanner.app.ui.semana.WeekDayScreen
import com.superplanner.app.ui.semana.WeekScreen
import com.superplanner.app.ui.tasks.TaskFormScreen
import com.superplanner.app.ui.tasks.TasksListScreen
import com.superplanner.app.ui.theme.SuperPlannerColors
import java.time.LocalDate

@Composable
fun SuperPlannerNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = currentRoute in MAIN_TABS

    Scaffold(
        bottomBar = {
            if (showBar) NavigationBar(
                containerColor = SuperPlannerColors.Surface.copy(alpha = 0.96f),
            ) {
                NavigationBarItem(
                    selected = currentRoute == SuperPlannerRoutes.AGORA,
                    onClick = { navController.navigateToTab(SuperPlannerRoutes.AGORA) },
                    icon = { Icon(Icons.Outlined.Home, null) },
                    label = { Text(stringResource(R.string.nav_agora)) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = currentRoute == SuperPlannerRoutes.MEU_DIA,
                    onClick = { navController.navigateToTab(SuperPlannerRoutes.MEU_DIA) },
                    icon = { Icon(Icons.Outlined.CalendarToday, null) },
                    label = { Text(stringResource(R.string.nav_meu_dia)) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = currentRoute == SuperPlannerRoutes.PLANNING,
                    onClick = { navController.navigateToTab(SuperPlannerRoutes.PLANNING) },
                    icon = { Icon(Icons.Outlined.FavoriteBorder, null) },
                    label = { Text("Planejar") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = currentRoute == SuperPlannerRoutes.EVENTS,
                    onClick = { navController.navigateToTab(SuperPlannerRoutes.EVENTS) },
                    icon = { Icon(Icons.Outlined.Menu, null) },
                    label = { Text(stringResource(R.string.nav_eventos)) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = currentRoute == SuperPlannerRoutes.TASKS,
                    onClick = { navController.navigateToTab(SuperPlannerRoutes.TASKS) },
                    icon = { Icon(Icons.Outlined.CheckCircleOutline, null) },
                    label = { Text(stringResource(R.string.nav_tarefas)) },
                    colors = navColors(),
                )
            }
        },
    ) { padding ->
        NavHost(
            navController,
            SuperPlannerRoutes.AGORA,
            Modifier
                .padding(padding)
                .swipeBetweenMainTabs(navController, currentRoute),
        ) {
            composable(SuperPlannerRoutes.AGORA) { HomeScreen() }
            composable(SuperPlannerRoutes.MEU_DIA) { MeuDiaScreen({ navController.navigate(SuperPlannerRoutes.eventEditor()) }, { navController.navigate(SuperPlannerRoutes.eventEditor(it)) }, { navController.navigate(SuperPlannerRoutes.taskEditor(it)) }, { navController.navigate(SuperPlannerRoutes.habitEditor(it)) }, { navController.navigate(SuperPlannerRoutes.AVAILABILITY) }, { navController.navigate(SuperPlannerRoutes.WEEK) }) }
            composable(SuperPlannerRoutes.WEEK) { WeekScreen(onOpenDay = { date -> navController.navigate(SuperPlannerRoutes.weekDay(date)) }) }
            composable(SuperPlannerRoutes.WEEK_DAY, arguments = listOf(navArgument("date") { type = NavType.StringType })) { entry -> WeekDayScreen(LocalDate.parse(entry.arguments?.getString("date"))) }
            composable(SuperPlannerRoutes.PLANNING) { PlanningScreen(onOpenHorizons = { navController.navigate(SuperPlannerRoutes.HORIZONS) }, onOpenReview = { navController.navigate(SuperPlannerRoutes.REVIEW) }, onOpenFinance = { navController.navigate(SuperPlannerRoutes.FINANCE) }, onOpenLifeAreas = { navController.navigate(SuperPlannerRoutes.LIFE_AREAS) }, onOpenDayCheckpoint = { navController.navigate(SuperPlannerRoutes.DAY_CHECKPOINT) }, onOpenPlans = { navController.navigate(SuperPlannerRoutes.PLANS) }) }
            composable(SuperPlannerRoutes.HORIZONS) { HorizonsScreen() }
            composable(SuperPlannerRoutes.REVIEW) { WeeklyReviewScreen() }
            composable(SuperPlannerRoutes.FINANCE) { FinanceScreen() }
            composable(SuperPlannerRoutes.LIFE_AREAS) { LifeAreaScreen() }
            composable(SuperPlannerRoutes.DAY_CHECKPOINT) { DailyCheckpointScreen() }
            composable(SuperPlannerRoutes.PLANS) { AdvancedPlansScreen() }
            composable(SuperPlannerRoutes.AVAILABILITY) { AvailabilityScreen() }
            composable(SuperPlannerRoutes.EVENTS) { EventsListScreen({ navController.navigate(SuperPlannerRoutes.eventEditor()) }, { navController.navigate(SuperPlannerRoutes.eventEditor(it)) }) }
            composable(SuperPlannerRoutes.TASKS) { TasksListScreen({ navController.navigate(SuperPlannerRoutes.taskEditor()) }, { navController.navigate(SuperPlannerRoutes.taskEditor(it)) }) }
            composable(SuperPlannerRoutes.HABITS) { HabitsListScreen({ navController.navigate(SuperPlannerRoutes.habitEditor()) }, { navController.navigate(SuperPlannerRoutes.habitEditor(it)) }) }
            composable(SuperPlannerRoutes.ROUTINES) { RoutinesListScreen({ navController.navigate(SuperPlannerRoutes.routineEditor()) }, { navController.navigate(SuperPlannerRoutes.routineEditor(it)) }) }
            composable(SuperPlannerRoutes.EVENT_EDITOR, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { EventFormScreen(onDone = { navController.popBackStack() }) }
            composable(SuperPlannerRoutes.TASK_EDITOR, arguments = listOf(navArgument("taskId") { type = NavType.StringType })) { TaskFormScreen(onDone = { navController.popBackStack() }) }
            composable(SuperPlannerRoutes.HABIT_EDITOR, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) { HabitFormScreen(onDone = { navController.popBackStack() }) }
            composable(SuperPlannerRoutes.ROUTINE_EDITOR, arguments = listOf(navArgument("routineId") { type = NavType.StringType })) { RoutineFormScreen(onDone = { navController.popBackStack() }) }
        }
    }
}

private val MAIN_TABS = listOf(
    SuperPlannerRoutes.AGORA,
    SuperPlannerRoutes.MEU_DIA,
    SuperPlannerRoutes.PLANNING,
    SuperPlannerRoutes.EVENTS,
    SuperPlannerRoutes.TASKS,
)

private fun Modifier.swipeBetweenMainTabs(
    navController: NavHostController,
    currentRoute: String?,
): Modifier = if (currentRoute in MAIN_TABS) {
    pointerInput(currentRoute) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
            onDragEnd = {
                if (kotlin.math.abs(totalDrag) >= 96f) {
                    val index = MAIN_TABS.indexOf(currentRoute)
                    val targetIndex = if (totalDrag < 0) index + 1 else index - 1
                    MAIN_TABS.getOrNull(targetIndex)?.let { navController.navigateToTab(it) }
                }
                totalDrag = 0f
            },
            onDragCancel = { totalDrag = 0f },
        )
    }
} else this

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SuperPlannerColors.TerracottaDark,
    selectedTextColor = SuperPlannerColors.TerracottaDark,
    indicatorColor = SuperPlannerColors.TerracottaSoft,
    unselectedIconColor = SuperPlannerColors.InkSoft,
    unselectedTextColor = SuperPlannerColors.InkSoft,
)

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
