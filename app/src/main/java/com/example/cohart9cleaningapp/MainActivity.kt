package com.example.cohart9cleaningapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

class MainActivity : ComponentActivity() {

    // Updated student list
    private val students = listOf(
        "Bailasan", "Christina", "Cyrus", "Harshpreet", "Isaish",
        "Janna", "Laura", "Linda", "Madison", "Marianne",
        "Raghad", "Shaista", "Neil", "Tamara", "Thomas",
        "Wesley", "Yunjia", "Ruby"
    )

    private val areas = ('A'..'R').map { it.toString() }

    // Calculate student's duty area for the day based on schedule
    private fun getDutyAreaForStudent(student: String, day: Int): String {
        val studentIndex = students.indexOf(student)
        if (studentIndex == -1) return "A" // Default to area A

        // Schedule pattern: each student's area cycles sequentially
        // Student 0 (Bailasan) starts from A, student 1 (Christina) from B, etc.
        val startAreaIndex = studentIndex % areas.size
        val areaIndex = (startAreaIndex + day - 1) % areas.size
        return areas[areaIndex]
    }

    // Helper function to check if two calendars represent the same day
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    // Get avatar resource ID based on student name
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
            else -> R.drawable.avatar_default // Default avatar
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedStudent = prefs.getString("student_name", null)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        setContent {
            Cohart9CleaningAppTheme {
                var selectedStudent by remember { mutableStateOf(savedStudent) }
                var showWelcome by remember { mutableStateOf(false) }
                var showFirstTimeSelection by remember { mutableStateOf(isFirstLaunch && savedStudent == null) }
                var showCalendar by remember { mutableStateOf(false) }
                var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

                // Show welcome screen only on app cold start
                LaunchedEffect(Unit) {
                    if (savedStudent != null) {
                        showWelcome = true
                    }
                }

                Scaffold(
                    topBar = {
                        when {
                            showFirstTimeSelection -> {
                                // No top bar for first time selection
                            }

                            showCalendar -> {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Select Date",
                                            color = Color.White
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { showCalendar = false }) {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF13547A)
                                    )
                                )
                            }

                            selectedStudent != null && !showWelcome -> {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Cohart9 - ${selectedStudent}",
                                            color = Color.White
                                        )
                                    },
                                    navigationIcon = {
                                        // Change to person icon to indicate student selection
                                        IconButton(onClick = {
                                            selectedStudent = null
                                            showWelcome = false
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.Person, // Use person icon for student selection
                                                contentDescription = "Change Student",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            showCalendar = true
                                            selectedDate = Calendar.getInstance()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.DateRange,
                                                contentDescription = "Calendar",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF13547A)
                                    )
                                )
                            }

                            else -> {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Cohart9",
                                            color = Color.White
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF13547A)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            showFirstTimeSelection -> {
                                FirstTimeSelectionScreen(
                                    students = students,
                                    onSelect = { student ->
                                        selectedStudent = student
                                        prefs.edit().putString("student_name", student).apply()
                                        prefs.edit().putBoolean("is_first_launch", false).apply()
                                        showFirstTimeSelection = false
                                        // Show welcome screen when first selecting student
                                        showWelcome = true
                                    }
                                )
                            }

                            showCalendar -> {
                                CalendarScreen(
                                    selectedDate = selectedDate,
                                    onDateSelected = { date ->
                                        selectedDate = date
                                    },
                                    onBack = { showCalendar = false },
                                    student = selectedStudent ?: "",
                                    getDutyAreaForStudent = { student, day ->
                                        getDutyAreaForStudent(student, day)
                                    }
                                )
                            }

                            selectedStudent == null -> {
                                StudentSelectionScreen(
                                    students = students,
                                    selectedStudent = null,
                                    onSelect = { student ->
                                        selectedStudent = student
                                        prefs.edit().putString("student_name", student).apply()
                                        // Do not show welcome screen when switching students
                                    }
                                )
                            }

