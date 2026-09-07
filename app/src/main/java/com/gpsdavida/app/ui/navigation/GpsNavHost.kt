package com.gpsdavida.app.ui.navigation

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
import com.gpsdavida.app.R
import com.gpsdavida.app.ui.availability.AvailabilityScreen
import com.gpsdavida.app.ui.contexto.LifeAreaScreen
import com.gpsdavida.app.ui.dia.DailyCheckpointScreen
import com.gpsdavida.app.ui.events.EventFormScreen
import com.gpsdavida.app.ui.events.EventsListScreen
import com.gpsdavida.app.ui.financas.FinanceScreen
import com.gpsdavida.app.ui.habits.HabitFormScreen
import com.gpsdavida.app.ui.habits.HabitsListScreen
import com.gpsdavida.app.ui.home.HomeScreen
import com.gpsdavida.app.ui.horizontes.HorizonsScreen
import com.gpsdavida.app.ui.horizontes.WeeklyReviewScreen
import com.gpsdavida.app.ui.meudia.MeuDiaScreen
import com.gpsdavida.app.ui.planejamento.PlanningScreen
import com.gpsdavida.app.ui.planos.AdvancedPlansScreen
import com.gpsdavida.app.ui.routines.RoutineFormScreen
import com.gpsdavida.app.ui.routines.RoutinesListScreen
import com.gpsdavida.app.ui.semana.WeekDayScreen
import com.gpsdavida.app.ui.semana.WeekScreen
import com.gpsdavida.app.ui.tasks.TaskFormScreen
import com.gpsdavida.app.ui.tasks.TasksListScreen
import java.time.LocalDate

@Composable
fun GpsNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = currentRoute in setOf(GpsRoutes.AGORA, GpsRoutes.MEU_DIA, GpsRoutes.PLANNING, GpsRoutes.EVENTS, GpsRoutes.TASKS)
    Scaffold(bottomBar = {
        if (showBar) NavigationBar {
            NavigationBarItem(currentRoute == GpsRoutes.AGORA, { navController.navigateToTab(GpsRoutes.AGORA) }, { Icon(Icons.Filled.Home, null) }, label = { Text(stringResource(R.string.nav_agora)) })
            NavigationBarItem(currentRoute == GpsRoutes.MEU_DIA, { navController.navigateToTab(GpsRoutes.MEU_DIA) }, { Icon(Icons.Filled.DateRange, null) }, label = { Text(stringResource(R.string.nav_meu_dia)) })
            NavigationBarItem(currentRoute == GpsRoutes.PLANNING, { navController.navigateToTab(GpsRoutes.PLANNING) }, { Icon(Icons.Filled.Tune, null) }, label = { Text("Planejar") })
            NavigationBarItem(currentRoute == GpsRoutes.EVENTS, { navController.navigateToTab(GpsRoutes.EVENTS) }, { Icon(Icons.Filled.List, null) }, label = { Text(stringResource(R.string.nav_eventos)) })
            NavigationBarItem(currentRoute == GpsRoutes.TASKS, { navController.navigateToTab(GpsRoutes.TASKS) }, { Icon(Icons.Filled.Check, null) }, label = { Text(stringResource(R.string.nav_tarefas)) })
        }
    }) { padding ->
        NavHost(navController, GpsRoutes.AGORA, Modifier.padding(padding)) {
            composable(GpsRoutes.AGORA) { HomeScreen() }
            composable(GpsRoutes.MEU_DIA) { MeuDiaScreen({ navController.navigate(GpsRoutes.eventEditor()) }, { navController.navigate(GpsRoutes.eventEditor(it)) }, { navController.navigate(GpsRoutes.taskEditor(it)) }, { navController.navigate(GpsRoutes.habitEditor(it)) }, { navController.navigate(GpsRoutes.AVAILABILITY) }, { navController.navigate(GpsRoutes.WEEK) }) }
            composable(GpsRoutes.WEEK) { WeekScreen { date -> navController.navigate(GpsRoutes.weekDay(date)) } }
            composable(GpsRoutes.WEEK_DAY, arguments = listOf(navArgument("date") { type = NavType.StringType })) { entry -> WeekDayScreen(LocalDate.parse(entry.arguments?.getString("date"))) }
            composable(GpsRoutes.PLANNING) { PlanningScreen(onOpenHorizons = { navController.navigate(GpsRoutes.HORIZONS) }, onOpenReview = { navController.navigate(GpsRoutes.REVIEW) }, onOpenFinance = { navController.navigate(GpsRoutes.FINANCE) }, onOpenLifeAreas = { navController.navigate(GpsRoutes.LIFE_AREAS) }, onOpenDayCheckpoint = { navController.navigate(GpsRoutes.DAY_CHECKPOINT) }, onOpenPlans = { navController.navigate(GpsRoutes.PLANS) }) }
            composable(GpsRoutes.HORIZONS) { HorizonsScreen() }
            composable(GpsRoutes.REVIEW) { WeeklyReviewScreen() }
            composable(GpsRoutes.FINANCE) { FinanceScreen() }
            composable(GpsRoutes.LIFE_AREAS) { LifeAreaScreen() }
            composable(GpsRoutes.DAY_CHECKPOINT) { DailyCheckpointScreen() }
            composable(GpsRoutes.PLANS) { AdvancedPlansScreen() }
            composable(GpsRoutes.AVAILABILITY) { AvailabilityScreen() }
            composable(GpsRoutes.EVENTS) { EventsListScreen({ navController.navigate(GpsRoutes.eventEditor()) }, { navController.navigate(GpsRoutes.eventEditor(it)) }) }
            composable(GpsRoutes.TASKS) { TasksListScreen({ navController.navigate(GpsRoutes.taskEditor()) }, { navController.navigate(GpsRoutes.taskEditor(it)) }) }
            composable(GpsRoutes.HABITS) { HabitsListScreen({ navController.navigate(GpsRoutes.habitEditor()) }, { navController.navigate(GpsRoutes.habitEditor(it)) }) }
            composable(GpsRoutes.ROUTINES) { RoutinesListScreen({ navController.navigate(GpsRoutes.routineEditor()) }, { navController.navigate(GpsRoutes.routineEditor(it)) }) }
            composable(GpsRoutes.EVENT_EDITOR, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { EventFormScreen { navController.popBackStack() } }
            composable(GpsRoutes.TASK_EDITOR, arguments = listOf(navArgument("taskId") { type = NavType.StringType })) { TaskFormScreen { navController.popBackStack() } }
            composable(GpsRoutes.HABIT_EDITOR, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) { HabitFormScreen { navController.popBackStack() } }
            composable(GpsRoutes.ROUTINE_EDITOR, arguments = listOf(navArgument("routineId") { type = NavType.StringType })) { RoutineFormScreen { navController.popBackStack() } }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) { navigate(route) { popUpTo(graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
