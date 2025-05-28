package com.example.lendeezy.ui.screen

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lendeezy.NotificationReceiver
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.data.model.isCurrentlyBorrowed
import com.example.lendeezy.data.repository.UserRepository
import com.example.lendeezy.data.util.hasExactAlarmPermission
import com.example.lendeezy.data.util.requestExactAlarmPermission
import com.example.lendeezy.data.util.scheduleNotification
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.ProductListState
import com.example.lendeezy.ui.viewmodel.RecentlyViewedViewModel
import com.example.lendeezy.ui.viewmodel.ReservationState
import com.example.lendeezy.ui.viewmodel.ReserveProductsViewModel
import com.example.lendeezy.ui.viewmodel.SellerUserState
import com.example.lendeezy.ui.viewmodel.SellerViewModel
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel


/**
 * Shows detailed view of a specific clicked on product
 */
@Composable
fun ProductScreen(
    padding: PaddingValues,
    productId: String,
    productViewModel: GetProductsViewModel = viewModel(),
    reserveViewModel: ReserveProductsViewModel = viewModel(),
    recentlyViewedViewModel: RecentlyViewedViewModel = viewModel()
) {
    val productState by productViewModel.productState.collectAsState()
    val selectedProduct by productViewModel.selectedProduct.collectAsState()

    // fetch the product details for product ID
    LaunchedEffect(productId) {
        productViewModel.fetchProductById(productId)
    }

    // once selected product has loaded, save to local storage for recently viewed
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { product ->
            recentlyViewedViewModel.saveProductLocally(product)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {


        when (productState) {
            // if loading, show spinner
            is ProductListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // if error, show error message
            is ProductListState.Error -> {
                Text("Failed to load product")
            }
            // if success, show details
            is ProductListState.Success -> {
                selectedProduct?.let { product ->
                    ProductDetailContent(product = product)
                } ?: Text("Product not found.")
            }
            ProductListState.Idle -> {
                Text("No product data yet.")
            }
        }
    }

}

/**
 * Content of Product Screen with
 * 1. seller info
 * 2. Title and category
 * 3. Description, Terms, and Location
 * 4. Rent or borrow status
 */
@Composable
fun ProductDetailContent(
    product: Product,
    reserveViewModel: ReserveProductsViewModel = viewModel(),
    userRepository: UserRepository = UserRepository()
) {

    val reservationState by reserveViewModel.reservationState.collectAsState()
    val context = LocalContext.current

    // state changes for adding reservations
    LaunchedEffect(reservationState) {
        when (reservationState) {
            is ReservationState.Success -> {
                Toast.makeText(context, "Product reservation made.", Toast.LENGTH_SHORT).show()
                reserveViewModel.clearReservationState()
            }
            is ReservationState.Error -> {
                val error = (reservationState as ReservationState.Error).message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                reserveViewModel.clearReservationState()
            }
            else -> {}
        }
    }

    // get is borrowed and borrowed until when from reservations list
    val (isBorrowed, borrowedUntil) = product.isCurrentlyBorrowed()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // owner info, product name + category, intent to Mail
        ProductDetailTop(product)

        HorizontalDivider(thickness = 26.dp, color = Color(0xFFFF3F3F3))

        // description, terms, location, intent to Maps
        ProductDetailMiddle(product)

        HorizontalDivider(thickness = 26.dp, color = Color(0xFFFF3F3F3))

        // borrow status with rent button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BorrowStatus(
                isBorrowed = isBorrowed,
                borrowedUntil = borrowedUntil,
                onConfirm = { start, end ->
                    val userId = userRepository.getCurrentUserId()
                    if (userId != null) {
                        reserveViewModel.makeReservation(product, userId, start, end)
                    }
                }
            )
        }

    }
}

/**
 * Image, seller details, title and category
 */
@Composable
fun ProductDetailTop(product: Product) {
    AsyncImage(
        model = product.photoUrl,
        contentDescription = product.name,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentScale = ContentScale.Crop
    )

    // owner information
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        OwnerInfo(product, ownerId = product.ownerId)
    }

    HorizontalDivider(thickness = 1.dp, color = Color.Gray)

    // product name and category
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(product.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Category: ${product.category}", style = MaterialTheme.typography.bodyLarge)
    }

}

/**
 * Description, terms and location
 * Button to open Google Maps for location
 */
