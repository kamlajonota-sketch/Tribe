package com.example.core.localization

/**
 * Strict English authentication constants.
 * MANDATORY: Section 7 requires the entire authentication interface to ALWAYS remain in English,
 * even when the application language is Bangla.
 */
object StrictAuthStrings {
    const val WELCOME_TITLE = "Welcome to TRIBE"
    const val WELCOME_SUBTITLE = "Your next adventure starts here."
    const val CONTINUE_WITH_GOOGLE = "Continue with Google"
    const val OR_CONTINUE_WITH_EMAIL = "Or continue with email"
    const val EMAIL_ADDRESS = "Email Address"
    const val PASSWORD = "Password"
    const val FORGOT_PASSWORD = "Forgot Password?"
    const val SIGN_IN = "Sign In"
    const val CREATE_ACCOUNT = "Create Account"
    const val SIGN_UP = "Sign Up"
    const val DONT_HAVE_ACCOUNT = "Don't have an account?"
    const val ALREADY_HAVE_ACCOUNT = "Already have an account?"
}

/**
 * Typed localized strings for TRIBE.
 */
data class TribeStrings(
    // Bottom Navigation
    val navHome: String,
    val navMyPlans: String,
    val navExplore: String,
    val navProfile: String,
    val navCreatePlan: String,

    // Home Screen
    val greetingTraveler: String,
    val greetingSubtitle: String,
    val searchDestinationsHint: String,
    val quickAccess: String,
    val popularDestinations: String,
    val featuredDestinations: String,
    val seeAll: String,
    val planNextTrip: String,

    // My Plans Screen
    val myPlansTitle: String,
    val filterAll: String,
    val filterUpcoming: String,
    val filterPlanned: String,
    val filterCompleted: String,
    val filterDrafts: String,
    val createNewPlan: String,
    val membersCount: String,
    val noPlansYet: String,
    val noPlansSubtitle: String,

    // Create New Plan
    val createPlanTitle: String,
    val tripTitleLabel: String,
    val destinationLabel: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val maxMembersLabel: String,
    val meetingPointLabel: String,
    val descriptionLabel: String,
    val visibilityLabel: String,
    val visibilityPublic: String,
    val visibilityPrivate: String,
    val savePlanAction: String,

    // Explore Screen
    val exploreTitle: String,
    val categoryHill: String,
    val categoryBeach: String,
    val categoryForest: String,
    val categoryHeritage: String,
    val paradiseInBangladesh: String,

    // Trip Details Screen
    val tripDetailsTitle: String,
    val tabOverview: String,
    val tabItinerary: String,
    val tabExpenses: String,
    val tabMembers: String,
    val tabChecklist: String,
    val tripOverviewHeader: String,
    val viewItinerary: String,
    val editTrip: String,
    val shareTrip: String,

    // Expense Management
    val expensesTitle: String,
    val totalExpenses: String,
    val perPersonShare: String,
    val addExpense: String,
    val expenseCategoryFood: String,
    val expenseCategoryTransport: String,
    val expenseCategoryStay: String,
    val expenseCategoryActivity: String,
    val expenseCategoryOther: String,

    // Saved Trips
    val savedTripsTitle: String,
    val noSavedTrips: String,

    // Profile & Settings
    val profileTitle: String,
    val tripsStat: String,
    val placesStat: String,
    val distanceStat: String,
    val myActivity: String,
    val languageLabel: String,
    val themeLabel: String,
    val notificationSettings: String,
    val accountSecurity: String,
    val syncData: String,
    val privacySecurity: String,
    val helpSupport: String,
    val aboutTribe: String,
    val signOut: String,
    val settingsTitle: String,
    val appearance: String,
    val lightMode: String,
    val darkMode: String,
    val systemMode: String,

    // Common Actions
    val cancel: String,
    val confirm: String,
    val save: String,
    val back: String,
    val retry: String,
    val offlineModeBadge: String
)

