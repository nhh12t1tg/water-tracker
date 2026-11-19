package com.watertracker.app.ui.i18n

/**
 * Multi-Language Localization System for Water Tracker.
 * Default language: US English ("en").
 * Supported languages: US English ("en"), Vietnamese ("vi"), Spanish ("es"), Japanese ("ja"), German ("de").
 */
enum class SupportedLanguage(val code: String, val displayName: String, val flag: String) {
    EN("en", "English (US)", "🇺🇸"),
    VI("vi", "Tiếng Việt", "🇻🇳"),
    ES("es", "Español", "🇪🇸"),
    JA("ja", "日本語", "🇯🇵"),
    DE("de", "Deutsch", "🇩🇪");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: EN
        }
    }
}

data class Strings(
    // App Bar & Common
    val appName: String,
    val streakDays: (Int) -> String,
    val historyTitle: String,
    val settingsTitle: String,
    val back: String,
    val close: String,
    val cancel: String,
    val confirm: String,
    val manage: String,

    // Home Screen & Motivation
    val motivationDefault: String,
    val motivationGoalReached: String,
    val progressFormat: (Int, Int) -> String,

    // Quick Add Bar
    val quickAdd150: String,
    val quickAdd250: String,
    val quickAdd500: String,
    val quickAddCustom: String,
    val themesButton: String,

    // Beverages
    val beverageWater: String,
    val beverageTea: String,
    val beverageCoffee: String,
    val beverageJuice: String,
    val beverageMilk: String,
    val beverageElectrolytes: String,
    val bhiFeedbackFormat: (String, Int, String, Int, Int) -> String,

    // Custom Water Dialog
    val customDialogTitle: String,
    val customDialogSelected: (String, Int) -> String,
    val customDialogInputLabel: String,
    val customDialogNetHydration: (Int) -> String,

    // Themes Dialog
    val themeDialogTitle: String,
    val themeDialogSubtitle: String,
    val themeCyan: String,
    val themeMint: String,
    val themeSunset: String,
    val themeDeepOcean: String,

    // Goal Completed Dialog
    val goalDialogTitle: String,
    val goalDialogDesc: (Int) -> String,
    val goalDialogCta: String,

    // Onboarding
    val onboardingWelcome: String,
    val onboardingSubtitle: String,
    val genderTitle: String,
    val genderMale: String,
    val genderFemale: String,
    val genderMaleRate: String,
    val genderFemaleRate: String,
    val weightTitle: (Int) -> String,
    val activityTitle: String,
    val activitySedentary: String,
    val activityModerate: String,
    val activityActive: String,
    val recommendedGoalTitle: String,
    val recommendedGoalFormat: (Int) -> String,
    val formulaNote: (Int, Int, Int) -> String,
    val startTrackingCta: String,

    // Settings
    val settingsLanguageSection: String,
    val settingsLanguageLabel: String,
    val settingsRemindersSection: String,
    val settingsReminderFrequency: String,
    val settingsReminderFreqFormat: (Int) -> String,
    val settingsDndSection: String,
    val settingsDndTitle: String,
    val settingsDndSubtitle: (String, String) -> String,
    val settingsWakeTime: String,
    val settingsBedTime: String,
    val settingsPrivacySection: String,
    val settingsPrivacyTitle: String,
    val settingsPrivacyDesc: String,

    // History & Badges
    val historyChartTitle: String,
    val streakShieldTitle: (Int) -> String,
    val streakShieldDesc: String,
    val addShieldCta: String,
    val badgesTitle: (Int) -> String,
    val badgeStarter: String,
    val badgeStarterDays: String,
    val badgeWeek: String,
    val badgeWeekDays: String,
    val badgeGold: String,
    val badgeGoldDays: String,
    val badgeMaster: String,
    val badgeMasterDays: String,
    val daysOfWeek: List<String>
)