@Composable
fun ProductDetailMiddle(product: Product) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // description
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(product.description, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // terms
        Text("Terms", style = MaterialTheme.typography.titleMedium)
        Text(product.terms, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // button to view on Google Maps
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                Text("Location", style = MaterialTheme.typography.titleMedium)
                Text(product.location, style = MaterialTheme.typography.bodyLarge)
            }

            // button which on click, opens Maps for that location
            Button(
                onClick = {
                    // create URI
                    val mapsIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(product.location)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapsIntentUri)
                    // use the Google Maps app
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text("View")
            }
        }
    }
}



/**
 * Button for setting a reminder and allows to choose date and time
 */
@Composable
fun SetReminderButton(onSchedule: (Calendar) -> Unit) {
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    Button(onClick = {
        // check if app has permission for exact alarms
        if (hasExactAlarmPermission(context)) {
            // if yes, show date and time selectors
            val now = Calendar.getInstance()

            // open date picker
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val calendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth) // set chosen date on calendar
                    }

                    // after date picker, show time picker
                    android.app.TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            // set chosen hour, minite, second at zero
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            selectedDate = calendar
                            onSchedule(calendar)
                        },
                        // default on the time picker is current time
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                    ).show()

                },
                // default date is current date on calendar
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        } else {
            // if no permission granted, show message and request permission
            requestExactAlarmPermission(context)
            Toast.makeText(
                context,
                "Grant exact alarm permission to set a notification",
                Toast.LENGTH_LONG
            ).show()
        }
    }) {
        Text("Set Notification Reminder")
    }
}



/**
 * If rented, show message it is rented, else show Rent button + renting UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowStatus(
    isBorrowed: Boolean,
    borrowedUntil: String?,
    onConfirm: (startDate: String, endDate: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val formatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }

    // boolean to keep track of whether to show date selectors
    var showDatePickers by remember { mutableStateOf(false) }
    // start and end date
    var startDate by remember { mutableStateOf<String?>(null) }
    var endDate by remember { mutableStateOf<String?>(null) }

    var showReminderButton by remember { mutableStateOf(false) }


    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // show Rent button
        if (!showDatePickers) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // if borrowed so until when, else say available
                if (isBorrowed) {
                    Text("Borrowed until: $borrowedUntil")

                } else {
                    Text("Available for rent")
                }

                Button(
                    onClick = { showDatePickers = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Rent")
                }
            }
            // on click of rent button, show select start and end dates, cancel and confirm buttons
        } else {
            // row with date selectors
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start date
                Button(
                    onClick = {
                        // on click, show date picker
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val date = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                startDate = formatter.format(date.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    // text shows selected start date or message
                    Text(startDate ?: "Start Date")
                }
                // End date
                Button(
                    onClick = {
                        // on click, opens date selector
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val date = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                endDate = formatter.format(date.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    // button shows selected end date or message
                    Text(endDate ?: "End Date")
                }
            }

            // confirm and cancel buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // confirm
                Button(
                    onClick = {
                        if (startDate != null && endDate != null) {
                            onConfirm(startDate!!, endDate!!)
                            showDatePickers = false
                            startDate = null
                            endDate = null
                            showReminderButton = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    // confirm button only enabled if start and end date selected
                    enabled = startDate != null && endDate != null
                ) {
                    Text("Confirm")
                }

                // cancel button - hides date picker section and resets selected start and end date
                Button(
                    onClick = {
                        showDatePickers = false
                        startDate = null
                        endDate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Cancel")
                }
            }
        }

        if (showReminderButton) {
            SetReminderButton { calendar ->
                scheduleNotification(context, calendar, "Lendeezy Reminder", "Don't forget your rental!")
                Toast.makeText(context, "Reminder set!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}




/**
 * Show seller information
 */
@Composable
fun OwnerInfo(product: Product ,ownerId: String, sellerViewModel: SellerViewModel = viewModel()) {
    val sellerState by sellerViewModel.sellerState.collectAsState()
    val context = LocalContext.current

    // fetch seller details
    LaunchedEffect(ownerId) {
        sellerViewModel.fetchUser(ownerId)
    }

    when (sellerState) {
        // when loading, show spinner
        is SellerUserState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        is SellerUserState.Error -> {
            Text("Could not load owner", color = Color.Red)
        }

        // if success show image and name of seller
        is SellerUserState.Success -> {
            val owner = (sellerState as SellerUserState.Success).user

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // image and name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = owner.photoUrl,
                        contentDescription = owner.name,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(owner.name, style = MaterialTheme.typography.bodyLarge)
                }

                // button which opens Mail app to email seller
                Button(
                    onClick = {
                        // intent to open Mail
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${owner.email}")
                            putExtra(Intent.EXTRA_SUBJECT, "More info about your product ${product.name} on Lendeezy")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Email")
                }
            }
        }

        SellerUserState.Idle -> {
            // nothing
        }
    }
}