val EnglishStrings = TribeStrings(
    navHome = "Home",
    navMyPlans = "My Plans",
    navExplore = "Explore",
    navProfile = "Profile",
    navCreatePlan = "Create Plan",

    greetingTraveler = "Hello, Traveler!",
    greetingSubtitle = "Where do you want to go today?",
    searchDestinationsHint = "Search destinations...",
    quickAccess = "Quick Access",
    popularDestinations = "Popular Destinations",
    featuredDestinations = "Featured Destinations",
    seeAll = "See All >",
    planNextTrip = "Plan your next trip",

    myPlansTitle = "My Plans",
    filterAll = "All",
    filterUpcoming = "Upcoming",
    filterPlanned = "Planned",
    filterCompleted = "Completed",
    filterDrafts = "Drafts",
    createNewPlan = "+ Create New Plan",
    membersCount = "%d Members",
    noPlansYet = "No travel plans yet",
    noPlansSubtitle = "Start planning your next adventure with friends!",

    createPlanTitle = "Create New Plan",
    tripTitleLabel = "Trip Title",
    destinationLabel = "Destination",
    startDateLabel = "Start Date",
    endDateLabel = "End Date",
    maxMembersLabel = "Maximum Members",
    meetingPointLabel = "Meeting Point",
    descriptionLabel = "Trip Description",
    visibilityLabel = "Trip Visibility",
    visibilityPublic = "Public (Discoverable)",
    visibilityPrivate = "Private (Invite Only)",
    savePlanAction = "Save Travel Plan",

    exploreTitle = "Explore",
    categoryHill = "Hill",
    categoryBeach = "Beach",
    categoryForest = "Forest",
    categoryHeritage = "Heritage",
    paradiseInBangladesh = "Paradise in Bangladesh",

    tripDetailsTitle = "Trip Details",
    tabOverview = "Overview",
    tabItinerary = "Itinerary",
    tabExpenses = "Expenses",
    tabMembers = "Members",
    tabChecklist = "Checklist",
    tripOverviewHeader = "Trip Overview",
    viewItinerary = "View Itinerary",
    editTrip = "Edit Trip",
    shareTrip = "Share Trip",

    expensesTitle = "Expense Tracker",
    totalExpenses = "Total Expenses",
    perPersonShare = "Per-Person Share",
    addExpense = "+ Add Expense",
    expenseCategoryFood = "Food & Dining",
    expenseCategoryTransport = "Transportation",
    expenseCategoryStay = "Accommodation",
    expenseCategoryActivity = "Activities",
    expenseCategoryOther = "Other",

    savedTripsTitle = "Saved Trips",
    noSavedTrips = "No saved trips yet",

    profileTitle = "Profile",
    tripsStat = "Trips",
    placesStat = "Districts",
    distanceStat = "Approx. km",
    myActivity = "My Activity",
    languageLabel = "Language",
    themeLabel = "Theme",
    notificationSettings = "Notification Settings",
    accountSecurity = "Account Security",
    syncData = "Sync Data",
    privacySecurity = "Privacy & Security",
    helpSupport = "Help & Support",
    aboutTribe = "About TRIBE",
    signOut = "Sign Out",
    settingsTitle = "Settings",
    appearance = "Appearance",
    lightMode = "Light Mode",
    darkMode = "Dark Mode",
    systemMode = "System Default",

    cancel = "Cancel",
    confirm = "Confirm",
    save = "Save",
    back = "Back",
    retry = "Retry",
    offlineModeBadge = "Offline Ready"
)