val EnglishStrings = Strings(
    appName = "Water Tracker",
    streakDays = { count -> if (count == 1) "1 day" else "$count days" },
    historyTitle = "History & Badges",
    settingsTitle = "Settings",
    back = "Back",
    close = "Close",
    cancel = "Cancel",
    confirm = "Save",
    manage = "Manage",

    motivationDefault = "Take small, regular sips for optimal cellular hydration",
    motivationGoalReached = "Outstanding! You reached today's hydration target 🎉",
    progressFormat = { current, target -> "$current / $target ml" },

    quickAdd150 = "+150ml",
    quickAdd250 = "+250ml",
    quickAdd500 = "+500ml",
    quickAddCustom = "+ Other",
    themesButton = "Themes",

    beverageWater = "Water",
    beverageTea = "Tea",
    beverageCoffee = "Coffee",
    beverageJuice = "Juice",
    beverageMilk = "Milk",
    beverageElectrolytes = "Electrolytes",
    bhiFeedbackFormat = { icon, raw, name, effective, factor ->
        "$icon +${raw}ml $name ➔ Net hydration: +${effective}ml ($factor% BHI)"
    },

    customDialogTitle = "Log Custom Drink",
    customDialogSelected = { name, factor -> "Selected: $name (BHI index $factor%)" },
    customDialogInputLabel = "Volume in ml consumed",
    customDialogNetHydration = { net -> "➔ Net body hydration: +$net ml" },

    themeDialogTitle = "Unlock Wave Themes",
    themeDialogSubtitle = "Watch a short video (Rewarded Ad) to unlock your favorite wave style:",
    themeCyan = "Cyan Classic (Default)",
    themeMint = "Mint Fresh",
    themeSunset = "Sunset Gold",
    themeDeepOcean = "Deep Ocean",

    goalDialogTitle = "Daily Goal Achieved!",
    goalDialogDesc = { goal -> "You reached $goal ml of water today. Your body and mind thank you!" },
    goalDialogCta = "Awesome, continue!",

    onboardingWelcome = "Welcome to\nWater Tracker",
    onboardingSubtitle = "Personalize your daily hydration target based on EFSA clinical physiology standards",
    genderTitle = "1. YOUR BIOLOGICAL SEX",
    genderMale = "Male",
    genderFemale = "Female",
    genderMaleRate = "35 ml/kg",
    genderFemaleRate = "31 ml/kg",
    weightTitle = { kg -> "2. CURRENT WEIGHT: $kg KG" },
    activityTitle = "3. PHYSICAL ACTIVITY LEVEL",
    activitySedentary = "Sedentary (< 30 min/day)",
    activityModerate = "Moderate (30 - 60 min/day)",
    activityActive = "Active (> 60 min/day)",
    recommendedGoalTitle = "RECOMMENDED DAILY GOAL",
    recommendedGoalFormat = { goal -> "$goal ml/day" },
    formulaNote = { weight, rate, offset -> "Formula: ($weight kg × $rate) + $offset ml (EFSA standard)" },
    startTrackingCta = "Start Hydrating Now 🚀",

    settingsLanguageSection = "LANGUAGE & LOCALIZATION",
    settingsLanguageLabel = "App Display Language",
    settingsRemindersSection = "DRINK REMINDERS",
    settingsReminderFrequency = "Reminder Frequency",
    settingsReminderFreqFormat = { min -> "Every $min minutes" },
    settingsDndSection = "DO NOT DISTURB (DND)",
    settingsDndTitle = "Nighttime Silent Mode (DND)",
    settingsDndSubtitle = { wake, sleep -> "Silence notifications while sleeping ($sleep - $wake)" },
    settingsWakeTime = "Wake-up time",
    settingsBedTime = "Bedtime",
    settingsPrivacySection = "PRIVACY & ADMOB",
    settingsPrivacyTitle = "Privacy Preferences (GDPR / UMP)",
    settingsPrivacyDesc = "Manage ad personalization and cookie consent under international privacy laws",

    historyChartTitle = "Last 7 Days Intake",
    streakShieldTitle = { freezes -> "Streak Protection ($freezes ready)" },
    streakShieldDesc = "Watch 1 video to earn 1 Freeze Shield if you ever miss a day",
    addShieldCta = "+1 Shield",
    badgesTitle = { streak -> "Streak Badges ($streak Days)" },
    badgeStarter = "Starter",
    badgeStarterDays = "3 days",
    badgeWeek = "1-Week Habit",
    badgeWeekDays = "7 days",
    badgeGold = "Gold Habit",
    badgeGoldDays = "14 days",
    badgeMaster = "Master Habit",
    badgeMasterDays = "30 days",
    daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
)

