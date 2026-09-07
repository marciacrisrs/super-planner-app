package com.gpsdavida.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
import com.gpsdavida.app.ui.events.EventFormScreen
import com.gpsdavida.app.ui.events.EventsListScreen
import com.gpsdavida.app.ui.habits.HabitFormScreen
import com.gpsdavida.app.ui.habits.HabitsListScreen
import com.gpsdavida.app.ui.home.HomeScreen
import com.gpsdavida.app.ui.meudia.MeuDiaScreen
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
    val showBar = currentRoute in setOf(
        GpsRoutes.AGORA, GpsRoutes.MEU_DIA, GpsRoutes.EVENTS,
        GpsRoutes.TASKS, GpsRoutes.HABITS, GpsRoutes.ROUTINES,
    )

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.AGORA,
                        onClick = { navController.navigateToTab(GpsRoutes.AGORA) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_agora)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.MEU_DIA,
                        onClick = { navController.navigateToTab(GpsRoutes.MEU_DIA) },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_meu_dia)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.EVENTS,
                        onClick = { navController.navigateToTab(GpsRoutes.EVENTS) },
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_eventos)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.TASKS,
                        onClick = { navController.navigateToTab(GpsRoutes.TASKS) },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_tarefas)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.HABITS,
                        onClick = { navController.navigateToTab(GpsRoutes.HABITS) },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_habitos)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == GpsRoutes.ROUTINES,
                        onClick = { navController.navigateToTab(GpsRoutes.ROUTINES) },
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_rotinas)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController, GpsRoutes.AGORA, Modifier.padding(padding)) {
            composable(GpsRoutes.AGORA) { HomeScreen() }
            composable(GpsRoutes.MEU_DIA) {
                MeuDiaScreen(
                    onAddEvent = { navController.navigate(GpsRoutes.eventEditor()) },
                    onOpenEvent = { id -> navController.navigate(GpsRoutes.eventEditor(id)) },
                    onOpenTask = { id -> navController.navigate(GpsRoutes.taskEditor(id)) },
                    onOpenHabit = { id -> navController.navigate(GpsRoutes.habitEditor(id)) },
                    onOpenAvailability = { navController.navigate(GpsRoutes.AVAILABILITY) },
                    onOpenWeek = { navController.navigate(GpsRoutes.WEEK) },
                )
            }
            composable(GpsRoutes.WEEK) {
                WeekScreen(onOpenDay = { date -> navController.navigate(GpsRoutes.weekDay(date)) })
            }
            composable(GpsRoutes.WEEK_DAY, arguments = listOf(navArgument("date") { type = NavType.StringType })) { entry ->
                WeekDayScreen(date = LocalDate.parse(entry.arguments?.getString("date")))
            }
            composable(GpsRoutes.AVAILABILITY) { AvailabilityScreen() }
            composable(GpsRoutes.EVENTS) { EventsListScreen({ navController.navigate(GpsRoutes.eventEditor()) }, { navController.navigate(GpsRoutes.eventEditor(it)) }) }
            composable(GpsRoutes.TASKS) { TasksListScreen({ navController.navigate(GpsRoutes.taskEditor()) }, { navController.navigate(GpsRoutes.taskEditor(it)) }) }
            composable(GpsRoutes.HABITS) { HabitsListScreen({ navController.navigate(GpsRoutes.habitEditor()) }, { navController.navigate(GpsRoutes.habitEditor(it)) }) }
            composable(GpsRoutes.ROUTINES) { RoutinesListScreen({ navController.navigate(GpsRoutes.routineEditor()) }, { navController.navigate(GpsRoutes.routineEditor(it)) }) }
            composable(GpsRoutes.EVENT_EDITOR, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                EventFormScreen(onDone = { navController.popBackStack() })
            }
            composable(GpsRoutes.TASK_EDITOR, arguments = listOf(navArgument("taskId") { type = NavType.StringType })) {
                TaskFormScreen(onDone = { navController.popBackStack() })
            }
            composable(GpsRoutes.HABIT_EDITOR, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) {
                HabitFormScreen(onDone = { navController.popBackStack() })
            }
            composable(GpsRoutes.ROUTINE_EDITOR, arguments = listOf(navArgument("routineId") { type = NavType.StringType })) {
                RoutineFormScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
