package com.superplanner.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.superplanner.app.ui.planejamento.PlanningScreen
import com.superplanner.app.ui.planos.AdvancedPlansScreen
import com.superplanner.app.ui.routines.RoutineFormScreen
import com.superplanner.app.ui.routines.RoutinesListScreen
import com.superplanner.app.ui.semana.WeekDayScreen
import com.superplanner.app.ui.semana.WeekScreen
import com.superplanner.app.ui.tasks.TaskFormScreen
import com.superplanner.app.ui.tasks.TasksListScreen
import java.time.LocalDate

@Composable
fun SuperPlannerNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = currentRoute in setOf(SuperPlannerRoutes.AGORA, SuperPlannerRoutes.MEU_DIA, SuperPlannerRoutes.PLANNING, SuperPlannerRoutes.EVENTS, SuperPlannerRoutes.TASKS)
    Scaffold(bottomBar = {
        if (showBar) NavigationBar {
            NavigationBarItem(currentRoute == SuperPlannerRoutes.AGORA, { navController.navigateToTab(SuperPlannerRoutes.AGORA) }, { Icon(Icons.Filled.Home, null) }, label = { Text(stringResource(R.string.nav_agora)) })
            NavigationBarItem(currentRoute == SuperPlannerRoutes.MEU_DIA, { navController.navigateToTab(SuperPlannerRoutes.MEU_DIA) }, { Icon(Icons.Filled.DateRange, null) }, label = { Text(stringResource(R.string.nav_meu_dia)) })
            NavigationBarItem(currentRoute == SuperPlannerRoutes.PLANNING, { navController.navigateToTab(SuperPlannerRoutes.PLANNING) }, { Icon(Icons.Filled.Tune, null) }, label = { Text("Planejar") })
            NavigationBarItem(currentRoute == SuperPlannerRoutes.EVENTS, { navController.navigateToTab(SuperPlannerRoutes.EVENTS) }, { Icon(Icons.Filled.List, null) }, label = { Text(stringResource(R.string.nav_eventos)) })
            NavigationBarItem(currentRoute == SuperPlannerRoutes.TASKS, { navController.navigateToTab(SuperPlannerRoutes.TASKS) }, { Icon(Icons.Filled.Check, null) }, label = { Text(stringResource(R.string.nav_tarefas)) })
        }
    }) { padding ->
        NavHost(navController, SuperPlannerRoutes.AGORA, Modifier.padding(padding)) {
            composable(SuperPlannerRoutes.AGORA) { HomeScreen() }
            composable(SuperPlannerRoutes.MEU_DIA) { MeuDiaScreen({ navController.navigate(SuperPlannerRoutes.eventEditor()) }, { navController.navigate(SuperPlannerRoutes.eventEditor(it)) }, { navController.navigate(SuperPlannerRoutes.taskEditor(it)) }, { navController.navigate(SuperPlannerRoutes.habitEditor(it)) }, { navController.navigate(SuperPlannerRoutes.AVAILABILITY) }, { navController.navigate(SuperPlannerRoutes.WEEK) }) }
            composable(SuperPlannerRoutes.WEEK) { WeekScreen { date -> navController.navigate(SuperPlannerRoutes.weekDay(date)) } }
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
            composable(SuperPlannerRoutes.EVENT_EDITOR, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { EventFormScreen { navController.popBackStack() } }
            composable(SuperPlannerRoutes.TASK_EDITOR, arguments = listOf(navArgument("taskId") { type = NavType.StringType })) { TaskFormScreen { navController.popBackStack() } }
            composable(SuperPlannerRoutes.HABIT_EDITOR, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) { HabitFormScreen { navController.popBackStack() } }
            composable(SuperPlannerRoutes.ROUTINE_EDITOR, arguments = listOf(navArgument("routineId") { type = NavType.StringType })) { RoutineFormScreen { navController.popBackStack() } }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) { navigate(route) { popUpTo(graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