val VietnameseStrings = Strings(
    appName = "Water Tracker",
    streakDays = { count -> "$count ngày" },
    historyTitle = "Lịch Sử & Huy Hiệu",
    settingsTitle = "Cài Đặt",
    back = "Quay lại",
    close = "Đóng",
    cancel = "Hủy",
    confirm = "Ghi nhận",
    manage = "Quản lý",

    motivationDefault = "Hãy uống từng ngụm nhỏ để cơ thể hấp thụ tốt nhất",
    motivationGoalReached = "Tuyệt vời! Bạn đã đủ nước hôm nay 🎉",
    progressFormat = { current, target -> "$current / $target ml" },

    quickAdd150 = "+150ml",
    quickAdd250 = "+250ml",
    quickAdd500 = "+500ml",
    quickAddCustom = "+ Khác",
    themesButton = "Màu Nước",

    beverageWater = "Nước lọc",
    beverageTea = "Trà",
    beverageCoffee = "Cà phê",
    beverageJuice = "Nước ép",
    beverageMilk = "Sữa tươi",
    beverageElectrolytes = "Điện giải",
    bhiFeedbackFormat = { icon, raw, name, effective, factor ->
        "$icon +${raw}ml $name ➔ Hấp thu thực tế: +${effective}ml ($factor% BHI)"
    },

    customDialogTitle = "Ghi Lượng Nước Tùy Chỉnh",
    customDialogSelected = { name, factor -> "Đang chọn: $name (Hệ số BHI $factor%)" },
    customDialogInputLabel = "Số ml nước đã uống",
    customDialogNetHydration = { net -> "➔ Nước thực nạp vào cơ thể: +$net ml" },

    themeDialogTitle = "Mở Khóa Màu Nước Mới",
    themeDialogSubtitle = "Xem 1 video ngắn (Rewarded Ad) để mở khóa màu nước yêu thích:",
    themeCyan = "Cyan Tươi Mát (Mặc định)",
    themeMint = "Mint Bạc Hà",
    themeSunset = "Sunset Hoàng Hôn",
    themeDeepOcean = "Deep Ocean",

    goalDialogTitle = "Hoàn Thành Mục Tiêu!",
    goalDialogDesc = { goal -> "Bạn đã uống đủ $goal ml nước hôm nay. Cơ thể của bạn đang cảm ơn bạn đấy!" },
    goalDialogCta = "Tuyệt vời, tiếp tục!",

    onboardingWelcome = "Chào mừng đến với\nWater Tracker",
    onboardingSubtitle = "Hãy cá nhân hóa lượng nước mỗi ngày dựa trên chuẩn sinh lý học",
    genderTitle = "1. GIỚI TÍNH CỦA BẠN",
    genderMale = "Nam",
    genderFemale = "Nữ",
    genderMaleRate = "35 ml/kg",
    genderFemaleRate = "31 ml/kg",
    weightTitle = { kg -> "2. CÂN NẶNG HIỆN TẠI: $kg KG" },
    activityTitle = "3. MỨC ĐỘ HOẠT ĐỘNG THỂ CHẤT",
    activitySedentary = "Ít vận động (Dưới 30 phút/ngày)",
    activityModerate = "Vận động vừa (30 - 60 phút/ngày)",
    activityActive = "Vận động nhiều (Trên 60 phút/ngày)",
    recommendedGoalTitle = "MỤC TIÊU NƯỚC KHUYẾN NGHỊ",
    recommendedGoalFormat = { goal -> "$goal ml/ngày" },
    formulaNote = { weight, rate, offset -> "Công thức: ($weight kg × $rate) + $offset ml theo chuẩn EFSA" },
    startTrackingCta = "Bắt Đầu Uống Nước Ngay 🚀",

    settingsLanguageSection = "NGÔN NGỮ & HIỂN THỊ",
    settingsLanguageLabel = "Ngôn ngữ ứng dụng",
    settingsRemindersSection = "NHẮC NHỞ UỐNG NƯỚC",
    settingsReminderFrequency = "Tần suất nhắc nhở",
    settingsReminderFreqFormat = { min -> "$min phút một lần" },
    settingsDndSection = "CHẾ ĐỘ KHÔNG LÀM PHIỀN (DND)",
    settingsDndTitle = "Tắt chuông ban đêm (DND)",
    settingsDndSubtitle = { wake, sleep -> "Không gửi thông báo khi đang ngủ ($sleep - $wake)" },
    settingsWakeTime = "Giờ thức dậy",
    settingsBedTime = "Giờ đi ngủ",
    settingsPrivacySection = "QUYỀN RIÊNG TƯ & ADMOB",
    settingsPrivacyTitle = "Tùy chọn quyền riêng tư (GDPR / UMP)",
    settingsPrivacyDesc = "Quản lý dữ liệu đồng ý quảng cáo cá nhân hóa theo luật Châu Âu",

    historyChartTitle = "Biểu Đồ 7 Ngày Qua",
    streakShieldTitle = { freezes -> "Bảo Vệ Chuỗi Streak ($freezes sẵn có)" },
    streakShieldDesc = "Xem 1 video nhận 1 Khiên Đóng Băng khi lỡ quên uống",
    addShieldCta = "+1 Khiên",
    badgesTitle = { streak -> "Huy Hiệu Chuỗi ($streak Ngày)" },
    badgeStarter = "Khởi Đầu",
    badgeStarterDays = "3 ngày",
    badgeWeek = "Kiên Trì 1 Tuần",
    badgeWeekDays = "7 ngày",
    badgeGold = "Thói Quen Vàng",
    badgeGoldDays = "14 ngày",
    badgeMaster = "Bậc Thầy",
    badgeMasterDays = "30 ngày",
    daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
)

