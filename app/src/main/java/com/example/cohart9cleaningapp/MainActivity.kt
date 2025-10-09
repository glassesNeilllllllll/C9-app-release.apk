package com.example.cohart9cleaningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cohart9cleaningapp.ui.theme.Cohart9CleaningAppTheme
import kotlinx.coroutines.delay
import java.util.*
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    // List of all students in the cohort
    private val students = listOf(
        "Bailasan", "Christina", "Cyrus", "Harshpreet", "Isaish",
        "Janna", "Laura", "Linda", "Madison", "Marianne",
        "Raghad", "Shaista", "Neil", "Tamara", "Thomas",
        "Wesley", "Yunjia", "Ruby"
    )

    // Cleaning areas labeled A through R
    private val areas = ('A'..'R').map { it.toString() }

    // Detailed descriptions for each cleaning area
    private val areaDescriptions = mapOf(
        "A" to "Clean Up Supervisor Front and Back Area. Dental Laboratory Classroom 332, 333 & 334",
        "B" to "Porcelain Room",
        "C" to "Acrylic Packing & Sink",
        "D" to "Printing & Sink / Counter. Boil Out & Curing Tank",
        "E" to "Trimmers & Sink",
        "F" to "Mixer Counter",
        "G" to "Mixer Counter",
        "H" to "Trimmers & Sink",
        "I" to "Trimmers & Sink",
        "J" to "Mixer Counter",
        "K" to "Mixer Counter",
        "L" to "Trimmers & Sink",
        "M" to "Sand Blaster",
        "N" to "Left Polishing",
        "O" to "Right Polishing",
        "P" to "Sink & Counter",
        "Q" to "Sink & Steamer",
        "R" to "Casting & Gypsum Area"
    )

    /**
     * Calculate which area a student is responsible for on a given day
     * @param student The student's name
     * @param day The day of the month
     * @return The area code (A-R) the student is assigned to
     */
    private fun getDutyAreaForStudent(student: String, day: Int): String {
        val studentIndex = students.indexOf(student)
        if (studentIndex == -1) return "A" // Default to area A if student not found

        // Calculate area based on student index and day, rotating through all areas
        val startAreaIndex = studentIndex % areas.size
        val areaIndex = (startAreaIndex + day - 1) % areas.size
        return areas[areaIndex]
    }

    /**
     * Check if two Calendar instances represent the same date
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Get the avatar resource ID for a given student name
     * @param studentName The name of the student
     * @return Resource ID of the avatar image, or default if not found
     */
    private fun getAvatarResourceId(studentName: String): Int {
        return when (studentName.lowercase()) {
            "bailasan" -> R.drawable.avatar_bailasan
            "christina" -> R.drawable.avatar_christina
            "cyrus" -> R.drawable.avatar_cyrus
            "harshpreet" -> R.drawable.avatar_harshpreet
            "isaish" -> R.drawable.avatar_isaish
            "janna" -> R.drawable.avatar_janna
            "laura" -> R.drawable.avatar_laura
            "linda" -> R.drawable.avatar_linda
            "madison" -> R.drawable.avatar_madison
            "marianne" -> R.drawable.avatar_marianne
            "raghad" -> R.drawable.avatar_raghad
            "shaista" -> R.drawable.avatar_shaista
            "neil" -> R.drawable.avatar_neil
            "tamara" -> R.drawable.avatar_tamara
            "thomas" -> R.drawable.avatar_thomas
            "wesley" -> R.drawable.avatar_wesley
            "yunjia" -> R.drawable.avatar_yunjia
            "ruby" -> R.drawable.avatar_ruby
            else -> R.drawable.avatar_default
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val savedStudent = prefs.getString("student_name", null)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        setContent {
            Cohart9CleaningAppTheme {
                var selectedStudent by remember { mutableStateOf(savedStudent) }
                var showWelcome by remember { mutableStateOf(false) }
                var showFirstTimeSelection by remember {
                    mutableStateOf(isFirstLaunch && savedStudent == null)
                }
                var showCalendar by remember { mutableStateOf(false) }
                var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
                var isSwitchingStudent by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (savedStudent != null && !isFirstLaunch) {
                        // Show welcome back only on app startup, not when switching students
                        showWelcome = true
                    }
                }

                Scaffold(
                    topBar = {
                        when {
                            showFirstTimeSelection -> TopAppBar(
                                title = { Text("Cohart9", color = Color.White) },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13547A))
                            )
                            showCalendar -> TopAppBar(
                                title = { Text("Select Date", color = Color.White) },
                                navigationIcon = {
                                    IconButton(onClick = { showCalendar = false }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13547A))
                            )
                            selectedStudent != null && !showWelcome -> TopAppBar(
                                title = { Text("Cohart9 - $selectedStudent", color = Color.White) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        // When clicking the person icon to switch students
                                        selectedStudent = null
                                        showWelcome = false
                                        isSwitchingStudent = true
                                    }) {
                                        Icon(Icons.Filled.Person, "Change Student", tint = Color.White)
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showCalendar = true }) {
                                        Icon(Icons.Filled.DateRange, "Calendar", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13547A))
                            )
                            else -> TopAppBar(
                                title = { Text("Cohart9", color = Color.White) },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13547A))
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            showFirstTimeSelection -> FirstTimeSelectionScreen(
                                students = students,
                                onSelect = { student ->
                                    selectedStudent = student
                                    prefs.edit {
                                        putString("student_name", student)
                                        putBoolean("is_first_launch", false)
                                    }
                                    showFirstTimeSelection = false
                                    showWelcome = true
                                }
                            )
                            showCalendar -> CalendarScreen(
                                selectedDate = selectedDate,
                                onDateSelected = { date ->
                                    selectedDate = date
                                    showCalendar = false
                                },
                                onBack = { showCalendar = false },
                                student = selectedStudent ?: "",
                                getDutyAreaForStudent = ::getDutyAreaForStudent
                            )
                            selectedStudent == null -> StudentSelectionScreen(
                                students = students,
                                selectedStudent = null,
                                onSelect = { student ->
                                    selectedStudent = student
                                    prefs.edit { putString("student_name", student) }

                                    if (isSwitchingStudent) {
                                        // Show switched message when changing students
                                        showWelcome = true
                                    } else if (isFirstLaunch) {
                                        // Show welcome only on first launch
                                        showWelcome = true
                                    }
                                    // For returning users, don't show welcome when selecting from main screen
                                }
                            )
                            showWelcome -> WelcomeAnimationScreen(
                                student = selectedStudent!!,
                                onAnimationComplete = {
                                    showWelcome = false
                                    isSwitchingStudent = false
                                },
                                isFirstTime = isFirstLaunch && savedStudent == null,
                                isSwitching = isSwitchingStudent
                            )
                            else -> DutyScreen(
                                student = selectedStudent!!,
                                onBack = {
                                    selectedStudent = null
                                    showWelcome = false
                                    isSwitchingStudent = true
                                },
                                selectedDate = selectedDate,
                                getDutyAreaForStudent = ::getDutyAreaForStudent,
                                isSameDay = ::isSameDay
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Composable that shows a scroll indicator when content overflows
     */
    @Composable
    private fun ScrollIndicatorBox(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val scrollState = rememberScrollState()
        val showBottomIndicator = scrollState.canScrollForward

        Box(modifier = modifier) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).fillMaxSize()
            ) {
                content()
            }

            // Show gradient indicator at bottom when more content is available
            if (showBottomIndicator) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x80000000))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Scroll down for more content",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(32.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }

    /**
     * First-time user experience screen for selecting a student
     */
    @Composable
    private fun FirstTimeSelectionScreen(
        students: List<String>,
        onSelect: (String) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
        ) {
            ScrollIndicatorBox(
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome to Cohart9 Cleaning App",
                        fontSize = 28.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Please select your name to get started",
                        fontSize = 18.sp,
                        color = Color(0xFFE3F2FD),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // List of all students as selectable cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        students.forEach { student ->
                            StudentSelectionCard(
                                student = student,
                                isSelected = false,
                                onSelect = { onSelect(student) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "You can change your selection later from the main screen",
                        fontSize = 14.sp,
                        color = Color(0xFFBBDEFB),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    /**
     * Welcome animation screen shown after student selection
     */
    @Composable
    private fun WelcomeAnimationScreen(
        student: String,
        onAnimationComplete: () -> Unit,
        isFirstTime: Boolean = false,
        isSwitching: Boolean = false
    ) {
        // Animation states
        var alphaState by remember { mutableFloatStateOf(0f) }
        var scaleState by remember { mutableFloatStateOf(0f) }
        var progress by remember { mutableFloatStateOf(0f) }

        // Use shorter duration for switching students
        val animationDuration = if (isSwitching) 1500 else 2500

        // Animated values
        val alphaAnim by animateFloatAsState(
            targetValue = alphaState,
            animationSpec = tween(animationDuration)
        )
        val scaleAnim by animateFloatAsState(
            targetValue = scaleState,
            animationSpec = tween(animationDuration)
        )
        val progressAnim by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(animationDuration)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alphaAnim)
                    .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
            ) {
                Text(
                    text = when {
                        isSwitching -> "Switched to"
                        isFirstTime -> "Welcome,"
                        else -> "Welcome back,"
                    },
                    fontSize = 32.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = student,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFFA500)
                )

                Spacer(modifier = Modifier.height(40.dp))

                LoadingProgressBar(progressAnim)
            }
        }

        // Start animations and navigate after completion
        LaunchedEffect(Unit) {
            alphaState = 1f
            scaleState = 1f
            progress = 1f
            delay(animationDuration.toLong())
            onAnimationComplete()
        }
    }

    /**
     * Animated progress bar for loading states
     */
    @Composable
    private fun LoadingProgressBar(progress: Float) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0x44FFFFFF), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(Color(0xFFFFA500), RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Loading...", fontSize = 12.sp, color = Color(0xFFCCCCCC), fontWeight = FontWeight.Normal)
        }
    }

    /**
     * Screen for selecting a student from the list
     */
    @Composable
    private fun StudentSelectionScreen(
        students: List<String>,
        selectedStudent: String?,
        onSelect: (String) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
        ) {
            ScrollIndicatorBox(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column {
                    Text(
                        text = "Select Student",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    students.forEach { student ->
                        StudentSelectionCard(
                            student = student,
                            isSelected = selectedStudent == student,
                            onSelect = { onSelect(student) }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    /**
     * Individual student selection card with avatar and name
     */
    @Composable
    private fun StudentSelectionCard(
        student: String,
        isSelected: Boolean,
        onSelect: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFBBDEFB) else Color(0xFFB2DFDB)
            ),
            elevation = CardDefaults.cardElevation(12.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = getAvatarResourceId(student)),
                    contentDescription = "$student's avatar",
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = student,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    /**
     * Calendar screen for selecting dates and viewing duties
     */
    @Composable
    private fun CalendarScreen(
        selectedDate: Calendar,
        onDateSelected: (Calendar) -> Unit,
        onBack: () -> Unit,
        student: String,
        getDutyAreaForStudent: (String, Int) -> String
    ) {
        var currentMonth by remember {
            mutableStateOf(Calendar.getInstance().apply { time = selectedDate.time })
        }

        // Update current month when selected date changes
        LaunchedEffect(selectedDate) {
            if (selectedDate.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH) ||
                selectedDate.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                currentMonth = Calendar.getInstance().apply { time = selectedDate.time }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
        ) {
            ScrollIndicatorBox(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column {
                    MonthNavigation(
                        currentMonth = currentMonth,
                        onPrevious = {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, -1)
                            currentMonth = newMonth
                        },
                        onNext = {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    WeekDayHeaders()
                    Spacer(modifier = Modifier.height(8.dp))
                    CalendarGrid(currentMonth, selectedDate, onDateSelected)
                    Spacer(modifier = Modifier.height(16.dp))
                    DutyInformationCard(selectedDate, student, getDutyAreaForStudent)
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarActionButtons(onBack, onDateSelected)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    /**
     * Month navigation header with previous/next buttons
     */
    @Composable
    private fun MonthNavigation(
        currentMonth: Calendar,
        onPrevious: () -> Unit,
        onNext: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", tint = Color.White)
            }

            Text(
                text = "${currentMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${currentMonth.get(Calendar.YEAR)}",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month", tint = Color.White)
            }
        }
    }

    /**
     * Header row showing days of the week
     */
    @Composable
    private fun WeekDayHeaders() {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    /**
     * Calendar grid showing days of the month
     */
    @Composable
    private fun CalendarGrid(
        currentMonth: Calendar,
        selectedDate: Calendar,
        onDateSelected: (Calendar) -> Unit
    ) {
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
        val today = Calendar.getInstance()

        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            val weeks = (daysInMonth + startingDayOfWeek - 1) / 7 + 1

            Column {
                repeat(weeks) { weekIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayIndex in 0..6) {
                            val dayNumber = weekIndex * 7 + dayIndex - startingDayOfWeek + 2
                            val isCurrentMonth = dayNumber in 1..daysInMonth
                            val dayCalendar = Calendar.getInstance().apply {
                                time = currentMonth.time
                                if (isCurrentMonth) set(Calendar.DAY_OF_MONTH, dayNumber)
                            }

                            val isToday = isCurrentMonth && isSameDay(dayCalendar, today)
                            val isSelected = isCurrentMonth && isSameDay(dayCalendar, selectedDate)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        color = when {
                                            isSelected -> Color(0xFFFFA500)
                                            isToday -> Color(0xFF64B5F6)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable(isCurrentMonth) {
                                        onDateSelected(dayCalendar)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentMonth) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = Color.White,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Card showing duty information for selected date
     */
    @Composable
    private fun DutyInformationCard(
        selectedDate: Calendar,
        student: String,
        getDutyAreaForStudent: (String, Int) -> String
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Duty for ${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                    fontSize = 18.sp,
                    color = Color(0xFF13547A),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (student.isNotEmpty()) {
                    val dutyArea = getDutyAreaForStudent(student, selectedDate.get(Calendar.DAY_OF_MONTH))
                    val dutyDescription = areaDescriptions[dutyArea] ?: ""

                    Text("$student is responsible for Area $dutyArea", fontSize = 16.sp, color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dutyDescription, fontSize = 14.sp, color = Color(0xFF666666))
                } else {
                    Text("Please select a student first", fontSize = 16.sp, color = Color(0xFF666666))
                }
            }
        }
    }

    /**
     * Action buttons for calendar screen
     */
    @Composable
    private fun CalendarActionButtons(onBack: () -> Unit, onDateSelected: (Calendar) -> Unit) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { onDateSelected(Calendar.getInstance()); onBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("Back to Today")
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Close Calendar")
            }
        }
    }

    /**
     * Main duty screen showing student's cleaning assignment
     */
    @Composable
    private fun DutyScreen(
        student: String,
        onBack: () -> Unit,
        selectedDate: Calendar = Calendar.getInstance(),
        getDutyAreaForStudent: (String, Int) -> String,
        isSameDay: (Calendar, Calendar) -> Boolean
    ) {
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val isToday = isSameDay(selectedDate, Calendar.getInstance())
        val dutyArea = getDutyAreaForStudent(student, day)
        val dutyDescription = areaDescriptions[dutyArea] ?: ""

        // Animation states for entrance animation
        var isContentVisible by remember { mutableStateOf(false) }
        val alphaAnim by animateFloatAsState(
            targetValue = if (isContentVisible) 1f else 0f,
            animationSpec = tween(600, easing = LinearOutSlowInEasing)
        )
        val scaleAnim by animateFloatAsState(
            targetValue = if (isContentVisible) 1f else 0.8f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )

        // Trigger entrance animation when student or date changes
        LaunchedEffect(student, selectedDate) {
            isContentVisible = false
            delay(50)
            isContentVisible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
        ) {
            ScrollIndicatorBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer {
                        alpha = alphaAnim
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Show date indicator if not viewing today
                    if (!isToday) {
                        DateIndicatorCard(selectedDate, alphaAnim, isContentVisible)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    DutyInfoCard(selectedDate, student, dutyArea, dutyDescription, isToday, alphaAnim, isContentVisible)
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedLayoutCanvas(dutyArea, alphaAnim, scaleAnim)
                    Spacer(modifier = Modifier.height(16.dp))
                    BackButton(onBack, alphaAnim, isContentVisible)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    /**
     * Card showing the selected date when not viewing today
     */
    @Composable
    private fun DateIndicatorCard(selectedDate: Calendar, alpha: Float, isContentVisible: Boolean) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = if (isContentVisible) 0f else 20f
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = "Viewing: ${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                fontSize = 16.sp,
                color = Color(0xFFE65100),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }

    /**
     * Card showing detailed duty information
     */
    @Composable
    private fun DutyInfoCard(
        selectedDate: Calendar,
        student: String,
        dutyArea: String,
        dutyDescription: String,
        isToday: Boolean,
        alpha: Float,
        isContentVisible: Boolean
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = if (isContentVisible) 0f else 30f
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isToday) "Today ${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}"
                    else "Date ${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                    fontSize = 20.sp,
                    color = Color(0xFF13547A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("$student is responsible for", fontSize = 20.sp, color = Color(0xFF1976D2), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Area $dutyArea", fontSize = 24.sp, color = Color(0xFFFFA500), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(dutyDescription, fontSize = 16.sp, color = Color(0xFF666666), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Normal, lineHeight = 20.sp)
            }
        }
    }

    /**
     * Animated wrapper for the layout canvas
     */
    @Composable
    private fun AnimatedLayoutCanvas(dutyArea: String, alpha: Float, scale: Float) {
        Box(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
        ) {
            CustomLayoutCanvas(areas = areas, highlightArea = dutyArea)
        }
    }

    /**
     * Back button to return to student selection
     */
    @Composable
    private fun BackButton(onBack: () -> Unit, alpha: Float, isContentVisible: Boolean) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = if (isContentVisible) 0f else 40f
                }
        ) {
            Text("View Other Students' Duties", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }

    /**
     * Custom canvas that draws the laboratory layout with highlighted area
     */
    @Composable
    private fun CustomLayoutCanvas(areas: List<String>, highlightArea: String) {
        // Layout definition for the laboratory areas
        val layout = listOf(
            listOf("A", "B"),
            listOf("D", "D", "C", "C"),
            listOf("H", "G", "F", "E"),
            listOf("I", "J", "K", "L"),
            listOf("M", "N", "O", "P", "Q", "R"),
            listOf("F", "A")
        )

        Canvas(modifier = Modifier.fillMaxWidth().height(480.dp)) {
            val totalWidth = size.width
            val totalHeight = size.height
            val horizontalSpacing = 14f
            val verticalSpacing = 8f

            // Calculate cell dimensions based on screen size
            val baseCellHeight = totalHeight / 7f
            val columnHeights = mapOf(1 to baseCellHeight * 0.8f, 2 to baseCellHeight * 0.8f, 3 to baseCellHeight * 0.8f, 4 to baseCellHeight)

            val totalColumns = layout.size
            val narrowColumnsCount = 2
            val standardColumnsCount = totalColumns - narrowColumnsCount
            val narrowWidthRatio = 0.8f
            val totalSpacing = (totalColumns - 1) * horizontalSpacing

            val availableWidth = totalWidth - totalSpacing
            val totalWidthRatio = (narrowColumnsCount * narrowWidthRatio) + standardColumnsCount
            val baseCellWidth = availableWidth / totalWidthRatio
            val narrowCellWidth = baseCellWidth * narrowWidthRatio
            val standardCellWidth = baseCellWidth

            val totalGridWidth = (narrowCellWidth * 2) + (standardCellWidth * 4) + (horizontalSpacing * 5)
            val startX = (totalWidth - totalGridWidth) / 2

            // Calculate column starting positions
            val column1StartX = startX
            val column2StartX = column1StartX + narrowCellWidth + horizontalSpacing
            val column3StartX = column2StartX + standardCellWidth + horizontalSpacing
            val column4StartX = column3StartX + standardCellWidth + horizontalSpacing
            val column5StartX = column4StartX + standardCellWidth + horizontalSpacing
            val column6StartX = column5StartX + standardCellWidth + horizontalSpacing

            val column5CellHeight = columnHeights[4] ?: baseCellHeight
            val column5TotalHeight = layout[4].size * column5CellHeight + (layout[4].size - 1) * verticalSpacing
            val column5StartY = (totalHeight - column5TotalHeight) / 2 - 35f

            val mTop = column5StartY
            val rBottom = column5StartY + column5TotalHeight

            val totalAvailableHeight = rBottom - mTop
            val column1CellHeight = (totalAvailableHeight - verticalSpacing) / 2f

            val aTop = mTop
            val aBottom = aTop + column1CellHeight
            val bTop = aBottom + verticalSpacing

            val column2CellHeight = columnHeights[1] ?: baseCellHeight
            val column2TotalHeight = layout[1].size * column2CellHeight + (layout[1].size - 1) * verticalSpacing
            val column2StartY = (totalHeight - column2TotalHeight) / 2 - 35f

            val blankRectWidth = standardCellWidth * 2f + horizontalSpacing
            val blankRectHeight = baseCellHeight * 0.6f
            val blankRectX = column2StartX
            val blankRectY = column2StartY - blankRectHeight - verticalSpacing

            // Draw blank cell for "Printing & Counter" area
            drawRoundRect(
                color = Color(0xFFE3F2FD),
                topLeft = Offset(blankRectX, blankRectY),
                size = Size(blankRectWidth, blankRectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            drawRoundRect(
                color = Color(0xFF90CAF9),
                topLeft = Offset(blankRectX, blankRectY),
                size = Size(blankRectWidth, blankRectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Draw text for blank cell
            drawContext.canvas.nativeCanvas.apply {
                val text = "Printing & Counter"
                val paint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = "#1565C0".toColorInt()
                    isFakeBoldText = true
                }

                var textSize = blankRectHeight * 0.5f
                paint.textSize = textSize
                var textWidth = paint.measureText(text)

                // Adjust text size to fit within cell
                while (textWidth > blankRectWidth * 0.95f && textSize > blankRectHeight * 0.2f) {
                    textSize -= 1f
                    paint.textSize = textSize
                    textWidth = paint.measureText(text)
                }

                val x = blankRectX + blankRectWidth / 2
                val y = blankRectY + blankRectHeight / 2 + textSize / 3
                drawText(text, x, y, paint)
            }

            // Draw column 1 cells (A and B)
            listOf(Pair("A", aTop to column1CellHeight), Pair("B", bTop to column1CellHeight)).forEach { (areaChar, position) ->
                val (top, height) = position
                val area = areas.find { it == areaChar } ?: return@forEach
                val left = column1StartX
                val cellColor = if (area == highlightArea) Color(0xFFFFA500) else Color(0xFF64B5F6)

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(left, top),
                    size = Size(narrowCellWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Draw area label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = height * 0.2f
                        color = android.graphics.Color.WHITE
                        isFakeBoldText = true
                    }
                    val x = left + narrowCellWidth / 2
                    val y = top + height / 2 + paint.textSize / 3
                    drawText(area, x, y, paint)
                }
            }

            // Define column positions and widths
            val columnPositions = listOf(
                column1StartX to narrowCellWidth,
                column2StartX to standardCellWidth,
                column3StartX to standardCellWidth,
                column4StartX to standardCellWidth,
                column5StartX to standardCellWidth,
                column6StartX to narrowCellWidth
            )

            // Draw all other columns
            for (colIndex in 1 until layout.size) {
                val columnAreas = layout[colIndex]
                val (columnStartX, baseColumnWidth) = columnPositions[colIndex]

                val cellHeight = if (colIndex == 5) column1CellHeight else columnHeights[colIndex] ?: baseCellHeight
                val columnTotalHeight = columnAreas.size * cellHeight + (columnAreas.size - 1) * verticalSpacing
                val startY = (totalHeight - columnTotalHeight) / 2 - 35f

                for ((rowIndex, areaChar) in columnAreas.withIndex()) {
                    val area = areas.find { it == areaChar } ?: continue

                    var top = startY + rowIndex * (cellHeight + verticalSpacing)
                    var left = columnStartX
                    var currentCellWidth = baseColumnWidth
                    var currentCellHeight = cellHeight

                    // Special handling for area R
                    val isCellR = colIndex == 4 && rowIndex == columnAreas.size - 1 && area == "R"
                    if (isCellR) {
                        currentCellWidth = column5StartX + standardCellWidth - column3StartX
                        currentCellHeight = baseCellHeight * 0.7f
                        left = column3StartX
                        top = rBottom - currentCellHeight
                    }

                    val isColumn6 = colIndex == 5
                    val isBlankArea = isColumn6 && areaChar == "F"

                    if (isBlankArea) {
                        // Draw blank faculty area
                        drawRoundRect(
                            color = Color(0xFFE3F2FD),
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF90CAF9),
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                        // Draw rotated text for faculty area
                        drawContext.canvas.nativeCanvas.withSave {
                            val text = "Faculty"
                            val paint = android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = "#1565C0".toColorInt()
                                isFakeBoldText = true
                            }
                            var textSize = currentCellWidth * 0.4f
                            paint.textSize = textSize
                            var textWidth = paint.measureText(text)
                            while (textWidth > currentCellHeight * 0.95f && textSize > currentCellWidth * 0.15f) {
                                textSize -= 1f
                                paint.textSize = textSize
                                textWidth = paint.measureText(text)
                            }
                            val centerX = left + currentCellWidth / 2
                            val centerY = top + currentCellHeight / 2
                            rotate(90f, centerX, centerY)
                            val x = centerX
                            val y = centerY + textSize / 3
                            drawText(text, x, y, paint)
                        }
                    } else {
                        // Draw regular area cell
                        val cellColor = if (area == highlightArea) Color(0xFFFFA500) else Color(0xFF64B5F6)
                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        // Draw area label
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = android.graphics.Color.WHITE
                                isFakeBoldText = true
                            }
                            val textSize = when {
                                isColumn6 && areaChar == "A" -> currentCellHeight * 0.2f
                                isCellR -> (columnHeights[1] ?: baseCellHeight) * 0.25f
                                else -> currentCellHeight * 0.25f
                            }
                            paint.textSize = textSize
                            val x = left + currentCellWidth / 2
                            val y = top + currentCellHeight / 2 + paint.textSize / 3
                            drawText(area, x, y, paint)
                        }
                    }
                }
            }
        }
    }
}