                            showWelcome -> {
                                WelcomeAnimationScreen(
                                    student = selectedStudent!!,
                                    onAnimationComplete = {
                                        showWelcome = false
                                    },
                                    isFirstTime = isFirstLaunch
                                )
                            }

                            else -> {
                                DutyScreen(
                                    student = selectedStudent!!,
                                    onBack = {
                                        selectedStudent = null
                                        showWelcome = false
                                    },
                                    selectedDate = selectedDate,
                                    getDutyAreaForStudent = { student, day ->
                                        getDutyAreaForStudent(student, day)
                                    },
                                    isSameDay = { cal1, cal2 ->
                                        isSameDay(cal1, cal2)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FirstTimeSelectionScreen(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(student) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFB2DFDB)
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
                                    painter = painterResource(
                                        id = getAvatarResourceId(student)
                                    ),
                                    contentDescription = "$student's avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
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
                }

                Text(
                    text = "You can change your selection later from the main screen",
                    fontSize = 14.sp,
                    color = Color(0xFFBBDEFB),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }

    @Composable
    fun WelcomeAnimationScreen(
        student: String,
        onAnimationComplete: () -> Unit,
        isFirstTime: Boolean = false
    ) {
        var alphaState by remember { mutableStateOf(0f) }
        var scaleState by remember { mutableStateOf(0f) }
        var progress by remember { mutableStateOf(0f) }

        val alphaAnim by animateFloatAsState(
            targetValue = alphaState,
            animationSpec = tween(durationMillis = 2500)
        )
        val scaleAnim by animateFloatAsState(
            targetValue = scaleState,
            animationSpec = tween(durationMillis = 2500)
        )
        val progressAnim by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 2500)
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
                    text = if (isFirstTime) "Welcome," else "Welcome back,",
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(200.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                color = Color(0x44FFFFFF),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressAnim)
                                .height(8.dp)
                                .background(
                                    color = Color(0xFFFFA500),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(progressAnim * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Loading...",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            alphaState = 1f
            scaleState = 1f
            progress = 1f
            delay(2500)
            onAnimationComplete()
        }
    }

    @Composable
    fun StudentSelectionScreen(
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    val isSelected = selectedStudent == student
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(student) },
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
                                painter = painterResource(
                                    id = getAvatarResourceId(student)
                                ),
                                contentDescription = "$student's avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
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
            }
        }
    }

    @Composable
    fun CalendarScreen(
        selectedDate: Calendar,
        onDateSelected: (Calendar) -> Unit,
        onBack: () -> Unit,
        student: String,
        getDutyAreaForStudent: (String, Int) -> String
    ) {
        // Use remember to save current month and ensure state updates
        var currentMonth by remember {
            mutableStateOf(Calendar.getInstance().apply {
                time = selectedDate.time // Initialize with selected date
            })
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            // Create new Calendar instance to trigger recomposition
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, -1)
                            currentMonth = newMonth
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "${
                            currentMonth.getDisplayName(
                                Calendar.MONTH,
                                Calendar.LONG,
                                Locale.getDefault()
                            )
                        } ${currentMonth.get(Calendar.YEAR)}",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            // Create new Calendar instance to trigger recomposition
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day of week headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar days
                val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
                val today = Calendar.getInstance()

                LazyColumn {
                    items((daysInMonth + startingDayOfWeek - 1) / 7 + 1) { weekIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayIndex in 0..6) {
                                val dayNumber = weekIndex * 7 + dayIndex - startingDayOfWeek + 2
                                val isCurrentMonth = dayNumber in 1..daysInMonth
                                val dayCalendar = Calendar.getInstance().apply {
                                    time = currentMonth.time
                                    if (isCurrentMonth) {
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                    }
                                }

                                val isToday = isCurrentMonth &&
                                        dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                        dayCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                        dayCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                                val isSelected = isCurrentMonth &&
                                        dayCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                        dayCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                                        dayCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(
                                    Calendar.DAY_OF_MONTH
                                )

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
                                            if (isCurrentMonth) {
                                                onDateSelected(dayCalendar)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrentMonth) {
                                        Text(
                                            text = dayNumber.toString(),
                                            color = when {
                                                isSelected || isToday -> Color.White
                                                else -> Color.White
                                            },
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected date duty information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Duty for ${selectedDate.get(Calendar.YEAR)}-${
                                selectedDate.get(
                                    Calendar.MONTH
                                ) + 1
                            }-${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                            fontSize = 18.sp,
                            color = Color(0xFF13547A),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (student.isNotEmpty()) {
                            val dutyArea = getDutyAreaForStudent(
                                student,
                                selectedDate.get(Calendar.DAY_OF_MONTH)
                            )

                            val areaDescriptions = mapOf(
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

                            val dutyDescription = areaDescriptions[dutyArea] ?: ""

                            Text(
                                text = "$student is responsible for Area $dutyArea",
                                fontSize = 16.sp,
                                color = Color(0xFF1976D2)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dutyDescription,
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        } else {
                            Text(
                                text = "Please select a student first",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Back to Today")
                    }

                    Button(
                        onClick = {
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                        Text("View Selected Date")
                    }
                }
            }
        }
    }

    @Composable
    fun DutyScreen(
        student: String,
        onBack: () -> Unit,
        selectedDate: Calendar = Calendar.getInstance(),
        getDutyAreaForStudent: (String, Int) -> String,
        isSameDay: (Calendar, Calendar) -> Boolean
    ) {
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val isToday = isSameDay(selectedDate, Calendar.getInstance())

        // Calculate duty area based on schedule
        val dutyArea = getDutyAreaForStudent(student, day)

        // Area description mapping
        val areaDescriptions = mapOf(
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

        val dutyDescription = areaDescriptions[dutyArea] ?: ""

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date indicator - only show when viewing non-today date
                if (!isToday) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = "Viewing: ${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                            fontSize = 16.sp,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Top information card - updated to match calendar style
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                        Text(
                            text = "$student is responsible for",
                            fontSize = 20.sp,
                            color = Color(0xFF1976D2),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Area $dutyArea",
                            fontSize = 24.sp,
                            color = Color(0xFFFFA500),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = dutyDescription,
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Normal,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom layout Canvas - should always be displayed
                CustomLayoutCanvas(
                    areas = areas,
                    highlightArea = dutyArea
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        "View Other Students' Duties",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun CustomLayoutCanvas(
        areas: List<String>,
        highlightArea: String
    ) {
        // Define irregular column layout
        val layout = listOf(
            listOf("A", "B"),                    // Column 1: 2 items
            listOf("D", "D", "C", "C"),          // Column 2: 4 items
            listOf("H", "G", "F", "E"),          // Column 3: 4 items
            listOf("I", "J", "K", "L"),          // Column 4: 4 items
            listOf("M", "N", "O", "P", "Q", "R"), // Column 5: 6 items
            listOf("F", "A")                     // Column 6: 2 items
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val horizontalSpacing = 14f
            val verticalSpacing = 8f

            // Base cell height
            val baseCellHeight = totalHeight / 7f

            // Set different cell heights for each column
            val columnHeights = mapOf(
                1 to baseCellHeight,        // Column 2: standard height
                2 to baseCellHeight * 0.8f, // Column 3: 80% height
                3 to baseCellHeight * 0.8f, // Column 4: 80% height
                4 to baseCellHeight         // Column 5: standard height
            )

            // Calculate exact widths to ensure equal margins and column widths
            val totalColumns = layout.size
            val narrowColumnsCount = 2 // Column 1 and column 6
            val standardColumnsCount = totalColumns - narrowColumnsCount

            val narrowWidthRatio = 0.8f
            val totalSpacing = (totalColumns - 1) * horizontalSpacing

            // Calculate cell widths that will result in equal margins
            val availableWidth = totalWidth - totalSpacing
            val totalWidthRatio = (narrowColumnsCount * narrowWidthRatio) + standardColumnsCount
            val baseCellWidth = availableWidth / totalWidthRatio
            val narrowCellWidth = baseCellWidth * narrowWidthRatio
            val standardCellWidth = baseCellWidth

            // Calculate exact starting positions to center the grid
            val totalGridWidth = (narrowCellWidth * 2) + (standardCellWidth * 4) + (horizontalSpacing * 5)
            val startX = (totalWidth - totalGridWidth) / 2

            // Calculate column positions
            val column1StartX = startX
            val column2StartX = column1StartX + narrowCellWidth + horizontalSpacing
            val column3StartX = column2StartX + standardCellWidth + horizontalSpacing
            val column4StartX = column3StartX + standardCellWidth + horizontalSpacing
            val column5StartX = column4StartX + standardCellWidth + horizontalSpacing
            val column6StartX = column5StartX + standardCellWidth + horizontalSpacing

            // Calculate column 5 position first to determine A and B vertical positions
            val column5CellHeight = columnHeights[4] ?: baseCellHeight
            val column5TotalHeight = layout[4].size * column5CellHeight + (layout[4].size - 1) * verticalSpacing
            val column5StartY = (totalHeight - column5TotalHeight) / 2 - 35f

            // Calculate positions of M and R in column 5
            val mTop = column5StartY
            val rBottom = column5StartY + column5TotalHeight

            // Calculate column 1 A and B cell heights (A top aligns with M, B bottom aligns with R)
            val totalAvailableHeight = rBottom - mTop
            val column1CellHeight = (totalAvailableHeight - verticalSpacing) / 2f

            val aTop = mTop
            val aBottom = aTop + column1CellHeight
            val bTop = aBottom + verticalSpacing
            val bBottom = rBottom

            // Add blank horizontal rectangle above column 2
            val column2CellHeight = columnHeights[1] ?: baseCellHeight
            val column2TotalHeight = layout[1].size * column2CellHeight + (layout[1].size - 1) * verticalSpacing
            val column2StartY = (totalHeight - column2TotalHeight) / 2 - 35f

            val blankRectWidth = standardCellWidth * 2f + horizontalSpacing
            val blankRectHeight = baseCellHeight * 0.6f
            val blankRectX = column2StartX
            val blankRectY = column2StartY - blankRectHeight - verticalSpacing

            // Draw blank cell - updated colors to match calendar style
            drawRoundRect(
                color = Color(0xFFE3F2FD),
                topLeft = Offset(blankRectX, blankRectY),
                size = Size(blankRectWidth, blankRectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Draw border - updated color
            drawRoundRect(
                color = Color(0xFF90CAF9),
                topLeft = Offset(blankRectX, blankRectY),
                size = Size(blankRectWidth, blankRectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Add hint text in blank cell - updated color
            drawContext.canvas.nativeCanvas.apply {
                val text = "Printing & Counter"
                val paint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.parseColor("#1565C0") // Calendar blue color
                    isFakeBoldText = true
                }

                var textSize = blankRectHeight * 0.5f
                paint.textSize = textSize
                var textWidth = paint.measureText(text)

                while (textWidth > blankRectWidth * 0.95f && textSize > blankRectHeight * 0.2f) {
                    textSize -= 1f
                    paint.textSize = textSize
                    textWidth = paint.measureText(text)
                }

                val x = blankRectX + blankRectWidth / 2
                val y = blankRectY + blankRectHeight / 2 + textSize / 3
                drawText(text, x, y, paint)
            }

            // Draw column 1 A and B cells with updated colors
            listOf(
                Pair("A", aTop to column1CellHeight),
                Pair("B", bTop to column1CellHeight)
            ).forEach { (areaChar, position) ->
                val (top, height) = position
                val actualAreaIndex = areas.indexOf(areaChar).takeIf { it >= 0 } ?: return@forEach
                val area = areas[actualAreaIndex]
                val left = column1StartX

                // Updated cell colors to match calendar style
                val cellColor = if (area == highlightArea) {
                    Color(0xFFFFA500) // Highlight color remains orange
                } else {
                    Color(0xFF64B5F6) // Changed to calendar blue color
                }

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(left, top),
                    size = Size(narrowCellWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

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

            // Calculate cell R width
            val rWidth = column5StartX + standardCellWidth - column3StartX
            val rHeight = baseCellHeight * 0.7f

            // Define column positions for drawing - both column 1 and 6 use the same narrow width
            val columnPositions = listOf(
                column1StartX to narrowCellWidth, // Column 1: narrow width
                column2StartX to standardCellWidth, // Column 2: standard width
                column3StartX to standardCellWidth, // Column 3: standard width
                column4StartX to standardCellWidth, // Column 4: standard width
                column5StartX to standardCellWidth, // Column 5: standard width
                column6StartX to narrowCellWidth   // Column 6: same narrow width as column 1
            )

            // Draw other columns with updated colors
            for (colIndex in 1 until layout.size) {
                val columnAreas = layout[colIndex]
                val (columnStartX, baseColumnWidth) = columnPositions[colIndex]

                val cellHeight = if (colIndex == 5) column1CellHeight else columnHeights[colIndex] ?: baseCellHeight
                val columnTotalHeight = columnAreas.size * cellHeight + (columnAreas.size - 1) * verticalSpacing
                val startY = (totalHeight - columnTotalHeight) / 2 - 35f

                for ((rowIndex, areaChar) in columnAreas.withIndex()) {
                    val actualAreaIndex = areas.indexOf(areaChar).takeIf { it >= 0 } ?: continue
                    val area = areas[actualAreaIndex]

                    var top = startY + rowIndex * (cellHeight + verticalSpacing)
                    var left = columnStartX
                    var currentCellWidth = baseColumnWidth
                    var currentCellHeight = cellHeight

                    // Special handling for column 5 cell R
                    val isCellR = colIndex == 4 && rowIndex == columnAreas.size - 1 && area == "R"
                    if (isCellR) {
                        currentCellWidth = rWidth
                        currentCellHeight = rHeight
                        left = column3StartX
                        top = rBottom - currentCellHeight
                    }

                    // For column 6, use the full narrow width for both cells
                    val isColumn6 = colIndex == 5
                    val isBlankArea = isColumn6 && areaChar == "F"
                    val isColumn6CellA = isColumn6 && areaChar == "A"

                    // Check if this is cell D (in column 2, first row)
                    val isCellD = colIndex == 1 && rowIndex == 0 && areaChar == "D"

                    if (isBlankArea) {
                        // Draw blank faculty cell using full narrow width - updated colors
                        drawRoundRect(
                            color = Color(0xFFE3F2FD), // Calendar light blue
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF90CAF9), // Calendar border blue
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            save()
                            val text = "Faculty"
                            val paint = android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = android.graphics.Color.parseColor("#1565C0") // Calendar blue
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
                            restore()
                        }
                    } else {
                        // Updated cell colors to match calendar style
                        val cellColor = if (area == highlightArea) {
                            Color(0xFFFFA500) // Highlight color remains orange
                        } else {
                            Color(0xFF64B5F6) // Changed to calendar blue color
                        }

                        // Draw normal area using the column's base width
                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(left, top),
                            size = Size(currentCellWidth, currentCellHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = android.graphics.Color.WHITE
                                isFakeBoldText = true
                            }

                            // Calculate text size based on cell type
                            val textSize = when {
                                isColumn6CellA -> {
                                    // Column 6 cell A: match column 1 text size
                                    currentCellHeight * 0.2f
                                }
                                isCellR -> {
                                    // Cell R: match cell D text size (column 2 uses standard cell height)
                                    val column2CellHeight = columnHeights[1] ?: baseCellHeight
                                    column2CellHeight * 0.25f
                                }
                                else -> {
                                    // Other cells: use original text size
                                    currentCellHeight * 0.25f
                                }
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