val SpanishStrings = Strings(
    appName = "Water Tracker",
    streakDays = { count -> if (count == 1) "1 día" else "$count días" },
    historyTitle = "Historial y Medallas",
    settingsTitle = "Ajustes",
    back = "Atrás",
    close = "Cerrar",
    cancel = "Cancelar",
    confirm = "Guardar",
    manage = "Gestionar",

    motivationDefault = "Bebe a pequeños sorbos para una hidratación celular óptima",
    motivationGoalReached = "¡Excelente! Has alcanzado tu objetivo de hoy 🎉",
    progressFormat = { current, target -> "$current / $target ml" },

    quickAdd150 = "+150ml",
    quickAdd250 = "+250ml",
    quickAdd500 = "+500ml",
    quickAddCustom = "+ Otro",
    themesButton = "Temas",

    beverageWater = "Agua",
    beverageTea = "Té",
    beverageCoffee = "Café",
    beverageJuice = "Zumo",
    beverageMilk = "Leche",
    beverageElectrolytes = "Electrolitos",
    bhiFeedbackFormat = { icon, raw, name, effective, factor ->
        "$icon +${raw}ml $name ➔ Hidratación neta: +${effective}ml ($factor% BHI)"
    },

    customDialogTitle = "Registrar Bebida Personalizada",
    customDialogSelected = { name, factor -> "Seleccionado: $name (Índice BHI $factor%)" },
    customDialogInputLabel = "Volumen en ml consumido",
    customDialogNetHydration = { net -> "➔ Hidratación neta corporal: +$net ml" },

    themeDialogTitle = "Desbloquear Temas de Olas",
    themeDialogSubtitle = "Mira un video corto (Anuncio Bonificado) para desbloquear:",
    themeCyan = "Cian Clásico (Predeterminado)",
    themeMint = "Menta Fresca",
    themeSunset = "Puesta de Sol",
    themeDeepOcean = "Océano Profundo",

    goalDialogTitle = "¡Meta Diaria Cumplida!",
    goalDialogDesc = { goal -> "Has tomado $goal ml de agua hoy. ¡Tu cuerpo te lo agradece!" },
    goalDialogCta = "¡Genial, continuar!",

    onboardingWelcome = "Bienvenido a\nWater Tracker",
    onboardingSubtitle = "Personaliza tu objetivo diario de hidratación según las normas de la EFSA",
    genderTitle = "1. TU SEXO BIOLÓGICO",
    genderMale = "Hombre",
    genderFemale = "Mujer",
    genderMaleRate = "35 ml/kg",
    genderFemaleRate = "31 ml/kg",
    weightTitle = { kg -> "2. PESO ACTUAL: $kg KG" },
    activityTitle = "3. NIVEL DE ACTIVIDAD FÍSICA",
    activitySedentary = "Sedentario (< 30 min/día)",
    activityModerate = "Moderado (30 - 60 min/día)",
    activityActive = "Activo (> 60 min/día)",
    recommendedGoalTitle = "OBJETIVO DIARIO RECOMENDADO",
    recommendedGoalFormat = { goal -> "$goal ml/día" },
    formulaNote = { weight, rate, offset -> "Fórmula: ($weight kg × $rate) + $offset ml (Norma EFSA)" },
    startTrackingCta = "Comenzar a Hidratarse 🚀",

    settingsLanguageSection = "IDIOMA Y LOCALIZACIÓN",
    settingsLanguageLabel = "Idioma de la aplicación",
    settingsRemindersSection = "RECORDATORIOS DE AGUA",
    settingsReminderFrequency = "Frecuencia de recordatorio",
    settingsReminderFreqFormat = { min -> "Cada $min minutos" },
    settingsDndSection = "NO MOLESTAR (DND)",
    settingsDndTitle = "Modo Silencio Nocturno",
    settingsDndSubtitle = { wake, sleep -> "Silenciar notificaciones al dormir ($sleep - $wake)" },
    settingsWakeTime = "Hora de despertar",
    settingsBedTime = "Hora de acostarse",
    settingsPrivacySection = "PRIVACIDAD Y ADMOB",
    settingsPrivacyTitle = "Preferencias de privacidad (RGPD / UMP)",
    settingsPrivacyDesc = "Administre el consentimiento de anuncios según la normativa de la UE",

    historyChartTitle = "Consumo de los Últimos 7 Días",
    streakShieldTitle = { freezes -> "Protección de Racha ($freezes disponibles)" },
    streakShieldDesc = "Mira 1 video para obtener 1 Escudo de Congelación",
    addShieldCta = "+1 Escudo",
    badgesTitle = { streak -> "Medallas de Racha ($streak Días)" },
    badgeStarter = "Principiante",
    badgeStarterDays = "3 días",
    badgeWeek = "1 Semana",
    badgeWeekDays = "7 días",
    badgeGold = "Hábito de Oro",
    badgeGoldDays = "14 días",
    badgeMaster = "Maestro",
    badgeMasterDays = "30 días",
    daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
)