val BanglaStrings = TribeStrings(
    navHome = "হোম",
    navMyPlans = "আমার পরিকল্পনা",
    navExplore = "ঘুরে দেখুন",
    navProfile = "প্রোফাইল",
    navCreatePlan = "নতুন পরিকল্পনা",

    greetingTraveler = "হ্যালো, পরিব্রাজক!",
    greetingSubtitle = "আজ আপনি কোথায় যেতে চান?",
    searchDestinationsHint = "গন্তব্য খুঁজুন...",
    quickAccess = "দ্রুত অ্যাক্সেস",
    popularDestinations = "জনপ্রিয় গন্তব্যসমূহ",
    featuredDestinations = "আকর্ষণীয় গন্তব্য",
    seeAll = "সব দেখুন >",
    planNextTrip = "আপনার পরবর্তী ভ্রমণের পরিকল্পনা করুন",

    myPlansTitle = "আমার পরিকল্পনা",
    filterAll = "সব",
    filterUpcoming = "আসন্ন",
    filterPlanned = "পরিকল্পিত",
    filterCompleted = "সম্পন্ন",
    filterDrafts = "খসড়া",
    createNewPlan = "+ নতুন পরিকল্পনা তৈরি করুন",
    membersCount = "%d জন সদস্য",
    noPlansYet = "কোনো ভ্রমণ পরিকল্পনা নেই",
    noPlansSubtitle = "বন্ধুদের সাথে আপনার পরবর্তী রোমাঞ্চকর ভ্রমণের পরিকল্পনা শুরু করুন!",

    createPlanTitle = "নতুন পরিকল্পনা তৈরি করুন",
    tripTitleLabel = "ভ্রমণের শিরোনাম",
    destinationLabel = "গন্তব্য",
    startDateLabel = "শুরুর তারিখ",
    endDateLabel = "শেষের তারিখ",
    maxMembersLabel = "সর্বোচ্চ সদস্য",
    meetingPointLabel = "মিলিত হওয়ার স্থান",
    descriptionLabel = "ভ্রমণের বিবরণ",
    visibilityLabel = "ভ্রমণের দৃশ্যমানতা",
    visibilityPublic = "পাবলিক (উন্মুক্ত)",
    visibilityPrivate = "ব্যক্তিগত (শুধুমাত্র আমন্ত্রিত)",
    savePlanAction = "ভ্রমণ পরিকল্পনা সংরক্ষণ করুন",

    exploreTitle = "ঘুরে দেখুন",
    categoryHill = "পাহাড়",
    categoryBeach = "সমুদ্র সৈকত",
    categoryForest = "বন",
    categoryHeritage = "ঐতিহ্যবাহী স্থান",
    paradiseInBangladesh = "বাংলাদেশের স্বর্গ",

    tripDetailsTitle = "ভ্রমণের বিস্তারিত",
    tabOverview = "সারসংক্ষেপ",
    tabItinerary = "ভ্রমণসূচি",
    tabExpenses = "খরচ",
    tabMembers = "সদস্যবৃন্দ",
    tabChecklist = "চেকলিস্ট",
    tripOverviewHeader = "ভ্রমণ বিবরণ",
    viewItinerary = "ভ্রমণসূচি দেখুন",
    editTrip = "সম্পাদনা করুন",
    shareTrip = "শেয়ার করুন",

    expensesTitle = "খরচের হিসাব",
    totalExpenses = "মোট খরচ",
    perPersonShare = "জনপ্রতি খরচ",
    addExpense = "+ খরচ যোগ করুন",
    expenseCategoryFood = "খাবার",
    expenseCategoryTransport = "যাতায়াত",
    expenseCategoryStay = "থাকা/হোটেল",
    expenseCategoryActivity = "ঘোরাঘুরি ও কার্যক্রম",
    expenseCategoryOther = "অন্যান্য",

    savedTripsTitle = "সংরক্ষিত ভ্রমণসমূহ",
    noSavedTrips = "কোনো সংরক্ষিত ভ্রমণ নেই",

    profileTitle = "প্রোফাইল",
    tripsStat = "ভ্রমণ",
    placesStat = "জেলা",
    distanceStat = "দূরত্ব (কিমি)",
    myActivity = "আমার কার্যক্রম",
    languageLabel = "ভাষা",
    themeLabel = "থিম",
    notificationSettings = "বিজ্ঞপ্তি সেটিংস",
    accountSecurity = "অ্যাকাউন্ট নিরাপত্তা",
    syncData = "তথ্য সিঙ্ক",
    privacySecurity = "গোপনীয়তা ও নিরাপত্তা",
    helpSupport = "সহায়তা",
    aboutTribe = "TRIBE সম্পর্কে",
    signOut = "সাইন আউট",
    settingsTitle = "সেটিংস",
    appearance = "উপস্থিতি",
    lightMode = "লাইট মোড",
    darkMode = "ডার্ক মোড",
    systemMode = "সিস্টেম ডিফল্ট",

    cancel = "বাতিল",
    confirm = "নিশ্চিত করুন",
    save = "সংরক্ষণ",
    back = "পেছনে",
    retry = "পুনরায় চেষ্টা",
    offlineModeBadge = "অফলাইন প্রস্তুত"
)