val JapaneseStrings = Strings(
    appName = "Water Tracker",
    streakDays = { count -> "$count 日連続" },
    historyTitle = "履歴 & バッジ",
    settingsTitle = "設定",
    back = "戻る",
    close = "閉じる",
    cancel = "キャンセル",
    confirm = "記録する",
    manage = "管理",

    motivationDefault = "細胞が効果的に水分を吸収できるよう、少しずつ飲みましょう",
    motivationGoalReached = "素晴らしい！今日の水分補給目標を達成しました 🎉",
    progressFormat = { current, target -> "$current / $target ml" },

    quickAdd150 = "+150ml",
    quickAdd250 = "+250ml",
    quickAdd500 = "+500ml",
    quickAddCustom = "+ その他",
    themesButton = "テーマ",

    beverageWater = "水",
    beverageTea = "お茶",
    beverageCoffee = "コーヒー",
    beverageJuice = "ジュース",
    beverageMilk = "牛乳",
    beverageElectrolytes = "電解質飲料",
    bhiFeedbackFormat = { icon, raw, name, effective, factor ->
        "$icon +${raw}ml $name ➔ 実質水分補給: +${effective}ml ($factor% BHI)"
    },

    customDialogTitle = "カスタム水分量を記録",
    customDialogSelected = { name, factor -> "選択中: $name (BHI指数 $factor%)" },
    customDialogInputLabel = "飲んだ量 (ml)",
    customDialogNetHydration = { net -> "➔ 体内への実質吸収量: +$net ml" },

    themeDialogTitle = "新しい波テーマのロック解除",
    themeDialogSubtitle = "短い動画（リワード広告）を視聴してお気に入りの波カラーを解除：",
    themeCyan = "シアンクラシック (デフォルト)",
    themeMint = "フレッシュミント",
    themeSunset = "サンセットゴールド",
    themeDeepOcean = "ディープオーシャン",

    goalDialogTitle = "目標達成おめでとうございます！",
    goalDialogDesc = { goal -> "今日 $goal ml の水分を補給しました。体が感謝しています！" },
    goalDialogCta = "続ける！",

    onboardingWelcome = "Water Tracker へ\nようこそ",
    onboardingSubtitle = "EFSAの生理学的基準に基づき、毎日の水分補給目標をパーソナライズします",
    genderTitle = "1. 生物学的性別",
    genderMale = "男性",
    genderFemale = "女性",
    genderMaleRate = "35 ml/kg",
    genderFemaleRate = "31 ml/kg",
    weightTitle = { kg -> "2. 現在の体重: $kg KG" },
    activityTitle = "3. 身体活動レベル",
    activitySedentary = "軽い運動 (30分未満/日)",
    activityModerate = "中程度の運動 (30〜60分/日)",
    activityActive = "活発な運動 (60分以上/日)",
    recommendedGoalTitle = "推奨される毎日の目標",
    recommendedGoalFormat = { goal -> "$goal ml/日" },
    formulaNote = { weight, rate, offset -> "計算式: ($weight kg × $rate) + $offset ml (EFSA基準)" },
    startTrackingCta = "水分補給を始める 🚀",

    settingsLanguageSection = "言語設定",
    settingsLanguageLabel = "表示言語",
    settingsRemindersSection = "水分補給リマインダー",
    settingsReminderFrequency = "通知頻度",
    settingsReminderFreqFormat = { min -> "$min 分ごと" },
    settingsDndSection = "おやすみモード (DND)",
    settingsDndTitle = "夜間サイレントモード",
    settingsDndSubtitle = { wake, sleep -> "睡眠中は通知を停止します ($sleep - $wake)" },
    settingsWakeTime = "起床時間",
    settingsBedTime = "就寝時間",
    settingsPrivacySection = "プライバシー & ADMOB",
    settingsPrivacyTitle = "プライバシー設定 (GDPR / UMP)",
    settingsPrivacyDesc = "EU規則に基づく広告パーソナライズ同意の管理",

    historyChartTitle = "過去7日間の記録",
    streakShieldTitle = { freezes -> "連続記録シールド ($freezes 個保持)" },
    streakShieldDesc = "動画を1回視聴してフリーズシールドを獲得",
    addShieldCta = "+1 シールド",
    badgesTitle = { streak -> "ストリークバッジ ($streak 日)" },
    badgeStarter = "スタート",
    badgeStarterDays = "3日",
    badgeWeek = "1週間継続",
    badgeWeekDays = "7日",
    badgeGold = "ゴールド習慣",
    badgeGoldDays = "14日",
    badgeMaster = "マスター",
    badgeMasterDays = "30日",
    daysOfWeek = listOf("月", "火", "水", "木", "金", "土", "日")
)

val GermanStrings = Strings(
    appName = "Water Tracker",
    streakDays = { count -> if (count == 1) "1 Tag" else "$count Tage" },
    historyTitle = "Verlauf & Abzeichen",
    settingsTitle = "Einstellungen",
    back = "Zurück",
    close = "Schließen",
    cancel = "Abbrechen",
    confirm = "Speichern",
    manage = "Verwalten",

    motivationDefault = "Trinken Sie kleine Schlucke für optimale Flüssigkeitsaufnahme",
    motivationGoalReached = "Hervorragend! Sie haben Ihr Tagesziel erreicht 🎉",
    progressFormat = { current, target -> "$current / $target ml" },

    quickAdd150 = "+150ml",
    quickAdd250 = "+250ml",
    quickAdd500 = "+500ml",
    quickAddCustom = "+ Mehr",
    themesButton = "Farben",

    beverageWater = "Wasser",
    beverageTea = "Tee",
    beverageCoffee = "Kaffee",
    beverageJuice = "Saft",
    beverageMilk = "Milch",
    beverageElectrolytes = "Elektrolyte",
    bhiFeedbackFormat = { icon, raw, name, effective, factor ->
        "$icon +${raw}ml $name ➔ Netto-Hydratation: +${effective}ml ($factor% BHI)"
    },

    customDialogTitle = "Benutzerdefinierte Menge eintragen",
    customDialogSelected = { name, factor -> "Ausgewählt: $name (BHI-Index $factor%)" },
    customDialogInputLabel = "Getrunkene Menge in ml",
    customDialogNetHydration = { net -> "➔ Echte Flüssigkeitsaufnahme: +$net ml" },

    themeDialogTitle = "Wellen-Themen freischalten",
    themeDialogSubtitle = "Sehen Sie ein kurzes Video (Prämienanzeige), um neue Stile freizuschalten:",
    themeCyan = "Klassisches Cyan (Standard)",
    themeMint = "Frische Minze",
    themeSunset = "Sonnenuntergang",
    themeDeepOcean = "Tiefer Ozean",

    goalDialogTitle = "Tagesziel Erreicht!",
    goalDialogDesc = { goal -> "Sie haben heute $goal ml Wasser getrunken. Ihr Körper dankt es Ihnen!" },
    goalDialogCta = "Großartig, weiter!",

    onboardingWelcome = "Willkommen bei\nWater Tracker",
    onboardingSubtitle = "Personalisieren Sie Ihren täglichen Wasserbedarf nach physiologischen EFSA-Standards",
    genderTitle = "1. BIOLOGISCHES GESCHLECHT",
    genderMale = "Männlich",
    genderFemale = "Weiblich",
    genderMaleRate = "35 ml/kg",
    genderFemaleRate = "31 ml/kg",
    weightTitle = { kg -> "2. AKTUELLES GEWICHT: $kg KG" },
    activityTitle = "3. KÖRPERLICHE AKTIVITÄT",
    activitySedentary = "Gering (< 30 Min./Tag)",
    activityModerate = "Mäßig (30 - 60 Min./Tag)",
    activityActive = "Aktiv (> 60 Min./Tag)",
    recommendedGoalTitle = "EMPFOHLENES TAGESZIEL",
    recommendedGoalFormat = { goal -> "$goal ml/Tag" },
    formulaNote = { weight, rate, offset -> "Formel: ($weight kg × $rate) + $offset ml (EFSA-Standard)" },
    startTrackingCta = "Jetzt mit Trinken beginnen 🚀",

    settingsLanguageSection = "SPRACHE & REGION",
    settingsLanguageLabel = "App-Sprache",
    settingsRemindersSection = "TRINKERINNERUNGEN",
    settingsReminderFrequency = "Erinnerungsintervall",
    settingsReminderFreqFormat = { min -> "Alle $min Minuten" },
    settingsDndSection = "BITTE NICHT STÖREN (DND)",
    settingsDndTitle = "Nacht-Ruhemodus (DND)",
    settingsDndSubtitle = { wake, sleep -> "Keine Benachrichtigungen während des Schlafs ($sleep - $wake)" },
    settingsWakeTime = "Aufstehzeit",
    settingsBedTime = "Schlafenszeit",
    settingsPrivacySection = "DATENSCHUTZ & ADMOB",
    settingsPrivacyTitle = "Datenschutzeinstellungen (DSGVO / UMP)",
    settingsPrivacyDesc = "Werbeeinwilligung gemäß europäischen Datenschutzrichtlinien verwalten",

    historyChartTitle = "Verlauf der letzten 7 Tage",
    streakShieldTitle = { freezes -> "Serie-Schutz ($freezes verfügbar)" },
    streakShieldDesc = "Schauen Sie 1 Video für 1 Schutzschild bei verpassten Tagen",
    addShieldCta = "+1 Schild",
    badgesTitle = { streak -> "Serien-Abzeichen ($streak Tage)" },
    badgeStarter = "Anfänger",
    badgeStarterDays = "3 Tage",
    badgeWeek = "1 Woche",
    badgeWeekDays = "7 Tage",
    badgeGold = "Goldene Gewohnheit",
    badgeGoldDays = "14 Tage",
    badgeMaster = "Meister",
    badgeMasterDays = "30 Tage",
    daysOfWeek = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
)

object LocalizationManager {
    fun getStrings(code: String): Strings {
        return when (code.lowercase()) {
            "vi" -> VietnameseStrings
            "es" -> SpanishStrings
            "ja" -> JapaneseStrings
            "de" -> GermanStrings
            else -> EnglishStrings // Default US English
        }
    }
}
